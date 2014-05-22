package com.codebits.d4m.ingest;

import com.clearspring.analytics.hash.MurmurHash;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;
import com.clearspring.analytics.stream.cardinality.ICardinality;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public static final Text CARDINALITY = new Text("cardinality");
    public static final Text CARDINALITY_BYTES = new Text("cardinality_bytes");
    public static final Text EMPTY_CF = new Text("");
    public static final Text DEGREE = new Text("degree");
    public static final Text FIELD = new Text("field");
    public static final Text RAW_DATA = new Text("rawdata");
    public static final Text TEXT = new Text("text");

    @Setter @Getter protected String fieldDelimiter;
    @Setter @Getter protected String factDelimiter;
    
    @Setter @Getter private boolean trackCardinality = false;

    Map<String, ICardinality> estimators = new TreeMap<String, ICardinality>();
    
    protected final Charset charset = Charset.defaultCharset();

    public MutationFactory(final String fieldDelimiter, final String factDelimiter) {
        ONE = new Value("1".getBytes(charset));
        this.fieldDelimiter = fieldDelimiter;
        this.factDelimiter = factDelimiter;
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
                String fieldName = fieldNames[nameIndex];
                String fieldInfo = fieldName + getFactDelimiter() + fieldValues[nameIndex];
                Text fact = new Text(fieldInfo);
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

    public List<Mutation> generateMetadata(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateMetadata(row, fieldNameArray, fieldValueArray);
    }

    public List<Mutation> generateMetadata(String row, String[] fieldNames, String[] fieldValues) {
        checkParameters(row, fieldNames, fieldValues);

        Map<String, Integer> fields = new HashMap<String, Integer>();
        for (int i = 0; i < fieldValues.length; i++) {
            String fieldName = fieldNames[i];
            String fieldInfo = fieldName + getFactDelimiter() + fieldValues[i];
            if (!fieldName.isEmpty() && !"d4msha1".equals(fieldName)) {
                Integer fieldCount = fields.get(fieldName);
                if (fieldCount == null) {
                    fields.put(fieldName, 1);
                } else {
                    fields.put(fieldName, fieldCount++);
                }
                if (trackCardinality) {
                    long hashCode = MurmurHash.hash64(fieldInfo);
                    ICardinality estimator = estimators.get(fieldName);
                    if (estimator == null) {
                        estimator = new HyperLogLogPlus(16);
                        estimators.put(fieldName, estimator);
                    }
                    estimator.offer(hashCode);
                }
            }
        }

        List<Mutation> mutations = new ArrayList<Mutation>();
        Mutation mutation = new Mutation(FIELD);
        for (Entry<String, Integer> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            Integer fieldCount = entry.getValue();
            mutation.put(FIELD, new Text(fieldName), new Value(fieldCount.toString().getBytes(charset)));
        }
        for (Entry<String, ICardinality> entry : estimators.entrySet()) {
            Text factName = new Text(entry.getKey());
            ICardinality estimator = entry.getValue();
            Value cardinalityBytes;
            try {
                cardinalityBytes = new Value(estimator.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Value cardinalityEstimate = new Value(Long.toString(estimator.cardinality()).getBytes(charset));
            mutation.put(CARDINALITY, factName, cardinalityEstimate);
            mutation.put(CARDINALITY_BYTES, factName, cardinalityBytes);
        }
        mutations.add(mutation);
        return mutations;
    }

    public Mutation generateRawData(String row, List<String> fieldNames, List<String> fieldValues) {
        String[] fieldNameArray = fieldNames.toArray(new String[fieldNames.size()]);
        String[] fieldValueArray = fieldValues.toArray(new String[fieldValues.size()]);
        return generateRawData(row, fieldNameArray, fieldValueArray);
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

    public Mutation generateText(String row, String text) {
        Validate.notNull(row, ROW_VALUE_ERROR);
        Validate.notEmpty(row, ROW_VALUE_ERROR);
        Validate.notNull(text, ROW_VALUE_ERROR);
        Validate.notEmpty(text, ROW_VALUE_ERROR);

        Mutation mutation = new Mutation(new Text(row));
        mutation.put(EMPTY_CF, TEXT, new Value(text.getBytes(charset)));
        return mutation;
    }

}
