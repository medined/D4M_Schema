package com.codebits.d4m.rest.controller;

import com.codebits.d4m.rest.response.D4MResponse;
import com.codebits.d4m.rest.response.FieldSetResponse;
import com.codebits.d4m.rest.response.RecordResponse;
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
    synchronized public D4MResponse create(
        @RequestParam(value = "name", required = true) String name, @RequestParam(value = "list", required = true) String list
    ) {
        fieldsetService.put(name, list);
        return new D4MResponse(String.format("Fieldset created [%s].", name));
    }

    @RequestMapping("/fieldset/list")
    public FieldSetResponse list() {
        return new FieldSetResponse(fieldsetService.getLists());
    }

    @RequestMapping("/fieldset/clear")
    public FieldSetResponse clear() {
        fieldsetService.clear();
        return new FieldSetResponse(fieldsetService.getLists());
    }

    @RequestMapping("/fieldset/delete")
    synchronized public D4MResponse delete(
        @RequestParam(value = "name", required = true) String name
    ) {
        fieldsetService.delete(name);
        return new D4MResponse(String.format("Fieldset deleted [%s].", name));
    }

}
