package com.codebits.d4m.rest.controller;

import com.codebits.d4m.D4MException;
import com.codebits.d4m.TableManager;
import com.codebits.d4m.rest.model.Record;
import com.codebits.d4m.rest.service.AccumuloService;
import java.util.Iterator;
import java.util.Map;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecordController {

    @Autowired
    private AccumuloService accumuloService = null;

    @RequestMapping("/record")
    public Record fetchRow(
        @RequestParam(value = "baseTableName", required = false, defaultValue = "edge") String baseTableName
        ,@RequestParam(value = "row", required = true) String row
        ,@RequestParam(value = "user", required = true) String user
        ,@RequestParam(value = "password", required = true) String password
    ) {
        Record rv = new Record();
        Scanner scanner = null;

        try {
            Connector connector = accumuloService.getConnector(user, password);
            TableManager tableManager = new TableManager(connector);
            tableManager.setBaseTableName(baseTableName);

            final String tableName = tableManager.getEdgeTable();

            try {
                scanner = connector.createScanner(tableName, new Authorizations());
                scanner.setRange(Range.exact(new Text(row)));

                Iterator<Map.Entry<Key, Value>> iterator = scanner.iterator();
                boolean entryFound = false;
                while (iterator.hasNext()) {
                    entryFound = true;
                    Map.Entry<Key, Value> entry = iterator.next();
                    Key key = entry.getKey();
                    rv.add(key);
                }
                if (not(entryFound)) {
                    rv.setMessage(String.format("Unknown record [%s].", row));
                }
            } catch (TableNotFoundException e) {
                rv.setMessage(String.format("Unknown table [%s].", tableName));
                rv.setThrowable(e);
            }

        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return rv;
    }

    public void setAccumuloService(AccumuloService accumuloService) {
        this.accumuloService = accumuloService;
    }
    
    private boolean not(boolean b) {
        return !b;
    }
}
