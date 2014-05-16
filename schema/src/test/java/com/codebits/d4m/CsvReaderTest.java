package com.codebits.d4m;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
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

        List<String> expectedRecord01 = new ArrayList<String>();
        expectedRecord01.add(" Portland");
        expectedRecord01.add(" Oregon  ");

        List<String> expectedRecord02 = new ArrayList<String>();
        expectedRecord02.add("San Diego");
        expectedRecord02.add(" California");

        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(expectedRecord01);
        expected.add(expectedRecord02);

        StringBuilder buffer = new StringBuilder();
        buffer.append("CITY,STATE\n");
        buffer.append(" Portland, Oregon  \n");
        buffer.append("San Diego, California\n");
        instance.setFilename("testfile");
        instance.setReader(new BufferedReader(new StringReader(buffer.toString())));
        instance.read();
        assertEquals(expectedHeaders, instance.getFieldNames());
        assertEquals(expected, instance.getRecords());
        assertEquals(2, instance.getRecordCount());
    }

    @Test
    public void test_read_with_lowercase_fieldnames() {
        List<String> expectedHeaders = new ArrayList<String>();
        expectedHeaders.add("city");
        expectedHeaders.add("state");

        List<String> expectedRecord01 = new ArrayList<String>();
        expectedRecord01.add(" Portland");
        expectedRecord01.add(" Oregon  ");

        List<String> expectedRecord02 = new ArrayList<String>();
        expectedRecord02.add("San Diego");
        expectedRecord02.add(" California");

        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(expectedRecord01);
        expected.add(expectedRecord02);

        StringBuilder buffer = new StringBuilder();
        buffer.append("CITY,STATE\n");
        buffer.append(" Portland, Oregon  \n");
        buffer.append("San Diego, California\n");
        instance.setLowercaseFieldnames();
        instance.setFilename("testfile");
        instance.setReader(new BufferedReader(new StringReader(buffer.toString())));
        instance.read();
        assertEquals(expectedHeaders, instance.getFieldNames());
        assertEquals(expected, instance.getRecords());
        assertEquals(2, instance.getRecordCount());
    }

    @Test
    public void test_read_with_trim() {
        List<String> expectedHeaders = new ArrayList<String>();
        expectedHeaders.add("CITY");
        expectedHeaders.add("STATE");

        List<String> expectedRecord01 = new ArrayList<String>();
        expectedRecord01.add("Portland");
        expectedRecord01.add("Oregon");

        List<String> expectedRecord02 = new ArrayList<String>();
        expectedRecord02.add("San Diego");
        expectedRecord02.add("California");

        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(expectedRecord01);
        expected.add(expectedRecord02);

        StringBuilder buffer = new StringBuilder();
        buffer.append("CITY,STATE\n");
        buffer.append(" Portland, Oregon  \n");
        buffer.append("San Diego, California\n");
        instance.setTrim();
        instance.setFilename("testfile");
        instance.setReader(new BufferedReader(new StringReader(buffer.toString())));
        instance.read();
        assertEquals(expectedHeaders, instance.getFieldNames());
        assertEquals(expected, instance.getRecords());
        assertEquals(2, instance.getRecordCount());
    }

    @Test
    public void test_read_with_sha1() {
        List<String> expectedHeaders = new ArrayList<String>();
        expectedHeaders.add("d4msha1");
        expectedHeaders.add("CITY");
        expectedHeaders.add("STATE");

        List<String> expectedRecord01 = new ArrayList<String>();
        expectedRecord01.add("4f159a5452964fe95e07a36412f79f04aeaaf9cc");
        expectedRecord01.add("Portland");
        expectedRecord01.add("Oregon");

        List<String> expectedRecord02 = new ArrayList<String>();
        expectedRecord02.add("e44d63ecc5b703db9aab662fde579b9d63463965");
        expectedRecord02.add("San Diego");
        expectedRecord02.add("California");

        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(expectedRecord01);
        expected.add(expectedRecord02);

        StringBuilder buffer = new StringBuilder();
        buffer.append("CITY,STATE\n");
        buffer.append("Portland,Oregon\n");
        buffer.append("San Diego,California\n");
        instance.setSha1();
        instance.setFilename("testfile");
        instance.setReader(new BufferedReader(new StringReader(buffer.toString())));
        instance.read();
        assertEquals(expectedHeaders, instance.getFieldNames());
        assertEquals(expected, instance.getRecords());
        assertEquals(2, instance.getRecordCount());
    }

}
