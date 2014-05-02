package com.codebits.d4m.ingest;

import com.codebits.d4m.TestableMutation;
import java.util.Map;
import java.util.TreeMap;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class KeyFactoryTest {

    private static final Value one = new Value("1".getBytes());
    private static final Text emptyCF = new Text("");
    private static final Text degree = new Text("degree");
    private static final Text rawData = new Text("RawData");

    KeyFactory instance = null;

    String row = "AA";

    String[] fieldNames = {
        "CITY_NAME"
    };

    String[] fieldValues = {
        "Akron"
    };

    // TODO: handle empty field value.

    @Before
    public void setup() {
        instance = new KeyFactory();
        instance.setUnderTest();
    }

    @Test
    public void testGetFieldDelimiter() {
        instance.setFieldDelimiter("A");
        assertEquals("A", instance.getFieldDelimiter());
    }
    
    @Test
    public void testGetFactDelimiter() {
        instance.setFactDelimiter("B");
        assertEquals("B", instance.getFactDelimiter());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGenerateEdge_with_null_row() {
        instance.generateEdges(null, fieldNames, fieldValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateEdge_with_empty_row() {
        instance.generateEdges("", fieldNames, fieldValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateEdge_with_null_field_names() {
        instance.generateEdges(row, null, fieldValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateEdge_with_empty_field_names() {
        String[] emptyFieldNames = {};
        instance.generateEdges(row, emptyFieldNames, fieldValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateEdge_with_null_field_values() {
        instance.generateEdges(row, fieldNames, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateEdge_with_empty_field_values() {
        String[] emptyFieldValues = {};
        instance.generateEdges(row, fieldNames, emptyFieldValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateEdge_with_different_lengths() {
        String[] twoFieldNames = {"first", "last"};
        String[] oneFieldValues = {"jack"};
        instance.generateEdges(row, twoFieldNames, oneFieldValues);
    }

    @Test
    public void testGenerateEdge() {
        Map<Key, Value> actual = instance.generateEdges(row, fieldNames, fieldValues);
        assertEquals("{AA :CITY_NAME|Akron [] 0 false=1}", actual.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateTranspose_with_null_row() {
        instance.generateTranspose(null, fieldNames, fieldValues);
    }

    @Test
    public void testGenerateTranspose() {
        Map<Key, Value> actual = instance.generateTranspose(row, fieldNames, fieldValues);
        assertEquals("{CITY_NAME|Akron :AA [] 0 false=1}", actual.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateDegree_with_null_row() {
        instance.generateDegree(null, fieldNames, fieldValues);
    }

    @Test
    public void testGenerateDegree() {
        Map<Key, Value> actual = instance.generateDegree(row, fieldNames, fieldValues);
        assertEquals("{CITY_NAME|Akron :degree [] 0 false=1}", actual.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateText_with_null_row() {
        instance.generateText(null, fieldNames, fieldValues);
    }

    @Test
    public void testGenerateText() {
        Map<Key, Value> actual = instance.generateText(row, fieldNames, fieldValues);
        assertEquals("{AA :RawData [] 0 false=CITY_NAME|Akron}", actual.toString());
    }

}
