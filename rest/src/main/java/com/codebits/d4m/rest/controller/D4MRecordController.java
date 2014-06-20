package com.codebits.d4m.rest.controller;

import com.codebits.d4m.TableManager;
import com.codebits.d4m.rest.model.D4MRecord;
import com.codebits.d4m.rest.service.AccumuloService;
import java.util.Iterator;
import java.util.Map;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class D4MRecordController {
    
    @Autowired
    private AccumuloService accumuloService = null;

    @RequestMapping("/record")
    public D4MRecord fetchRow(
        @RequestParam(value = "baseTableName", required = false, defaultValue = "edge") String baseTableName
        ,@RequestParam(value = "row", required = true) String row
    ) {
        D4MRecord rv = new D4MRecord();
        
        TableManager tableManager = new TableManager(accumuloService.getConnector(), accumuloService.getTableOperations());
        tableManager.setBaseTableName(baseTableName);
        
        Connector connector = accumuloService.getConnector();

        final String tableName = tableManager.getEdgeTable();

        Scanner scanner = null;
        try {
            scanner = connector.createScanner(tableName, new Authorizations());
        } catch (TableNotFoundException e) {
            throw new RuntimeException(String.format("Error getting scanning table [%s].", tableName), e);
        }

        scanner.setRange(Range.exact(new Text(row)));

        Iterator<Map.Entry<Key, Value>> iterator = scanner.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();
            Key key = entry.getKey();
            rv.add(key);
        }
        
        scanner.close();
        return rv;
    }

    public void setAccumuloService(AccumuloService accumuloService) {
        this.accumuloService = accumuloService;
    }
}
