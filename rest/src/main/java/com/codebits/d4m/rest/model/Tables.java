package com.codebits.d4m.rest.model;

import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;

public class Tables extends D4MResponse {

    @Setter
    @Getter
    private Map<String, String> tables = new TreeMap<>();
}
