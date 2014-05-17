package com.codebits.examples.bulk;

import com.codebits.d4m.PropertyManager;
import com.codebits.hadoop.util.CreateOrReplaceHadoopDirectory;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.file.FileSKVWriter;
import org.apache.accumulo.core.file.rfile.RFileOperations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

public class CreateExampleRFile {
    
    public static void main(String[] args) throws IOException {
        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties = propertyManager.load();

        String filesystemDefaultName = properties.getProperty("fs.default.name");
        String hadoopUserHomeDirectory = properties.getProperty("hadoop.user.home.directory");

        Configuration conf = new Configuration();
        conf.set("fs.default.name", filesystemDefaultName);
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        FileSystem fs = FileSystem.get(conf);

        String input = hadoopUserHomeDirectory + "/rfiles";
        
        new CreateOrReplaceHadoopDirectory().mkdirs(fs, input);

        String filename = input + "/testFile.rf";
        Path file = new Path(filename);
        if (fs.exists(file)) {
            file.getFileSystem(conf).delete(file, false);
        }
        FileSKVWriter out = RFileOperations.getInstance().openWriter(filename, fs, conf, AccumuloConfiguration.getDefaultConfiguration());
        out.startDefaultLocalityGroup();
        long timestamp = new Date().getTime();
        
        Key key = new Key(new Text("row_1"), new Text("cf"), new Text("cq"), new ColumnVisibility(), timestamp);
        Value value = new Value("".getBytes());
        out.append(key, value);

        out.close();
    }
}
