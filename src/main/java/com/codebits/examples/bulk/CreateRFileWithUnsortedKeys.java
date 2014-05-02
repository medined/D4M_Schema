package com.codebits.examples.bulk;

import java.io.IOException;
import java.util.Date;
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

public class CreateRFileWithUnsortedKeys {
    
    private static final String FILE_TYPE = "filetype";

    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        conf.set("fs.default.name", "hdfs://affy-master:9000/");
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        FileSystem fs = FileSystem.get(conf);

        Path input = new Path("./input");
        Path output = new Path("./output");
        
        try {
            fs.mkdirs(input);
            fs.mkdirs(output);
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
        
        Key key = new Key(new Text("ZZZ"), new Text("cf"), new Text("cq"), new ColumnVisibility(), timestamp);
        Value value = new Value("".getBytes());
        out.append(key, value);

        key = new Key(new Text("AAA"), new Text("cf"), new Text("cq"), new ColumnVisibility(), timestamp);
        value = new Value("".getBytes());
        out.append(key, value);
        
        out.close();
    }
}
