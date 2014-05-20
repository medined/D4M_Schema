package com.codebits.examples.d4m;

import com.codebits.d4m.CsvReader;
import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import com.codebits.d4m.ingest.MutationFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Mutation;

public class StateCSVToAccumulo {

    //private static final Logger log = Logger.getLogger(WriteMultipleExplodedRecords.class);
    private final Charset charset = Charset.defaultCharset();

    public static void main(String[] args) throws IOException, AccumuloException, AccumuloSecurityException, TableNotFoundException, TableExistsException {
        StateCSVToAccumulo driver = new StateCSVToAccumulo();
        driver.process("../data/SUB-EST2012_1.csv");

        FieldPaginationDriver fieldPaginationDriver = new FieldPaginationDriver();
        fieldPaginationDriver.process();
    }

    void process(final String csvFile) throws AccumuloException, AccumuloSecurityException, TableNotFoundException, IOException, TableExistsException {
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

        TableManager tableManager = new TableManager(connector.tableOperations());
        tableManager.setSha1(true);
        tableManager.createTables();

        MutationFactory factory = new MutationFactory();

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
                for (Mutation mutation : factory.generateField(row, fieldNames, fieldValues)) {
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
