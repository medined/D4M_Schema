package com.codebits.d4m;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CsvReaderTest {

    private CsvReader instance = null;

    @Before
    public void setUp() {
        instance = new CsvReader();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_with_no_filename() {
        instance.read();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_with_no_reader() {
        instance.setFilename("testfile");
        instance.read();
    }

    @Test
    public void test_read() {
        List<String> expectedHeaders = new ArrayList<String>();
        expectedHeaders.add("CITY");
        expectedHeaders.add("STATE");

        String[] expectedRecord01 = { " Portland", " Oregon  " };
        String[] expectedRecord02 = { "San Diego", " California" };
        StringBuilder buffer = new StringBuilder();
        buffer.append("CITY,STATE\n");
        buffer.append(" Portland, Oregon  \n");
        buffer.append("San Diego, California\n");
        instance.setFilename("testfile");
        instance.setReader(new BufferedReader(new StringReader(buffer.toString())));
        instance.read();
        List<String[]> records = instance.getRecords();
        assertEquals(expectedHeaders, instance.getFieldNames());
        assertArrayEquals(expectedRecord01, records.get(0));
        assertArrayEquals(expectedRecord02, records.get(1));
        assertEquals(2, instance.getRecordCount());
    }

    @Test
    public void test_read_with_trim() {
        List<String> expectedHeaders = new ArrayList<String>();
        expectedHeaders.add("CITY");
        expectedHeaders.add("STATE");

        String[] expectedRecord01 = { "Portland", "Oregon" };
        String[] expectedRecord02 = { "San Diego", "California" };
        StringBuilder buffer = new StringBuilder();
        buffer.append("CITY,STATE\n");
        buffer.append(" Portland, Oregon  \n");
        buffer.append("San Diego, California\n");
        instance.setTrim();
        instance.setFilename("testfile");
        instance.setReader(new BufferedReader(new StringReader(buffer.toString())));
        instance.read();
        List<String[]> records = instance.getRecords();
        assertEquals(expectedHeaders, instance.getFieldNames());
        assertArrayEquals(expectedRecord01, records.get(0));
        assertArrayEquals(expectedRecord02, records.get(1));
        assertEquals(2, instance.getRecordCount());
    }

}
