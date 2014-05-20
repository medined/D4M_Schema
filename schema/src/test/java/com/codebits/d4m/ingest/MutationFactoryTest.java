package com.codebits.d4m.ingest;

import java.util.ArrayList;
import java.util.List;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import com.codebits.d4m.TestableMutation;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class MutationFactoryTest {

    private static final Value one = new Value("1".getBytes());
    private static final Text emptyCF = new Text("");
    private static final Text degree = new Text("degree");
    private static final Text field = new Text("field");
    private static final Text rawData = new Text("rawdata");

    MutationFactory instance = null;

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
        instance = new MutationFactory("\t", "|");
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
        TestableMutation expectedEdges = new TestableMutation("AA");
        expectedEdges.put(emptyCF, new Text("CITY_NAME|Akron"), one);

        Mutation actual = instance.generateEdges(row, fieldNames, fieldValues);
        assertEquals(expectedEdges, actual);
    }

    @Test
    public void testGenerateEdge_does_not_passthru_d4msha1_field() {
        TestableMutation expectedEdges = new TestableMutation("AA");
        expectedEdges.put(emptyCF, new Text("CITY_NAME|Akron"), one);

        Mutation actual = instance.generateEdges(row, xfieldNames, xfieldValues);
        assertEquals(expectedEdges, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateTranspose_with_null_row() {
        instance.generateTranspose(null, fieldNames, fieldValues);
    }

    @Test
    public void testGenerateTranspose() {
        TestableMutation transpose = new TestableMutation("CITY_NAME|Akron");
        transpose.put(emptyCF, new Text("AA"), one);

        List<Mutation> expected = new ArrayList<Mutation>();
        expected.add(transpose);

        List<Mutation> actual = instance.generateTranspose(row, fieldNames, fieldValues);
        assertEquals(expected, actual);
    }

    @Test
    public void testGenerateTranspose_does_not_passthru_d4msha1_field() {
        TestableMutation transpose = new TestableMutation("CITY_NAME|Akron");
        transpose.put(emptyCF, new Text("AA"), one);

        List<Mutation> expected = new ArrayList<Mutation>();
        expected.add(transpose);

        List<Mutation> actual = instance.generateTranspose(row, xfieldNames, xfieldValues);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateDegree_with_null_row() {
        instance.generateDegree(null, fieldNames, fieldValues);
    }

    @Test
    public void testGenerateDegree() {
        TestableMutation mutation = new TestableMutation("CITY_NAME|Akron");
        mutation.put(emptyCF, degree, one);

        List<Mutation> expected = new ArrayList<Mutation>();
        expected.add(mutation);

        List<Mutation> actual = instance.generateDegree(row, fieldNames, fieldValues);
        assertEquals(expected, actual);
    }

    @Test
    public void testGenerateField() {
        TestableMutation mutation = new TestableMutation(field);
        mutation.put(field, new Text("CITY_NAME"), one);

        List<Mutation> expected = new ArrayList<Mutation>();
        expected.add(mutation);

        List<Mutation> actual = instance.generateField(row, fieldNames, fieldValues);
        assertEquals(expected, actual);
    }

    @Test
    public void testGenerateDegree_does_not_passthru_d4msha1_field() {
        TestableMutation mutation = new TestableMutation("CITY_NAME|Akron");
        mutation.put(emptyCF, degree, one);

        List<Mutation> expected = new ArrayList<Mutation>();
        expected.add(mutation);

        List<Mutation> actual = instance.generateDegree(row, xfieldNames, xfieldValues);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateText_with_null_row() {
        instance.generateRawData(null, fieldNames, fieldValues);
    }

    @Test
    public void testGenerateRawData() {
        TestableMutation expected = new TestableMutation("AA");
        expected.put(emptyCF, rawData, new Value("CITY_NAME|Akron".getBytes()));

        Mutation actual = instance.generateRawData(row, fieldNames, fieldValues);
        assertEquals(expected, actual);
    }

    @Test
    public void testGenerateRawData_does_not_passthru_d4msha1_field() {
        TestableMutation expected = new TestableMutation("AA");
        expected.put(emptyCF, rawData, new Value("CITY_NAME|Akron".getBytes()));

        Mutation actual = instance.generateRawData(row, xfieldNames, xfieldValues);
        expected.equals(actual);
        System.out.println(expected.getDifferences());
        assertEquals(expected, actual);
    }

}
