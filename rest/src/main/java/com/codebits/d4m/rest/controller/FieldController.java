package com.codebits.d4m.rest.controller;

import com.codebits.d4m.TableManager;
import com.codebits.d4m.rest.response.FieldResponse;
import com.codebits.d4m.rest.service.AccumuloService;
import java.util.Iterator;
import java.util.Map;
import lombok.Setter;
import org.apache.accumulo.core.client.AccumuloSecurityException;
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
public class FieldController {

    @Autowired
    @Setter
    private AccumuloService accumuloService = null;
    
    @RequestMapping("/field/list")
    public FieldResponse list(
        @RequestParam(value = "baseTableName", required = false, defaultValue = "edge") String baseTableName
        ,@RequestParam(value = "user", required = true) String user
        ,@RequestParam(value = "password", required = true) String password
        ,@RequestParam(value = "authorizationList", required = false, defaultValue = "") String authorizationList
        ,@RequestParam(value = "target", required = false, defaultValue=".*") String target
    ) {
        FieldResponse rv = new FieldResponse();
        Scanner scanner = null;
        
        Authorizations authorizations = null;
        if (authorizationList.isEmpty()) {
            authorizations = new Authorizations();
        } else {
            authorizations = new Authorizations(authorizationList);
        }

        Connector connector = accumuloService.getConnector(user, password);
        TableManager tableManager = new TableManager(connector);
        tableManager.setBaseTableName(baseTableName);
        final String tableName = tableManager.getMetadataTable();
        try {
            scanner = connector.createScanner(tableName, authorizations);
            scanner.setRange(Range.exact(new Text("field"), new Text("field")));

            Iterator<Map.Entry<Key, Value>> iterator = scanner.iterator();
            while (iterator.hasNext()) {
                Map.Entry<Key, Value> entry = iterator.next();
                String fieldname = entry.getKey().getColumnQualifier().toString();
                if (fieldname.matches(target)) {
                    String cardinality = entry.getValue().toString();
                    rv.put(fieldname, cardinality);
                }
            }
        } catch (TableNotFoundException e) {
            rv.setMessage(String.format("Unknown table [%s].", tableName));
            rv.setThrowable(e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        
        return rv;
    }

}
