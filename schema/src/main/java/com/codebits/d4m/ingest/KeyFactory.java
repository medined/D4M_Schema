package com.codebits.d4m.ingest;

import static com.codebits.d4m.ingest.MutationFactory.DEGREE;
import static com.codebits.d4m.ingest.MutationFactory.EMPTY_CF;
import static com.codebits.d4m.ingest.MutationFactory.FIELD;
import static com.codebits.d4m.ingest.MutationFactory.RAW_DATA;
import static com.codebits.d4m.ingest.MutationFactory.TEXT;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.io.Text;

/*
 * This class wraps a MutationFactory transforming the Mutation to Keys and Values
 *
 * This class does as little transformation as possible to the data. If
 * the underTest flag is set, then a 0 timestamp is used to make unit testing
 * easier. Otherwise, the only transformation is that the d4msha1 field is
 * ignored.
 */
public class KeyFactory {

    protected static final String MUTATION_FACTORY_VALUE_ERROR = "Please supply a Mutation Factory.";

    @Setter
    @Getter
    MutationFactory mutationFactory = null;

    private Key key = null;
    @Setter
    private boolean underTest = false;

    public Map<Key, Value> generateEdges(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateEdges(row, fieldNameArray, fieldValueArray);
    }

    public Map<Key, Value> generateEdges(String row, String[] fieldNames, String[] fieldValues) {
        Validate.notNull(mutationFactory, MUTATION_FACTORY_VALUE_ERROR);
        Map<Key, Value> entries = new TreeMap<Key, Value>();

        Mutation mutation = mutationFactory.generateEdges(row, fieldNames, fieldValues);
        Text tRow = new Text(mutation.getRow());
        for (ColumnUpdate columnUpdate : mutation.getUpdates()) {
            Text fact = new Text(columnUpdate.getColumnQualifier());
            if (underTest) {
                key = new Key(tRow, EMPTY_CF, fact, 0);
            } else {
                key = new Key(tRow, EMPTY_CF, fact);
            }
            entries.put(key, mutationFactory.ONE);
        }
        return entries;
    }

    public Map<Key, Value> generateTranspose(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateTranspose(row, fieldNameArray, fieldValueArray);
    }

    public Map<Key, Value> generateTranspose(String row, String[] fieldNames, String[] fieldValues) {
        Validate.notNull(mutationFactory, MUTATION_FACTORY_VALUE_ERROR);
        Map<Key, Value> entries = new TreeMap<Key, Value>();

        for (Mutation mutation : mutationFactory.generateTranspose(row, fieldNames, fieldValues)) {
            Text fact = new Text(mutation.getRow());
            for (ColumnUpdate columnUpdate : mutation.getUpdates()) {
                if (underTest) {
                    key = new Key(fact, EMPTY_CF, new Text(columnUpdate.getColumnQualifier()), 0);
                } else {
                    key = new Key(fact, EMPTY_CF, new Text(columnUpdate.getColumnQualifier()));
                }
                entries.put(key, mutationFactory.ONE);
            }
        }

        return entries;
    }

    public Map<Key, Value> generateDegree(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateDegree(row, fieldNameArray, fieldValueArray);
    }

    public Map<Key, Value> generateDegree(String row, String[] fieldNames, String[] fieldValues) {
        Validate.notNull(mutationFactory, MUTATION_FACTORY_VALUE_ERROR);
        Map<Key, Value> entries = new TreeMap<Key, Value>();

        for (Mutation mutation : mutationFactory.generateDegree(row, fieldNames, fieldValues)) {
            Text fact = new Text(mutation.getRow());
            for (ColumnUpdate columnUpdate : mutation.getUpdates()) {
                if (underTest) {
                    key = new Key(fact, EMPTY_CF, DEGREE, 0);
                } else {
                    key = new Key(fact, EMPTY_CF, DEGREE);
                }
                entries.put(key, new Value(columnUpdate.getValue()));
            }

        }

        return entries;
    }

    public Map<Key, Value> generateField(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateField(row, fieldNameArray, fieldValueArray);
    }

    public Map<Key, Value> generateField(String row, String[] fieldNames, String[] fieldValues) {
        Validate.notNull(mutationFactory, MUTATION_FACTORY_VALUE_ERROR);
        Map<Key, Value> entries = new TreeMap<Key, Value>();

        for (Mutation mutation : mutationFactory.generateMetadata(row, fieldNames, fieldValues)) {
            for (ColumnUpdate columnUpdate : mutation.getUpdates()) {
                if (underTest) {
                    key = new Key(FIELD, FIELD, new Text(columnUpdate.getColumnQualifier()), 0);
                } else {
                    key = new Key(FIELD, FIELD, new Text(columnUpdate.getColumnQualifier()));
                }
                entries.put(key, new Value(columnUpdate.getValue()));
            }
        }
        return entries;
    }

    public Map<Key, Value> generateRawData(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateRawData(row, fieldNameArray, fieldValueArray);
    }
    
    public Map<Key, Value> generateRawData(String row, String[] fieldNames, String[] fieldValues) {
        Validate.notNull(mutationFactory, MUTATION_FACTORY_VALUE_ERROR);
        Map<Key, Value> entries = new TreeMap<Key, Value>();
        Mutation mutation = mutationFactory.generateRawData(row, fieldNames, fieldValues);
        Text tRow = new Text(mutation.getRow());
        for (ColumnUpdate columnUpdate : mutation.getUpdates()) {
            if (underTest) {
                key = new Key(tRow, EMPTY_CF, RAW_DATA, 0);
            } else {
                key = new Key(tRow, EMPTY_CF, RAW_DATA);
            }
            entries.put(key, new Value(columnUpdate.getValue()));
        }
        return entries;
    }

    public Map<Key, Value> generateText(String row, String text) {
        Validate.notNull(mutationFactory, MUTATION_FACTORY_VALUE_ERROR);
        Map<Key, Value> entries = new TreeMap<Key, Value>();
        Mutation mutation = mutationFactory.generateText(row, text);
        Text tRow = new Text(mutation.getRow());
        for (ColumnUpdate columnUpdate : mutation.getUpdates()) {
            if (underTest) {
                key = new Key(tRow, EMPTY_CF, TEXT, 0);
            } else {
                key = new Key(tRow, EMPTY_CF, TEXT);
            }
            entries.put(key, new Value(columnUpdate.getValue()));
        }
        return entries;
    }
    
}
