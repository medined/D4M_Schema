package com.codebits.d4m.rest.controller;

import com.codebits.d4m.rest.model.FieldSetModel;
import com.codebits.d4m.rest.model.RecordModel;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FieldSetController {
    
    @Getter
    private static final Map lists = new HashMap<>();

    @RequestMapping("/fieldset/create")
    synchronized public RecordModel create(
        @RequestParam(value = "name", required = true) String name
        ,@RequestParam(value = "list", required = true) String list
    ) {
        lists.put(name, list);
        RecordModel rv = new RecordModel();
        rv.setMessage(String.format("List created [%s].", name));
        return rv;
    }
    
    @RequestMapping("/fieldset/list")
    public FieldSetModel list(
    ) {
        return new FieldSetModel(lists);
    }

    @RequestMapping("/fieldset/clear")
    public FieldSetModel clear(
    ) {
        lists.clear();
        return new FieldSetModel(lists);
    }

    @RequestMapping("/fieldset/delete")
    synchronized public RecordModel delete(
        @RequestParam(value = "name", required = true) String name
    ) {
        lists.remove(name);
        RecordModel rv = new RecordModel();
        rv.setMessage(String.format("List deleted [%s].", name));
        return rv;
    }
    
}
