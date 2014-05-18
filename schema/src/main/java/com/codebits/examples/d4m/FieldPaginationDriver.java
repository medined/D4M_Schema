package com.codebits.examples.d4m;

import com.codebits.d4m.FieldPaginationPrecomputor;
import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;

/**
 * Pre-compute page breaks for the column names in the TedgeField table.
 *
 * Page sizes of 10, 50, and 100 are pre-computed and the page break values are
 * written to the TedgeField table. Previous page breaks are deleted.
 *
 * In order to show records added since the pre-computation use the timestamp
 * field of the num_pages entry.
 */
public class FieldPaginationDriver {
    
    private final Charset charset = Charset.defaultCharset();
    private String propertyFile = "d4m.properties";

    public static void main(final String[] args) {
        FieldPaginationDriver fieldsPaginationDriver = new FieldPaginationDriver();
        fieldsPaginationDriver.process();
    }

    public void process() {

        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename(propertyFile);
        Properties properties;
        try {
            properties = propertyManager.load();
        } catch (IOException e) {
            throw new RuntimeException("Problem loading properties from " + propertyFile, e);
        }

        String instanceName = properties.getProperty("accumulo.instance.name");
        String zooKeepers = properties.getProperty("accumulo.zookeeper.ensemble");
        String user = properties.getProperty("accumulo.user");
        byte[] pass = properties.getProperty("accumulo.password").getBytes(charset);

        ZooKeeperInstance instance = new ZooKeeperInstance(instanceName, zooKeepers);
        Connector connector;
        BatchWriter wr = null;
        String tableName = null;

        try {
            connector = instance.getConnector(user, pass);
            TableManager tableManager = new TableManager(connector.tableOperations());
            tableName = tableManager.getFieldTable();

            final int pageSizes[] = {5, 10, 50, 100};
            final Text cqPages = new Text("pages");

            wr = connector.createBatchWriter(tableManager.getFieldTable(), 10000000, 10000, 5);
            for (int pageSize : pageSizes) {
                int pageNumbers = 0;
                FieldPaginationPrecomputor precomputor = new FieldPaginationPrecomputor();
                precomputor.setAuthorizations(new Authorizations());
                precomputor.setConnector(connector);
                precomputor.setPageSize(pageSize);
                precomputor.setTableName(tableName);
                Map<Integer, Text> pageBreaks = precomputor.computePageBreaks();

                Text row = new Text(String.format("%d_per_page", pageSize));
                Text endRow = new Text(String.format("%d_per_page\\0", pageSize));

                connector.tableOperations().deleteRows(tableName, row, endRow);

                Mutation mutation = new Mutation(new Text(String.format("%d_per_page", pageSize)));
                for (Entry<Integer, Text> entry : pageBreaks.entrySet()) {
                    Text cfPageNumber = new Text(Integer.toString(entry.getKey()));
                    Value value = new Value(entry.getValue().getBytes());
                    mutation.put(cfPageNumber, cqPages, value);
                    pageNumbers++;
                }

                mutation.put(new Text("num_pages"), cqPages, new Value(Integer.toString(pageNumbers).getBytes(charset)));
                // get the pagination timestamp from the num_pages entry.
                wr.addMutation(mutation);
            }
        } catch (AccumuloException e) {
            throw new RuntimeException("Problem connecting to Accumulo.", e);
        } catch (AccumuloSecurityException e) {
            throw new RuntimeException("Security Problem connecting to Accumulo.", e);
        } catch (TableNotFoundException e) {
            throw new RuntimeException("Problem reading " + tableName, e);
        } finally {
            if (wr != null) {
                try {
                    wr.close();
                } catch (MutationsRejectedException e) {
                    throw new RuntimeException("Problem writing mutations.", e);
                }
            }
        }

    }

    public void setPropertyFile(String propertyFile) {
        this.propertyFile = propertyFile;
    }
}
