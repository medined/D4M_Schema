package com.codebits.examples.bulk;

import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import com.codebits.hadoop.util.CreateOrReplaceHadoopDirectory;
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
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

public class ImportD4MRFiles {

    private static final Charset charset = Charset.defaultCharset();

    public static void main(String[] args) throws IOException, AccumuloException, AccumuloSecurityException, TableNotFoundException, TableExistsException {
        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties = propertyManager.load();

        String instanceName = properties.getProperty("accumulo.instance.name");
        String zooKeepers = properties.getProperty("accumulo.zookeeper.ensemble");
        String user = properties.getProperty("accumulo.user");
        byte[] pass = properties.getProperty("accumulo.password").getBytes(charset);
        String filesystemDefaultName = properties.getProperty("fs.default.name");
        String hadoopUserHomeDirectory = properties.getProperty("hadoop.user.home.directory");

        String input = hadoopUserHomeDirectory + "/rfiles";
        String failure = hadoopUserHomeDirectory + "/failures";

        Configuration conf = new Configuration();
        conf.set("fs.default.name", filesystemDefaultName);
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        CachedConfiguration.setInstance(conf);

        FileSystem fs = FileSystem.get(conf);

        new CreateOrReplaceHadoopDirectory().mkdirs(fs, failure);

        ZooKeeperInstance instance = new ZooKeeperInstance(instanceName, zooKeepers);
        Connector connector = instance.getConnector(user, pass);

        TableOperations tableOperations = connector.tableOperations();

        TableManager tableManager = new TableManager(connector, tableOperations);
        tableManager.createTables();

        String rfile;

        rfile = String.format("%s/%s.rf", input, tableManager.getEdgeTable());
        tableOperations.importDirectory(tableManager.getEdgeTable(), rfile, failure, false);

        rfile = String.format("%s/%s.rf", input, tableManager.getTransposeTable());
        tableOperations.importDirectory(tableManager.getTransposeTable(), rfile, failure, false);

        rfile = String.format("%s/%s.rf", input, tableManager.getDegreeTable());
        tableOperations.importDirectory(tableManager.getDegreeTable(), rfile, failure, false);

        rfile = String.format("%s/%s.rf", input, tableManager.getMetadataTable());
        tableOperations.importDirectory(tableManager.getMetadataTable(), rfile, failure, false);

        rfile = String.format("%s/%s.rf", input, tableManager.getTextTable());
        tableOperations.importDirectory(tableManager.getTextTable(), rfile, failure, false);
    }

}
