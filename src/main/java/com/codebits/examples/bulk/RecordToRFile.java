package com.codebits.examples.bulk;

import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import com.codebits.d4m.ingest.KeyFactory;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.conf.DefaultConfiguration;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.file.FileSKVWriter;
import org.apache.accumulo.core.file.rfile.RFileOperations;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;

public class RecordToRFile {

    Configuration configuration = null;
    DefaultConfiguration defaultConfiguration = null;
    FileSystem fileSystem = null;
    KeyFactory factory = new KeyFactory();
    TableManager tableManager = new TableManager();
    String input = null;

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
        String hadoopUserHomeDirectory = properties.getProperty("hadoop.user.home.directory");

        configuration = new Configuration();
        configuration.set("fs.default.name", filesystemDefaultName);
        configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        fileSystem = FileSystem.get(configuration);

        input = hadoopUserHomeDirectory + "/rfiles";

        try {
            fileSystem.delete(new Path(input), true);
        } catch (AccessControlException e) {
            // ignore
        }
        try {
            fileSystem.mkdirs(new Path(input));
        } catch (AccessControlException e) {
            throw new RuntimeException("Please fix the permissions. Perhaps create parent directories?", e);
        }

        defaultConfiguration = AccumuloConfiguration.getDefaultConfiguration();

        writeRFile(tableManager.getEdgeTable(), factory.generateEdges(row, fieldNames, fieldValues));
        writeRFile(tableManager.getTransposeTable(), factory.generateTranspose(row, fieldNames, fieldValues));
        writeRFile(tableManager.getDegreeTable(), factory.generateDegree(row, fieldNames, fieldValues));
        writeRFile(tableManager.getTextTable(), factory.generateText(row, fieldNames, fieldValues));
    }
    
    private void writeRFile(final String tableName, final Map<Key, Value> entries) {
        final String rFile = String.format("%s/%s.rf", input, tableName);
        FileSKVWriter out = null;
        try {
            out = RFileOperations.getInstance().openWriter(rFile, fileSystem, configuration, defaultConfiguration);
            out.startDefaultLocalityGroup();

            for (Entry<Key, Value> entry : entries.entrySet()) {
                out.append(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
