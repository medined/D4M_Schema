package com.codebits.examples.d4m;

import com.codebits.d4m.CsvReader;
import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import com.codebits.d4m.ingest.MutationFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Mutation;

/* Download 11zpallagi.csv from
 * http://www.irs.gov/uac/SOI-Tax-Stats-Individual-Income-Tax-Statistics-2011-ZIP-Code-Data-%28SOI%29
 *
 * The ingest took about 11 minutes.
 * The ingest took about 6 minutes with pre-split edge and degree tables.
 */

public class SOICSVToAccumulo {

    private final Charset charset = Charset.defaultCharset();

    public static void main(String[] args) throws IOException, AccumuloException, AccumuloSecurityException, TableNotFoundException {
        SOICSVToAccumulo driver = new SOICSVToAccumulo();
        driver.process("../data/11zpallagi.csv");

        FieldPaginationDriver fieldPaginationDriver = new FieldPaginationDriver();
        fieldPaginationDriver.process();
    }

    void process(final String csvFile) throws AccumuloException, AccumuloSecurityException, TableNotFoundException, IOException {
        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties = propertyManager.load();

        String instanceName = properties.getProperty("accumulo.instance.name");
        String zooKeepers = properties.getProperty("accumulo.zookeeper.ensemble");
        String user = properties.getProperty("accumulo.user");
        byte[] pass = properties.getProperty("accumulo.password").getBytes(charset);

        CsvReader reader = new CsvReader();
        reader.setLowercaseFieldnames(true);
        reader.setSha1(true);
        reader.setTrim(true);
        reader.setFilename(csvFile);
        reader.setReader(new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), charset)));
        reader.read();
        List<String> fieldNameList = reader.getFieldNames();
        List<List<String>> records = reader.getRecords();
        
        String[] fieldNames = fieldNameList.toArray(new String[fieldNameList.size()]);
        
        ZooKeeperInstance instance = new ZooKeeperInstance(instanceName, zooKeepers);
        Connector connector = instance.getConnector(user, pass);

        TableManager tableManager = new TableManager(connector, connector.tableOperations());
        tableManager.createTables();
        tableManager.addSplitsForSha1();

        MutationFactory factory = new MutationFactory("\t", "|");

        BatchWriter edgeWriter = null;
        BatchWriter transposeWriter = null;
        BatchWriter degreeWriter = null;
        BatchWriter fieldWriter = null;
        BatchWriter textWriter = null;

        try {
            edgeWriter = connector.createBatchWriter(tableManager.getEdgeTable(), 10000000, 10000, 5);
            transposeWriter = connector.createBatchWriter(tableManager.getTransposeTable(), 10000000, 10000, 5);
            degreeWriter = connector.createBatchWriter(tableManager.getDegreeTable(), 10000000, 10000, 5);
            fieldWriter = connector.createBatchWriter(tableManager.getMetadataTable(), 10000000, 10000, 5);
            textWriter = connector.createBatchWriter(tableManager.getTextTable(), 10000000, 10000, 5);

            for (List<String> fieldValueList : records) {
                String[] fieldValues = fieldValueList.toArray(new String[fieldValueList.size()]);
                String row = fieldValues[0]; // grab sha1.
                edgeWriter.addMutation(factory.generateEdges(row, fieldNames, fieldValues));
                for (Mutation mutation : factory.generateTranspose(row, fieldNames, fieldValues)) {
                    transposeWriter.addMutation(mutation);
                }
                for (Mutation mutation : factory.generateDegree(row, fieldNames, fieldValues)) {
                    degreeWriter.addMutation(mutation);
                }
                for (Mutation mutation : factory.generateMetadata(row, fieldNames, fieldValues)) {
                    fieldWriter.addMutation(mutation);
                }
                textWriter.addMutation(factory.generateRawData(row, fieldNames, fieldValues));
            }

        } finally {
            if (edgeWriter != null) {
                edgeWriter.close();
            }
            if (transposeWriter != null) {
                transposeWriter.close();
            }
            if (degreeWriter != null) {
                degreeWriter.close();
            }
            if (fieldWriter != null) {
                fieldWriter.close();
            }
            if (textWriter != null) {
                textWriter.close();
            }
        }

    }

}
