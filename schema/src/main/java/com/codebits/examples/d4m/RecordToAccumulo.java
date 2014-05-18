package com.codebits.examples.d4m;

import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import com.codebits.d4m.ingest.MutationFactory;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Mutation;

public class RecordToAccumulo {

    private final Charset charset = Charset.defaultCharset();

    String row = "ZIPCODE|51001";

    String[] fieldNames = {
        "CITY_NAME",
        "STATE_NAME",
        "ZIPCODE"
    };

    String[] fieldValues = {
        "Akron",
        "IOWA",
        "51001"
    };

    public static void main(String[] args) throws IOException, AccumuloException, AccumuloSecurityException, TableNotFoundException {
        RecordToAccumulo driver = new RecordToAccumulo();
        driver.process();
    }

    private void process() throws IOException, AccumuloException, AccumuloSecurityException, TableNotFoundException {
        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties = propertyManager.load();

        String instanceName = properties.getProperty("accumulo.instance.name");
        String zooKeepers = properties.getProperty("accumulo.zookeeper.ensemble");
        String user = properties.getProperty("accumulo.user");
        byte[] pass = properties.getProperty("accumulo.password").getBytes(charset);

        ZooKeeperInstance instance = new ZooKeeperInstance(instanceName, zooKeepers);
        Connector connector = instance.getConnector(user, pass);
        
        TableManager tableManager = new TableManager(connector.tableOperations());

        MutationFactory factory = new MutationFactory();

        BatchWriter wr = null;
        try {
            wr = connector.createBatchWriter(tableManager.getEdgeTable(), 10000000, 10000, 5);
            wr.addMutation(factory.generateEdges(row, fieldNames, fieldValues));
        } finally {
            if (wr != null) {
                wr.close();
            }
        }
        
        try {
            wr = connector.createBatchWriter(tableManager.getTransposeTable(), 10000000, 10000, 5);
            for (Mutation mutation : factory.generateTranspose(row, fieldNames, fieldValues)) {
                wr.addMutation(mutation);
            }
        } finally {
            if (wr != null) {
                wr.close();
            }
        }

        try {
            wr = connector.createBatchWriter(tableManager.getDegreeTable(), 10000000, 10000, 5);
            for (Mutation mutation : factory.generateDegree(row, fieldNames, fieldValues)) {
                wr.addMutation(mutation);
            }
        } finally {
            if (wr != null) {
                wr.close();
            }
        }

        try {
            wr = connector.createBatchWriter(tableManager.getTextTable(), 10000000, 10000, 5);
            wr.addMutation(factory.generateText(row, fieldNames, fieldValues));
        } finally {
            if (wr != null) {
                wr.close();
            }
        }
        
    }

}
