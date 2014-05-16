package com.codebits.d4m;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.security.Authorizations;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class FieldPaginationPrecomputorTest {
    
    private FieldPaginationPrecomputor instance = null;
    
    @Before
    public void setUp() {
        instance = new FieldPaginationPrecomputor();
    }

    @Test
    public void testComputePageBreaks() {
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
