package com.codebits.examples.d4m;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;

public class DisplayD4MSet {

    public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException, TableExistsException, IOException {

        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties = propertyManager.load();

        String instanceName = properties.getProperty("accumulo.instance.name");
        String zooKeepers = properties.getProperty("accumulo.zookeeper.ensemble");
        String user = properties.getProperty("accumulo.user");
        byte[] pass = properties.getProperty("accumulo.password").getBytes();

        ZooKeeperInstance instance = new ZooKeeperInstance(instanceName, zooKeepers);
        Connector connector = instance.getConnector(user, pass);

        TableManager tableManager = new TableManager(connector.tableOperations());
        //tableManager.setBaseTableName("sample");

        System.out.println("*****" + tableManager.getEdgeTable());
        Scanner scan = connector.createScanner(tableManager.getEdgeTable(), new Authorizations());
        Iterator<Map.Entry<Key, Value>> iterator = scan.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();
            System.out.println(String.format("row(%s) cq(%s)", entry.getKey().getRow(), entry.getKey().getColumnQualifier()));
        }

        System.out.println("*****" + tableManager.getTransposeTable());
        scan = connector.createScanner(tableManager.getTransposeTable(), new Authorizations());
        iterator = scan.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();
            System.out.println(String.format("row(%s) cq(%s)", entry.getKey().getRow(), entry.getKey().getColumnQualifier()));
        }

        System.out.println("*****" + tableManager.getDegreeTable());
        scan = connector.createScanner(tableManager.getDegreeTable(), new Authorizations());
        iterator = scan.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();
            System.out.println(String.format("row(%s) value(%s)", entry.getKey().getRow(), entry.getValue()));
        }

        System.out.println("*****" + tableManager.getTextTable());
        scan = connector.createScanner(tableManager.getTextTable(), new Authorizations());
        iterator = scan.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();
            System.out.println(String.format("row(%s) text(%s)", entry.getKey().getRow(), entry.getValue()));
        }
    }
}
