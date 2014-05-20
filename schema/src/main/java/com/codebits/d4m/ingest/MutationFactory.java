package com.codebits.d4m.ingest;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.Setter;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.io.Text;

/*
 * The special field d4msha1 is ignored. It is designed to work with 
 * ETL systems. As source material is read, the ETL system (think
 * Pentaho or Storm) it can add a d4msha1 field which will be used as
 * the Edge table row value.
 *
 * This class does as little transformation as possible to the data. If
 * the underTest flag is set, then a 0 timestamp is used to make unit testing
 * easier. Otherwise, the only transformation is that the d4msha1 field is
 * ignored.
 */
public class MutationFactory {

    protected static final String ROW_VALUE_ERROR = "Please supply a Row value.";
    protected static final String FIELD_NAMES_ERROR = "Please supply Field names.";
    protected static final String FIELD_VALUES_ERROR = "Please supply Field values.";
    protected static final String DIFFERING_LENGTH_ERROR = "Field names and Field Values arrays should have the same length.";

    public Value ONE = null;
    public static final Text EMPTY_CF = new Text("");
    public static final Text DEGREE = new Text("degree");
    public static final Text FIELD = new Text("field");
    public static final Text RAW_DATA = new Text("rawdata");
    public static final Text TEXT = new Text("text");

    @Setter @Getter protected String fieldDelimiter = "\t";
    @Setter @Getter protected String factDelimiter = "|";
    
    protected final Charset charset = Charset.defaultCharset();

    public MutationFactory() {
        ONE = new Value("1".getBytes(charset));
    }
    
    protected void checkParameters(String row, String[] fieldNames, String[] fieldValues) {
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

    public Mutation generateEdges(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateEdges(row, fieldNameArray, fieldValueArray);
    }

    public Mutation generateEdges(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);

        Mutation tEdge = new Mutation(new Text(row));
        for (int nameIndex = 0; nameIndex < fieldNames.length; nameIndex++) {
            if (false == "d4msha1".equals(fieldNames[nameIndex])) {
                Text fact = new Text(fieldNames[nameIndex] + getFactDelimiter() + fieldValues[nameIndex]);
                tEdge.put(EMPTY_CF, fact, ONE);
            }
        }
        return tEdge;
    }

    public List<Mutation> generateTranspose(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateTranspose(row, fieldNameArray, fieldValueArray);
    }

    public List<Mutation> generateTranspose(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);

        List<Mutation> mutations = new ArrayList<Mutation>();
        for (int nameIndex = 0; nameIndex < fieldNames.length; nameIndex++) {
            if (false == "d4msha1".equals(fieldNames[nameIndex])) {
                Text fact = new Text(fieldNames[nameIndex] + getFactDelimiter() + fieldValues[nameIndex]);
                Mutation transpose = new Mutation(new Text(fact));
                transpose.put(EMPTY_CF, new Text(row), ONE);
                mutations.add(transpose);
            }
        }
        return mutations;
    }

    public List<Mutation> generateDegree(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateDegree(row, fieldNameArray, fieldValueArray);
    }

    public List<Mutation> generateDegree(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);

        Map<String, Integer> degrees = new HashMap<String, Integer>();
        for (int i = 0; i < fieldValues.length; i++) {
            if (!fieldValues[i].isEmpty() && !"d4msha1".equals(fieldNames[i])) {
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
            mutation.put(EMPTY_CF, DEGREE, new Value(factCount.toString().getBytes(charset)));
            mutations.add(mutation);
        }
        return mutations;
    }

    public List<Mutation> generateField(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateField(row, fieldNameArray, fieldValueArray);
    }

    public List<Mutation> generateField(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);

        Map<String, Integer> fields = new HashMap<String, Integer>();
        for (int i = 0; i < fieldValues.length; i++) {
            if (!fieldValues[i].isEmpty() && !"d4msha1".equals(fieldNames[i])) {
                Integer fieldCount = fields.get(fieldNames[i]);
                if (fieldCount == null) {
                    fields.put(fieldNames[i], 1);
                } else {
                    fields.put(fieldNames[i], fieldCount++);
                }
            }
        }

        List<Mutation> mutations = new ArrayList<Mutation>();
        for (Entry<String, Integer> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            Integer fieldCount = entry.getValue();
            Mutation mutation = new Mutation(new Text(fieldName));
            mutation.put(EMPTY_CF, FIELD, new Value(fieldCount.toString().getBytes(charset)));
            mutations.add(mutation);
        }
        return mutations;
    }

    public Mutation generateText(String row, String text) {
        Validate.notNull(row, ROW_VALUE_ERROR);
        Validate.notEmpty(row, ROW_VALUE_ERROR);
        Validate.notNull(text, ROW_VALUE_ERROR);
        Validate.notEmpty(text, ROW_VALUE_ERROR);

        Mutation mutation = new Mutation(new Text(row));
        mutation.put(EMPTY_CF, TEXT, new Value(text.getBytes(charset)));
        return mutation;
    }

    public Mutation generateRawData(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);

        boolean addFieldDelimiter = false;
        StringBuilder value = new StringBuilder();
        for (int nameIndex = 0; nameIndex < fieldNames.length; nameIndex++) {
             if (false == "d4msha1".equals(fieldNames[nameIndex])) {
               Text fact = new Text(fieldNames[nameIndex] + getFactDelimiter() + fieldValues[nameIndex]);
                if (addFieldDelimiter) {
                    value.append(getFieldDelimiter());
                } else {
                    addFieldDelimiter = true;
                }
                value.append(fact);
            }
        }
        Mutation mutation = new Mutation(new Text(row));
        mutation.put(EMPTY_CF, RAW_DATA, new Value(value.toString().getBytes(charset)));
        return mutation;
    }

}
