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

public class CreateD4MSet {
    
    private static  final Charset charset = Charset.defaultCharset();

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

        TableManager tableManager = new TableManager(connector.tableOperations());
        tableManager.createTables();
        tableManager.addSplitsForSha1();
        
    }

}
