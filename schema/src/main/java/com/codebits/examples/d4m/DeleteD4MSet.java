package com.codebits.examples.d4m;

import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;

public class DeleteD4MSet {

    private static final Charset charset = Charset.defaultCharset();

    public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException, TableExistsException, IOException {

        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties = propertyManager.load();

        String instanceName = properties.getProperty("accumulo.instance.name");
        String zooKeepers = properties.getProperty("accumulo.zookeeper.ensemble");
        String user = properties.getProperty("accumulo.user");
        byte[] pass = properties.getProperty("accumulo.password").getBytes(charset);

        ZooKeeperInstance instance = new ZooKeeperInstance(instanceName, zooKeepers);
        Connector connector = instance.getConnector(user, pass);

        TableOperations tableOperations = connector.tableOperations();

        TableManager tableManager = new TableManager(connector, tableOperations);
        //tableManager.setBaseTableName("sample");

        if (tableOperations.exists(tableManager.getEdgeTable())) {
            tableOperations.delete(tableManager.getEdgeTable());
        }
        if (tableOperations.exists(tableManager.getTransposeTable())) {
            tableOperations.delete(tableManager.getTransposeTable());
        }
        if (tableOperations.exists(tableManager.getDegreeTable())) {
            tableOperations.delete(tableManager.getDegreeTable());
        }
        if (tableOperations.exists(tableManager.getMetadataTable())) {
            tableOperations.delete(tableManager.getMetadataTable());
        }
        if (tableOperations.exists(tableManager.getTextTable())) {
            tableOperations.delete(tableManager.getTextTable());
        }
    }

}
