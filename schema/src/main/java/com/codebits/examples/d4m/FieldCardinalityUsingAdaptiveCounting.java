package com.codebits.examples.d4m;

import com.clearspring.analytics.hash.MurmurHash;
import com.clearspring.analytics.stream.cardinality.AdaptiveCounting;
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
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;

public class FieldCardinalityUsingAdaptiveCounting {

    private final String factDelimiter = "|";
    private static final Charset charset = Charset.defaultCharset();

    public static void main(String[] args) throws IOException, AccumuloException, AccumuloSecurityException, TableNotFoundException {
        FieldCardinalityUsingAdaptiveCounting driver = new FieldCardinalityUsingAdaptiveCounting();
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

            String row = entry.getKey().getRow().toString();
            String factName = row.substring(0, row.indexOf(factDelimiter));

            long hashCode = MurmurHash.hash64(row);

            ICardinality estimator = estimators.get(factName);
            if (estimator == null) {
                estimator = new AdaptiveCounting(16);
                estimators.put(factName, estimator);
            }

            estimator.offer(hashCode);
        }

        for (Entry<String, ICardinality> entry : estimators.entrySet()) {
            String factName = entry.getKey();
            ICardinality estimator = entry.getValue();
            System.out.println(String.format("%s: %d", factName, estimator.cardinality()));
        }
    }
}
