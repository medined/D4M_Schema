package com.codebits.d4m.rest.controller;

import com.codebits.d4m.TableManager;
import com.codebits.d4m.rest.model.D4MResponse;
import com.codebits.d4m.rest.service.AccumuloService;
import lombok.Setter;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TableController {

    @Setter
    @Autowired
    private AccumuloService accumuloService = null;

    @RequestMapping("/tables-create")
    public D4MResponse create(
        @RequestParam(value = "baseTableName", required = false, defaultValue = "edge") String baseTableName, @RequestParam(value = "addSplitsForSha1", required = false, defaultValue = "false") boolean addSplitsForSha1
    ) {
        D4MResponse rv = new D4MResponse();
        try {
            TableManager tableManager = new TableManager(accumuloService.getConnector(), accumuloService.getTableOperations());
            tableManager.setBaseTableName(baseTableName);
            tableManager.createTables();
            if (addSplitsForSha1) {
                tableManager.addSplitsForSha1();
            }
            rv.setMessage("tables created.");
        } catch (Exception e) {
            rv.setMessage(e.getMessage());
            rv.setThrowable(e);
        }
        return rv;
    }

    @RequestMapping("/tables-delete")
    public D4MResponse delete(
        @RequestParam(value = "baseTableName", required = false, defaultValue = "edge") String baseTableName
    ) {
        D4MResponse rv = new D4MResponse();
        try {
            TableManager tableManager = new TableManager(accumuloService.getConnector(), accumuloService.getTableOperations());
            tableManager.setBaseTableName(baseTableName);
            tableManager.deleteTables();
            rv.setMessage("tables deleted.");
        } catch (Exception e) {
            rv.setMessage(e.getMessage());
            rv.setThrowable(e);
        }
        return rv;
    }
}
