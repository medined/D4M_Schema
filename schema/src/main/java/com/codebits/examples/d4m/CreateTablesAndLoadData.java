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

public class CreateTablesAndLoadData {
    
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

        TableManager tableManager = new TableManager(connector.tableOperations());
        tableManager.setSha1();
        tableManager.createTables();
        
        SOICSVToAccumulo soicLoader = new SOICSVToAccumulo();
        soicLoader.process("../data/11zpallagi.csv");

        StateCSVToAccumulo stateLoader = new StateCSVToAccumulo();
        stateLoader.process("../data/SUB-EST2012_1.csv");

        FieldPaginationDriver fieldPaginationDriver = new FieldPaginationDriver();
        fieldPaginationDriver.process();
    }

}
