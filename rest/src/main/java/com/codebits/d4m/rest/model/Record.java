package com.codebits.d4m.rest.model;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import org.apache.accumulo.core.data.Key;

public class Record extends D4MResponse {

    @Getter
    private final Map<String, String> fields = new TreeMap<>();

    protected final Charset charset = Charset.defaultCharset();
    private final String factDelimiter = "|";

    public void add(final Key key) {
        String factInfo = new String(key.getColumnQualifier().getBytes(), charset);
        int factDelimitorPosition = factInfo.indexOf(factDelimiter);
        String name = factInfo.substring(0, factDelimitorPosition);
        String value = factInfo.substring(factDelimitorPosition);
        fields.put(name, value);
    }

}
