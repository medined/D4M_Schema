package com.codebits.d4m.ingest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.io.Text;

public class MutationFactory {

    private static final String ROW_VALUE_ERROR = "Please supply a Row value.";
    private static final String FIELD_NAMES_ERROR = "Please supply Field names.";
    private static final String FIELD_VALUES_ERROR = "Please supply Field values.";
    private static final String DIFFERING_LENGTH_ERROR = "Field names and Field Values arrays should have the same length.";
    
    private static final Value one = new Value("1".getBytes());
    private static final Text emptyCF= new Text("");
    private static final Text emptyCQ = new Text("");
    private static final Text degree = new Text("degree");
    private static final Text rawData = new Text("RawData");
    
    private String fieldDelimiter = "\t";
    private String factDelimiter = "|";

    private void checkParameters(String row, String[] fieldNames, String[] fieldValues) {
        Validate.notNull(row, ROW_VALUE_ERROR);
        Validate.notEmpty(row, ROW_VALUE_ERROR);

        Validate.notNull(fieldNames, FIELD_NAMES_ERROR);
        Validate.notEmpty(fieldNames, FIELD_NAMES_ERROR);
        
        Validate.notNull(fieldValues, FIELD_VALUES_ERROR);
        Validate.notEmpty(fieldValues, FIELD_VALUES_ERROR);
        
        if (fieldNames.length != fieldValues.length) {
            throw new IllegalArgumentException(DIFFERING_LENGTH_ERROR);
        }
    }
    
    public Mutation generateEdges(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);
        
        Mutation tEdge = new Mutation(new Text(row));
        for (int nameIndex = 0; nameIndex < fieldNames.length; nameIndex++) {
            Text fact = new Text(fieldNames[nameIndex] + getFactDelimiter() + fieldValues[nameIndex]);
            tEdge.put(fact, emptyCQ, one);
        }
        return tEdge;
    }

    public List<Mutation> generateTranspose(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);
        
        List<Mutation> mutations = new ArrayList<Mutation>();
        for (int nameIndex = 0; nameIndex < fieldNames.length; nameIndex++) {
            Text fact = new Text(fieldNames[nameIndex] + getFactDelimiter() + fieldValues[nameIndex]);
            Mutation transpose = new Mutation(new Text(fact));
            transpose.put(new Text(row), emptyCQ, one);
            mutations.add(transpose);
        }
        return mutations;
    }

    public List<Mutation> generateDegree(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);
        
        Map<String, Integer> degrees = new HashMap<String, Integer>();
        for (int i = 0; i < fieldValues.length; i++) {
            if (! fieldValues[i].isEmpty()) {
                String fact = String.format("%s%s%s", fieldNames[i], getFactDelimiter(), fieldValues[i]);
                Integer factCount = degrees.get(fact);
                if (factCount == null) {
                    degrees.put(fact, 1);
                } else {
                    degrees.put(fact, factCount++);
                }
            }
        }

        List<Mutation> mutations = new ArrayList<Mutation>();
        for (Entry<String, Integer> entry : degrees.entrySet()) {
            String fact = entry.getKey();
            Integer factCount = entry.getValue();
            Mutation mutation = new Mutation(new Text(fact));
            mutation.put(emptyCF, degree, new Value(factCount.toString().getBytes()));
            mutations.add(mutation);
        }
        return mutations;
    }
    
    public Mutation generateText(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);
        
        StringBuilder value = new StringBuilder();
        for (int nameIndex = 0; nameIndex < fieldNames.length; nameIndex++) {
            Text fact = new Text(fieldNames[nameIndex] + getFactDelimiter() + fieldValues[nameIndex]);
            if (nameIndex > 0) {
                value.append(getFieldDelimiter());
            }
            value.append(fact);
        }
        Mutation mutation = new Mutation(new Text(row));
        mutation.put(rawData, emptyCQ, new Value(value.toString().getBytes()));
        return mutation;
    }

    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public String getFactDelimiter() {
        return factDelimiter;
    }

    public void setFactDelimiter(String factDelimiter) {
        this.factDelimiter = factDelimiter;
    }

}
