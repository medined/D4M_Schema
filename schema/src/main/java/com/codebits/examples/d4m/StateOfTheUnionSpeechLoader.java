package com.codebits.examples.d4m;

import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import com.codebits.d4m.ingest.MutationFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Mutation;

public class StateOfTheUnionSpeechLoader {

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    private final Charset charset = Charset.defaultCharset();

    public static void main(String[] args) throws MalformedURLException {
        StateOfTheUnionSpeechLoader loader = new StateOfTheUnionSpeechLoader();
        loader.process(new Date(), UUID.randomUUID().toString(), new URL("https://nltk.googlecode.com/svn-history/r8660/trunk/nltk_data/packages/corpora/state_union.zip"));
        //loader.process(new Date(), UUID.randomUUID().toString(), "/Users/davidmedinets/Downloads/state_union.zip");
    }

    private void closeQuietly(BatchWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (MutationsRejectedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void process(final Date batchDate, final String batchId, final URL url) {
        try {
            URLConnection urlConnection = url.openConnection();
            process(batchDate, batchId, new ZipInputStream(urlConnection.getInputStream()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    private void process(final Date batchDate, final String batchId, final String localZipFile) {
        try {
            process(batchDate, batchId, new ZipInputStream(new FileInputStream(localZipFile)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    */
    
    private void process(final Date batchDate, final String batchId, final ZipInputStream zipInputStream) {

        byte[] lineBuffer = new byte[1024];

        try {
            ZipEntry ze = zipInputStream.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();

                if (!fileName.endsWith("/")) {

                    StringBuilder buffer = new StringBuilder();
                    while ((zipInputStream.read(lineBuffer)) > 0) {
                        buffer.append(new String(lineBuffer, charset));
                    }
                    String text = buffer.toString();

                    PropertyManager propertyManager = new PropertyManager();
                    propertyManager.setPropertyFilename("d4m.properties");
                    Properties properties = propertyManager.load();

                    String instanceName = properties.getProperty("accumulo.instance.name");
                    String zooKeepers = properties.getProperty("accumulo.zookeeper.ensemble");
                    String user = properties.getProperty("accumulo.user");
                    byte[] pass = properties.getProperty("accumulo.password").getBytes(charset);

                    ZooKeeperInstance instance = new ZooKeeperInstance(instanceName, zooKeepers);
                    Connector connector;
                    TableManager tableManager;

                    try {
                        connector = instance.getConnector(user, pass);
                        tableManager = new TableManager(connector, connector.tableOperations());
                        tableManager.createTables();
                    } catch (AccumuloException e) {
                        throw new RuntimeException(e);
                    } catch (AccumuloSecurityException e) {
                        throw new RuntimeException(e);
                    }

                    MutationFactory factory = new MutationFactory("\t", "|");

                    String row = fileName;

                    List<String> fieldNames = new ArrayList<String>();
                    fieldNames.add("file_name");
                    fieldNames.add("file_size");
                    fieldNames.add("batch_date");
                    fieldNames.add("batch_id");
                    fieldNames.add("has_text");

                    List<String> fieldValues = new ArrayList<String>();
                    fieldValues.add(fileName);
                    fieldValues.add(Integer.toString(text.length()));
                    fieldValues.add(formatter.format(batchDate));
                    fieldValues.add(batchId);
                    fieldValues.add("1");

                    BatchWriter edgeWriter = null;
                    BatchWriter transposeWriter = null;
                    BatchWriter degreeWriter = null;
                    BatchWriter fieldWriter = null;
                    BatchWriter textWriter = null;

                    try {
                        edgeWriter = connector.createBatchWriter(tableManager.getEdgeTable(), 10000000, 10000, 5);
                        edgeWriter.addMutation(factory.generateEdges(row, fieldNames, fieldValues));

                        transposeWriter = connector.createBatchWriter(tableManager.getTransposeTable(), 10000000, 10000, 5);
                        for (Mutation mutation : factory.generateTranspose(row, fieldNames, fieldValues)) {
                            transposeWriter.addMutation(mutation);
                        }

                        degreeWriter = connector.createBatchWriter(tableManager.getDegreeTable(), 10000000, 10000, 5);
                        for (Mutation mutation : factory.generateDegree(row, fieldNames, fieldValues)) {
                            degreeWriter.addMutation(mutation);
                        }

                        fieldWriter = connector.createBatchWriter(tableManager.getMetadataTable(), 10000000, 10000, 5);
                        for (Mutation mutation : factory.generateMetadata(row, fieldNames, fieldValues)) {
                            fieldWriter.addMutation(mutation);
                        }

                        textWriter = connector.createBatchWriter(tableManager.getTextTable(), 10000000, 10000, 5);
                        textWriter.addMutation(factory.generateText(row, text));

                    } catch (TableNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (MutationsRejectedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        closeQuietly(edgeWriter);
                        closeQuietly(transposeWriter);
                        closeQuietly(degreeWriter);
                        closeQuietly(fieldWriter);
                        closeQuietly(textWriter);
                    }

                }
                ze = zipInputStream.getNextEntry();
            }

            zipInputStream.closeEntry();
            zipInputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
