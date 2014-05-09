package com.codebits.d4m;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

public class CsvReader {

    private String filename = null;
    private final List<String> fieldNames = new ArrayList<String>();
    private final List<List<String>> records = new ArrayList<List<String>>();
    private int recordCount = 0;
    private BufferedReader reader = null;
    private boolean trim = false;
    private boolean sha1 = false;
    private boolean lowercaseFieldnames = false;

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
                    if (sha1) {
                        fieldNames.add("d4msha1");
                    }
                    for (String component : components) {
                        String fieldName = component;
                        if (trim) {
                            fieldName = fieldName.trim();
                        }
                        if (lowercaseFieldnames) {
                            fieldName = fieldName.toLowerCase();
                        }
                        fieldNames.add(fieldName);
                    }
                    headerRead = true;
                } else {
                    List<String> fields = new ArrayList<String>();
                    if (sha1) {
                        fields.add(DigestUtils.sha1Hex(line));
                    }
                    for (String component : line.split(cvsSplitBy)) {
                        fields.add(trim ? component.trim() : component);
                    }
                    records.add(fields);
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

    public List<List<String>> getRecords() {
        return records;
    }

    public void setTrim() {
        this.trim = true;
    }

    public void setSha1() {
        this.sha1 = true;
    }

    public void setLowercaseFieldnames() {
        this.lowercaseFieldnames = true;
    }

}
