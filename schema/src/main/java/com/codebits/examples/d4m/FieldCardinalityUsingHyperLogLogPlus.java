package com.codebits.examples.d4m;

import com.clearspring.analytics.hash.MurmurHash;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;
import com.clearspring.analytics.stream.cardinality.ICardinality;
import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;

/* This program reads the TedgeDegree to estimate cardinality of the
 * field names. For example, given this data:
 *
 * first_name: david  record01
 * first_name: john   record02
 * first_name: john   record03
 *
 * The entry count for first_name is 3. However, the cardinality is 2.
 *
 * Cardinality can be important in some analytic situations.
 *
 */
public class FieldCardinalityUsingHyperLogLogPlus {

    private final String factDelimiter = "|";
    private static final Charset charset = Charset.defaultCharset();

    public static void main(String[] args) throws IOException, AccumuloException, AccumuloSecurityException, TableNotFoundException {
        FieldCardinalityUsingHyperLogLogPlus driver = new FieldCardinalityUsingHyperLogLogPlus();
        driver.process(args);
    }

    public void process(String[] args) throws IOException, AccumuloException, AccumuloSecurityException, TableNotFoundException {

        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties = propertyManager.load();

        String instanceName = properties.getProperty("accumulo.instance.name");
        String zooKeepers = properties.getProperty("accumulo.zookeeper.ensemble");
        String user = properties.getProperty("accumulo.user");
        byte[] pass = properties.getProperty("accumulo.password").getBytes(charset);

        ZooKeeperInstance instance = new ZooKeeperInstance(instanceName, zooKeepers);
        Connector connector = instance.getConnector(user, pass);

        TableManager tableManager = new TableManager(connector, connector.tableOperations());
        String tableName = tableManager.getDegreeTable();

        Map<String, ICardinality> estimators = new TreeMap<String, ICardinality>();

        Scanner scan = connector.createScanner(tableName, new Authorizations());
        Iterator<Map.Entry<Key, Value>> iterator = scan.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();

            // ROW format: census2010pop|10031 :degree []    1
            String fieldInfo = entry.getKey().getRow().toString();

            // factName format: census2010pop
            String factName = fieldInfo.substring(0, fieldInfo.indexOf(factDelimiter));

            // Turn the combiniation of fact and value into a hash.
            long hashCode = MurmurHash.hash64(fieldInfo);

            // Create a Cardinality Estimator for each fact.
            ICardinality estimator = estimators.get(factName);
            if (estimator == null) {
                estimator = new HyperLogLogPlus(16);
                estimators.put(factName, estimator);
            }

            // Add the fact and value into the estimator.
            estimator.offer(hashCode);
        }

        //Writing this entry format:
        //field cardinality:census2010pop [] 640
        Text field = new Text("field");
        Text cardinality = new Text("cardinality");
        Mutation mutation = new Mutation(field);
        for (Entry<String, ICardinality> entry : estimators.entrySet()) {
            Text factName = new Text(entry.getKey());
            ICardinality estimator = entry.getValue();
            Value cardinalityEstimate = new Value(Long.toString(estimator.cardinality()).getBytes(charset));
            mutation.put(cardinality, factName, cardinalityEstimate);
        }
        
        // New cardinality values overwrite the old values.
        BatchWriter writer = connector.createBatchWriter(tableManager.getMetadataTable(), 10000000, 10000, 5);
        writer.addMutation(mutation);
        writer.close();
    }
}
