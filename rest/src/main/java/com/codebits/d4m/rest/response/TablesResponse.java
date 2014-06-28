package com.codebits.d4m.rest.response;

import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;

public class TablesResponse extends D4MResponse {

    @Setter
    @Getter
    private Map<String, String> tables = new TreeMap<>();
}
