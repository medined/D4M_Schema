package com.codebits.d4m;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

public class CsvReader {

    @Setter private String filename = null;
    @Getter private final List<String> fieldNames = new ArrayList<String>();
    @Getter private final List<List<String>> records = new ArrayList<List<String>>();
    @Getter private int recordCount = 0;
    @Setter private BufferedReader reader = null;
    @Setter private boolean trim = false;
    @Setter private boolean sha1 = false;
    @Setter private boolean lowercaseFieldnames = false;

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

}
