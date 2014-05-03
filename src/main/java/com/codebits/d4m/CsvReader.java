package com.codebits.d4m;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

public class CsvReader {

    private String filename = null;
    private final List<String> fieldNames = new ArrayList<String>();
    private final List<String[]> records = new ArrayList<String[]>();
    private int recordCount = 0;
    private BufferedReader reader = null;
    private boolean trim = false;

    public void read() {
        Validate.notNull(filename, "filename must not be null");
        Validate.notNull(reader, "reader must not be null");

        String line;
        String cvsSplitBy = ",";

        try {
            boolean headerRead = false;
            while ((line = reader.readLine()) != null) {
                if (headerRead == false) {
                    // first line has field names.
                    String[] components = line.split(cvsSplitBy);
                    for (String component : components) {
                        fieldNames.add(trim ? component.trim() : component);
                    }
                    headerRead = true;
                } else {
                    if (trim) {
                        String[] components = line.split(cvsSplitBy);
                        String[] trimmedComponents = new String[components.length];
                        for (int i = 0; i < components.length; i++) {
                            trimmedComponents[i] = trim ? components[i].trim() : components[i];
                        }
                        records.add(trimmedComponents);
                    } else {
                        records.add(line.split(cvsSplitBy));
                    }
                    recordCount++;
                }
            }

        } catch (FileNotFoundException e) {
            throw new D4MException(String.format("%s not found.", filename), e);
        } catch (IOException e) {
            throw new D4MException(String.format("Unable to read %s.", filename), e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public List<String[]> getRecords() {
        return records;
    }

    public void setTrim() {
        this.trim = true;
    }

}
