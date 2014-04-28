package org.cbda.ingest;

import java.util.ArrayList;
import java.util.List;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import org.cbda.TestableMutation;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class MutationFactoryTest {

    private static final Value one = new Value("1".getBytes());
    private static final Text emptyCF = new Text("");
    private static final Text degree = new Text("Degree");
    private static final Text rawData = new Text("RawData");

    MutationFactory instance = null;

    String row = "AA";

    String[] fieldNames = {
        "CITY_NAME"
    };

    String[] fieldValues = {
        "Akron"
    };

    @Before
    public void setup() {
        instance = new MutationFactory();
    }
    
    // TODO: handle empty field value.

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
        expectedEdges.put(new Text("CITY_NAME|Akron"), emptyCF, one);

        Mutation actual = instance.generateEdges(row, fieldNames, fieldValues);
        assertEquals(expectedEdges, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateTranspose_with_null_row() {
        instance.generateTranspose(null, fieldNames, fieldValues);
    }

    @Test
    public void testGenerateTranspose() {
        TestableMutation transpose = new TestableMutation("CITY_NAME|Akron");
        transpose.put(new Text("AA"), emptyCF, one);
        
        List<Mutation> expected = new ArrayList<Mutation>();
        expected.add(transpose);

        List<Mutation> actual = instance.generateTranspose(row, fieldNames, fieldValues);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateDegree_with_null_row() {
        instance.generateDegree(null, fieldNames, fieldValues);
    }

    @Test
    public void testGenerateDegree() {
        TestableMutation mutation = new TestableMutation("CITY_NAME|Akron");
        mutation.put(degree, emptyCF, one);
        
        List<Mutation> expected = new ArrayList<Mutation>();
        expected.add(mutation);

        List<Mutation> actual = instance.generateDegree(row, fieldNames, fieldValues);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateText_with_null_row() {
        instance.generateText(null, fieldNames, fieldValues);
    }

    @Test
    public void testGenerateText() {
        TestableMutation expected = new TestableMutation("AA");
        expected.put(rawData, emptyCF, new Value("CITY_NAME|Akron".getBytes()));
        
        Mutation actual = instance.generateText(row, fieldNames, fieldValues);
        assertEquals(expected, actual);
    }

}
