package com.codebits.examples.bulk;

import com.codebits.d4m.PropertyManager;
import java.io.IOException;
import java.util.Properties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;

public class ImportExampleRFile {

    public static void main(String[] args) throws IOException, AccumuloException, AccumuloSecurityException, TableNotFoundException, TableExistsException {
        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties = propertyManager.load();

        String instanceName = properties.getProperty("accumulo.instance.name");
        String zooKeepers = properties.getProperty("accumulo.zookeeper.ensemble");
        String user = properties.getProperty("accumulo.user");
        byte[] pass = properties.getProperty("accumulo.password").getBytes();
        String filesystemDefaultName = properties.getProperty("fs.default.name");
        String hadoopUserHomeDirectory = properties.getProperty("hadoop.user.home.directory");
        
        String input = hadoopUserHomeDirectory + "/rfiles";
        String failure = hadoopUserHomeDirectory + "/failures";

        Configuration conf = new Configuration();
        conf.set("fs.default.name", filesystemDefaultName);
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        CachedConfiguration.setInstance(conf);

        FileSystem fs = FileSystem.get(conf);

        try {
            fs.mkdirs(new Path(failure));
        } catch (AccessControlException e) {
            throw new RuntimeException("Please fix the permissions. Perhaps create parent directories?", e);
        }
        
        ZooKeeperInstance instance = new ZooKeeperInstance(instanceName, zooKeepers);
        Connector connector = instance.getConnector(user, pass);
        TableOperations tableOperations = connector.tableOperations();
        if (tableOperations.exists("mynewtesttable")) {
            tableOperations.delete("mynewtesttable");
        }
        tableOperations.create("mynewtesttable");
        tableOperations.importDirectory("mynewtesttable", input, failure, false);
    }

}
