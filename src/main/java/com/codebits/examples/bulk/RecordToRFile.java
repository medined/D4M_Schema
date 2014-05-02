package com.codebits.examples.bulk;

import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import com.codebits.d4m.ingest.KeyFactory;
import com.codebits.d4m.ingest.MutationFactory;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.conf.DefaultConfiguration;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.file.FileSKVWriter;
import org.apache.accumulo.core.file.rfile.RFile;
import org.apache.accumulo.core.file.rfile.RFileOperations;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;

public class RecordToRFile {

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
        RecordToRFile driver = new RecordToRFile();
        driver.process();
    }

    private void process() throws IOException, AccumuloException, AccumuloSecurityException, TableNotFoundException {
        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties = propertyManager.load();

        String filesystemDefaultName = properties.getProperty("fs.default.name");

        Configuration conf = new Configuration();
        conf.set("fs.default.name", filesystemDefaultName);
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        FileSystem fs = FileSystem.get(conf);

        String input = "/user/566453/rfiles";

        try {
            fs.delete(new Path(input), true);
        } catch (AccessControlException e) {
            // ignore
        }

        try {
            fs.mkdirs(new Path(input));
        } catch (AccessControlException e) {
            throw new RuntimeException("Please fix the permissions. Perhaps create parent directories?", e);
        }

        DefaultConfiguration defaultConfiguration = AccumuloConfiguration.getDefaultConfiguration();
        FileSKVWriter out = null;
        String rfile;

        TableManager tableManager = new TableManager();
        KeyFactory factory = new KeyFactory();

        // edges
        rfile = String.format("%s/%s.rf", input, tableManager.getEdgeTable());
        try {
            out = RFileOperations.getInstance().openWriter(rfile, fs, conf, defaultConfiguration);
            out.startDefaultLocalityGroup();

            for (Entry<Key, Value> entry : factory.generateEdges(row, fieldNames, fieldValues).entrySet()) {
                out.append(entry.getKey(), entry.getValue());
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }

        // transpose
        rfile = String.format("%s/%s.rf", input, tableManager.getTransposeTable());
        try {
            out = RFileOperations.getInstance().openWriter(rfile, fs, conf, defaultConfiguration);
            out.startDefaultLocalityGroup();

            for (Entry<Key, Value> entry : factory.generateTranspose(row, fieldNames, fieldValues).entrySet()) {
                out.append(entry.getKey(), entry.getValue());
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }

        rfile = String.format("%s/%s.rf", input, tableManager.getDegreeTable());
        try {
            out = RFileOperations.getInstance().openWriter(rfile, fs, conf, defaultConfiguration);
            out.startDefaultLocalityGroup();

            for (Entry<Key, Value> entry : factory.generateDegree(row, fieldNames, fieldValues).entrySet()) {
                out.append(entry.getKey(), entry.getValue());
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }

        rfile = String.format("%s/%s.rf", input, tableManager.getTextTable());
        try {
            out = RFileOperations.getInstance().openWriter(rfile, fs, conf, defaultConfiguration);
            out.startDefaultLocalityGroup();

            for (Entry<Key, Value> entry : factory.generateText(row, fieldNames, fieldValues).entrySet()) {
                out.append(entry.getKey(), entry.getValue());
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }

}
