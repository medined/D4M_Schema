package com.codebits.d4m.ingest;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.io.Text;

/*
 * This class does as little transformation as possible to the data. If
 * the underTest flag is set, then a 0 timestamp is used to make unit testing
 * easier. Otherwise, the only transformation is that the d4msha1 field is
 * ignored.
 */
public class KeyFactory {

    private static final String ROW_VALUE_ERROR = "Please supply a Row value.";
    private static final String FIELD_NAMES_ERROR = "Please supply Field names.";
    private static final String FIELD_VALUES_ERROR = "Please supply Field values.";
    private static final String DIFFERING_LENGTH_ERROR = "Field names and Field Values arrays should have the same length.";

    private static final Value one = new Value("1".getBytes());
    private static final Text emptyCF = new Text("");
    private static final Text degree = new Text("degree");
    private static final Text field = new Text("field");
    private static final Text rawData = new Text("RawData");

    private String fieldDelimiter = "\t";
    private String factDelimiter = "|";

    private Key key = null;
    private boolean underTest = false;

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

    public Map<Key, Value> generateEdges(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);
        Map<Key, Value> entries = new TreeMap<Key, Value>();
        Text tRow = new Text(row);
        for (int nameIndex = 0; nameIndex < fieldNames.length; nameIndex++) {

            if (false == "d4msha1".equals(fieldNames[nameIndex])) {
                Text fact = new Text(fieldNames[nameIndex] + getFactDelimiter() + fieldValues[nameIndex]);
                if (underTest) {
                    key = new Key(tRow, emptyCF, fact, 0);
                } else {
                    key = new Key(tRow, emptyCF, fact);
                }
                entries.put(key, one);
            }
        }
        return entries;
    }

    public Map<Key, Value> generateTranspose(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);
        Map<Key, Value> entries = new TreeMap<Key, Value>();
        for (int nameIndex = 0; nameIndex < fieldNames.length; nameIndex++) {

            if (false == "d4msha1".equals(fieldNames[nameIndex])) {
                Text fact = new Text(fieldNames[nameIndex] + getFactDelimiter() + fieldValues[nameIndex]);
                if (underTest) {
                    key = new Key(new Text(fact), emptyCF, new Text(row), 0);
                } else {
                    key = new Key(new Text(fact), emptyCF, new Text(row));
                }
                entries.put(key, one);
            }
        }
        return entries;
    }

    public Map<Key, Value> generateDegree(String row, String[] fieldNames, String[] fieldValues) {
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

        Map<Key, Value> entries = new TreeMap<Key, Value>();
        for (Entry<String, Integer> entry : degrees.entrySet()) {
            String fact = entry.getKey();
            Integer factCount = entry.getValue();
            if (underTest) {
                key = new Key(new Text(fact), emptyCF, degree, 0);
            } else {
                key = new Key(new Text(fact), emptyCF, degree);
            }
            entries.put(key, new Value(factCount.toString().getBytes()));
        }
        return entries;
    }

    public Map<Key, Value> generateField(String row, String[] fieldNames, String[] fieldValues) {
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

        Map<Key, Value> entries = new TreeMap<Key, Value>();
        for (Entry<String, Integer> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            Integer fieldCount = entry.getValue();
            if (underTest) {
                key = new Key(new Text(fieldName), emptyCF, field, 0);
            } else {
                key = new Key(new Text(fieldName), emptyCF, field);
            }
            entries.put(key, new Value(fieldCount.toString().getBytes()));
        }
        return entries;
    }

    public Map<Key, Value> generateText(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);
        Map<Key, Value> entries = new TreeMap<Key, Value>();
        Text tRow = new Text(row);

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
        if (underTest) {
            key = new Key(tRow, emptyCF, rawData, 0);
        } else {
            key = new Key(tRow, emptyCF, rawData);
        }
        entries.put(key, new Value(value.toString().getBytes()));
        return entries;
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

    public void setUnderTest() {
        this.underTest = true;
    }

}
