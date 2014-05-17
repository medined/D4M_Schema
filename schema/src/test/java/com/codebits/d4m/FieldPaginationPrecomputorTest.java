package com.codebits.d4m;

import java.util.Map;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class FieldPaginationPrecomputorTest {

    private FieldPaginationPrecomputor instance = null;

    private Instance mock = null;
    private Connector connector = null;
    private String tableName = "TedgeField";

    @Before
    public void setUp() throws AccumuloException, AccumuloSecurityException, TableNotFoundException, TableExistsException {
        mock = new MockInstance("development");
        connector = mock.getConnector("root", "".getBytes());
        
        if (connector.tableOperations().exists(tableName)) {
            connector.tableOperations().delete(tableName);
        }
        connector.tableOperations().create(tableName);
        
        instance = new FieldPaginationPrecomputor();
        instance.setAuthorizations(new Authorizations());
        instance.setConnector(connector);
        instance.setTableName(tableName);
        instance.setPageSize(3);

        String[] fieldNames = { "A", "B", "C", "D", "E", "F", "G" };
        int entryCount = 123;
        
        /* Format of TedgeField table.
        * A :field []    123
        * B :field []    124
        */

        Text emptyCF = new Text("");
        Text field = new Text("field");
        
        BatchWriter writer = connector.createBatchWriter(tableName, 10000000, 10000, 5);
        for (String fieldName : fieldNames) {
            Mutation m = new Mutation(new Text(fieldName));
            m.put(emptyCF, field, new Value(Integer.toString(entryCount).getBytes()));
            writer.addMutation(m);
            entryCount++;
        }
        writer.close();
    }

    @Test
    public void testComputePageBreaks() {
        Map<Integer, Text> pageBreaks = instance.computePageBreaks();
        System.out.println("pageBreaks: " + pageBreaks);
        assertEquals("{1=A, 2=D, 3=G}", pageBreaks.toString());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testComputePageBreaks_missing_authorizations() {
        instance = new FieldPaginationPrecomputor();
        instance.computePageBreaks();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputePageBreaks_missing_connector() {
        instance = new FieldPaginationPrecomputor();
        instance.setAuthorizations(new Authorizations());
        instance.computePageBreaks();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputePageBreaks_missing_tablename() {
        instance = new FieldPaginationPrecomputor();
        instance.setAuthorizations(new Authorizations());
        instance.setConnector(connector);
        instance.computePageBreaks();
    }

    @Test(expected = D4MException.class)
    public void testComputePageBreaks_incorrect_table() {
        instance.setTableName("AAA");
        instance.computePageBreaks();
    }
    
    @Test
    public void testSetTableName() {
        final String expected = "AAA";
        instance.setTableName(expected);
        assertEquals(expected, instance.getTableName());
    }

    @Test
    public void testSetConnector() {
        Connector expected = mock(Connector.class);
        instance.setConnector(expected);
        assertEquals(expected, instance.getConnector());
    }

    @Test
    public void testSetPageSize() {
        int expected = 23;
        instance.setPageSize(expected);
        assertEquals(expected, instance.getPageSize());
    }

    @Test
    public void testSetAuthorizations() {
        Authorizations expected = new Authorizations();
        instance.setAuthorizations(expected);
        assertEquals(expected, instance.getAuthorizations());
    }

}
