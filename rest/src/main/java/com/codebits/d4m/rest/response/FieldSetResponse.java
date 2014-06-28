package com.codebits.d4m.rest.response;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class FieldSetResponse extends D4MResponse {

    @Getter
    private final Map lists = new HashMap<>();

    public FieldSetResponse(Map lists) {
        this.lists.putAll(lists);
    }

}
