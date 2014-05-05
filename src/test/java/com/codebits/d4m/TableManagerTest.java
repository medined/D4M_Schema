package com.codebits.d4m;

import java.util.SortedSet;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class TableManagerTest {

    TableOperations mockTableOperations = mock(TableOperations.class);

    private TableManager instance = null;

    @Before
    public void setup() {
        instance = new TableManager();
        instance.setTableOperations(mockTableOperations);
    }

    @Test
    public void testCreateTables() throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException {
        when(mockTableOperations.exists("Tedge")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeTranspose")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeDegree")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeText")).thenReturn(Boolean.FALSE);
        instance.createTables();
        verify(mockTableOperations, times(4)).exists(any(String.class));
        verify(mockTableOperations).create("Tedge");
        verify(mockTableOperations).create("TedgeTranspose");
        verify(mockTableOperations).create("TedgeDegree", false);
        verify(mockTableOperations).create("TedgeText");
        verify(mockTableOperations).attachIterator(matches("TedgeDegree"), any(IteratorSetting.class));
        verifyNoMoreInteractions(mockTableOperations);
    }

    @Test
    public void testCreateTables_adds_splits_for_sha1() throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException {
        when(mockTableOperations.exists("Tedge")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeTranspose")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeDegree")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeText")).thenReturn(Boolean.FALSE);
        instance.setSha1();
        instance.createTables();
        verify(mockTableOperations, times(4)).exists(any(String.class));
        verify(mockTableOperations).create("Tedge");
        verify(mockTableOperations).create("TedgeTranspose");
        verify(mockTableOperations).create("TedgeDegree", false);
        verify(mockTableOperations).create("TedgeText");
        verify(mockTableOperations).addSplits(matches("Tedge"), any(SortedSet.class));
        verify(mockTableOperations).addSplits(matches("TedgeText"), any(SortedSet.class));
        verify(mockTableOperations).attachIterator(matches("TedgeDegree"), any(IteratorSetting.class));
        verifyNoMoreInteractions(mockTableOperations);
    }
    
    @Test
    public void testCreateTables_with_all_existing_does_nothing() throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException {
        when(mockTableOperations.exists("Tedge")).thenReturn(Boolean.TRUE);
        when(mockTableOperations.exists("TedgeTranspose")).thenReturn(Boolean.TRUE);
        when(mockTableOperations.exists("TedgeDegree")).thenReturn(Boolean.TRUE);
        when(mockTableOperations.exists("TedgeText")).thenReturn(Boolean.TRUE);
        instance.createTables();
        verify(mockTableOperations).exists("Tedge");
        verify(mockTableOperations).exists("TedgeTranspose");
        verify(mockTableOperations).exists("TedgeDegree");
        verify(mockTableOperations).exists("TedgeText");
        verifyNoMoreInteractions(mockTableOperations);
    }

    @Test(expected = D4MException.class)
    public void testCreateTables_with_just_edge_table() throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException {
        when(mockTableOperations.exists("Tedge")).thenReturn(Boolean.TRUE);
        when(mockTableOperations.exists("TedgeTranspose")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeDegree")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeText")).thenReturn(Boolean.FALSE);
        instance.createTables();
    }

    @Test(expected = D4MException.class)
    public void testCreateTables_with_just_transpose_table() throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException {
        when(mockTableOperations.exists("Tedge")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeTranspose")).thenReturn(Boolean.TRUE);
        when(mockTableOperations.exists("TedgeDegree")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeText")).thenReturn(Boolean.FALSE);
        instance.createTables();
    }

    @Test(expected = D4MException.class)
    public void testCreateTables_with_just_degree_table() throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException {
        when(mockTableOperations.exists("Tedge")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeTranspose")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeDegree")).thenReturn(Boolean.TRUE);
        when(mockTableOperations.exists("TedgeText")).thenReturn(Boolean.FALSE);
        instance.createTables();
    }

    @Test(expected = D4MException.class)
    public void testCreateTables_with_just_text_table() throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException {
        when(mockTableOperations.exists("Tedge")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeTranspose")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeDegree")).thenReturn(Boolean.FALSE);
        when(mockTableOperations.exists("TedgeText")).thenReturn(Boolean.TRUE);
        instance.createTables();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTables_with_null_tableOperation() throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException {
        instance.setTableOperations(null);
        instance.createTables();
    }

}
