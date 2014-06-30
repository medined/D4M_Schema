package com.codebits.d4m.rest.response;

import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;

public class FieldResponse extends D4MResponse {
    
    @Getter
    private final Map<String, String> fields = new TreeMap<>();

    public void put(final String fieldname, final String cardinality) {
        this.fields.put(fieldname, cardinality);
    }

}
