package com.codebits.d4m.ingest;

import java.util.Map;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class KeyFactoryTest {

    KeyFactory instance = null;

    String row = "AA";

    String[] fieldNames = {
        "CITY_NAME"
    };

    String[] fieldValues = {
        "Akron"
    };

    String[] xfieldNames = {
        "d4msha1",
        "CITY_NAME"
    };

    String[] xfieldValues = {
        "ShortWrongSHA1",
        "Akron"
    };

    // TODO: handle empty field value.
    @Before
    public void setup() {
        instance = new KeyFactory();
        instance.setMutationFactory(new MutationFactory());
        instance.setUnderTest(true);
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

    @Test
    public void testGenerateEdge_does_not_passthru_d4msha1_field() {

        Map<Key, Value> actual = instance.generateEdges(row, xfieldNames, xfieldValues);
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

    @Test
    public void testGenerateTranspose_does_not_passthru_d4msha1_field() {
        Map<Key, Value> actual = instance.generateTranspose(row, xfieldNames, xfieldValues);
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

    @Test
    public void testGenerateField() {
        Map<Key, Value> actual = instance.generateField(row, fieldNames, fieldValues);
        assertEquals("{CITY_NAME :field [] 0 false=1}", actual.toString());
    }

    @Test
    public void testGenerateDegree_does_not_passthru_d4msha1_field() {
        Map<Key, Value> actual = instance.generateDegree(row, xfieldNames, xfieldValues);
        assertEquals("{CITY_NAME|Akron :degree [] 0 false=1}", actual.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateText_with_null_row() {
        instance.generateRawData(null, fieldNames, fieldValues);
    }

    @Test
    public void testGenerateText() {
        Map<Key, Value> actual = instance.generateRawData(row, fieldNames, fieldValues);
        assertEquals("{AA :rawdata [] 0 false=CITY_NAME|Akron}", actual.toString());
    }

    @Test
    public void testGenerateText_does_not_passthru_d4msha1_field() {
        Map<Key, Value> actual = instance.generateRawData(row, xfieldNames, xfieldValues);
        assertEquals("{AA :rawdata [] 0 false=CITY_NAME|Akron}", actual.toString());
    }

}
