package com.codebits.examples.bulk;

import com.codebits.d4m.PropertyManager;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.file.FileSKVWriter;
import org.apache.accumulo.core.file.rfile.RFile;
import org.apache.accumulo.core.file.rfile.RFileOperations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.security.AccessControlException;

public class CreateRFile {
    
    private static final String FILE_TYPE = "filetype";

    public static void main(String[] args) throws IOException {
        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties = propertyManager.load();

        String filesystemDefaultName = properties.getProperty("fs.default.name");

        Configuration conf = new Configuration();
        conf.set("fs.default.name", filesystemDefaultName);
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        FileSystem fs = FileSystem.get(conf);

        Path input = new Path("./input");
        
        try {
            fs.mkdirs(input);
        } catch (AccessControlException e) {
            throw new RuntimeException("Please fix the permissions. Perhaps create parent directories?", e);
        }

        String extension = conf.get(FILE_TYPE);
        if (extension == null || extension.isEmpty()) {
            extension = RFile.EXTENSION;
        }
        String filename = "./input/testFile." + extension;
        Path file = new Path(filename);
        if (fs.exists(file)) {
            file.getFileSystem(conf).delete(file, false);
        }
        FileSKVWriter out = RFileOperations.getInstance().openWriter(filename, fs, conf, AccumuloConfiguration.getDefaultConfiguration());
        out.startDefaultLocalityGroup();
        long timestamp = (new Date()).getTime();
        
        Key key = new Key(new Text("row_1"), new Text("cf"), new Text("cq"), new ColumnVisibility(), timestamp);
        Value value = new Value("".getBytes());
        out.append(key, value);

        out.close();
    }
}
