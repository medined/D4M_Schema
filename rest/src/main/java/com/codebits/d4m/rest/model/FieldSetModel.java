package com.codebits.d4m.rest.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class FieldSetModel extends D4MResponse {

    @Getter
    private final Map lists = new HashMap<>();

    public FieldSetModel(Map lists) {
        this.lists.putAll(lists);
    }

}
