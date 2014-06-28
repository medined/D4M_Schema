package com.codebits.d4m.rest.controller;

import com.codebits.d4m.rest.model.FieldSetModel;
import com.codebits.d4m.rest.model.RecordModel;
import com.codebits.d4m.rest.service.FieldsetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FieldSetController {

    @Autowired
    FieldsetService fieldsetService = null;
    
    @RequestMapping("/fieldset/create")
    synchronized public RecordModel create(
        @RequestParam(value = "name", required = true) String name, @RequestParam(value = "list", required = true) String list
    ) {
        fieldsetService.put(name, list);
        RecordModel rv = new RecordModel();
        rv.setMessage(String.format("List created [%s].", name));
        return rv;
    }

    @RequestMapping("/fieldset/list")
    public FieldSetModel list() {
        return new FieldSetModel(fieldsetService.getLists());
    }

    @RequestMapping("/fieldset/clear")
    public FieldSetModel clear() {
        fieldsetService.clear();
        return new FieldSetModel(fieldsetService.getLists());
    }

    @RequestMapping("/fieldset/delete")
    synchronized public RecordModel delete(
        @RequestParam(value = "name", required = true) String name
    ) {
        fieldsetService.delete(name);
        RecordModel rv = new RecordModel();
        rv.setMessage(String.format("List deleted [%s].", name));
        return rv;
    }

}
