package com.codebits.d4m.rest.controller;

import com.codebits.d4m.TableManager;
import com.codebits.d4m.rest.model.EdgeModel;
import com.codebits.d4m.rest.model.RecordModel;
import com.codebits.d4m.rest.model.TransposeInfoModel;
import com.codebits.d4m.rest.service.AccumuloService;
import com.codebits.d4m.rest.service.FieldsetService;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.Setter;
import org.apache.accumulo.core.client.BatchScanner;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.GrepIterator;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecordController {

    @Autowired
    @Setter
    private AccumuloService accumuloService = null;
    
    @Autowired
    @Setter
    private FieldsetService fieldsetService = null;

    @RequestMapping("/record/fetch")
    public RecordModel fetchRow(
        @RequestParam(value = "baseTableName", required = false, defaultValue = "edge") String baseTableName
        ,@RequestParam(value = "row", required = true) String row
        ,@RequestParam(value = "user", required = true) String user
        ,@RequestParam(value = "password", required = true) String password
        ,@RequestParam(value = "fieldset", required = false) String fieldset
    ) {
        RecordModel rv = new RecordModel();
        Scanner scanner = null;

        Set<String> wantedFields = new TreeSet<>();        
        if (fieldset != null && !fieldset.isEmpty()) {
            String fieldList = fieldsetService.getList(fieldset);
            if (fieldList == null) {
                rv.setMessage(String.format("Unknown fieldset [%s].", fieldset));
                return rv;
           }
            wantedFields.addAll(Arrays.asList(fieldList.split(",")));
        }
        
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
                    boolean wanted = true;
                    if (!wantedFields.isEmpty()) {
                        wanted = false;
                        EdgeModel edge = new EdgeModel(entry.getKey());
                        for (String s : wantedFields) {
                            if (edge.getFieldName().matches(s)) {
                                wanted = true;
                                break;
                            }
                        }
                    }
                    if (wanted) {
                        rv.add(entry.getKey());
                    }
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
    
    @RequestMapping("/record/grep")
    public TransposeInfoModel grep(
        @RequestParam(value = "baseTableName", required = false, defaultValue = "edge") String baseTableName
        ,@RequestParam(value = "numQueryThreads", required = false, defaultValue = "10") int numQueryThreads
        ,@RequestParam(value = "maxRecords", required = false, defaultValue = "10000") int maxRecords
        ,@RequestParam(value = "user", required = true) String user
        ,@RequestParam(value = "password", required = true) String password
        ,@RequestParam(value = "authorizationList", required = false, defaultValue = "") String authorizationList
        ,@RequestParam(value = "target", required = true) String target
    ) {
        TransposeInfoModel rv = new TransposeInfoModel();
        BatchScanner scanner = null;
        int recordCount = 0;
        
        Authorizations authorizations = null;
        if (authorizationList.isEmpty()) {
            authorizations = new Authorizations();
        } else {
            authorizations = new Authorizations(authorizationList);
        }

        try {
            Connector connector = accumuloService.getConnector(user, password);
            TableManager tableManager = new TableManager(connector);
            tableManager.setBaseTableName(baseTableName);

            final String tableName = tableManager.getTransposeTable();

            try {
                scanner = connector.createBatchScanner(tableName, authorizations, numQueryThreads);
                
                scanner.setRanges(Collections.singleton(new Range((Key) null, null)));
                
                IteratorSetting is = new IteratorSetting(1, GrepIterator.class);
                GrepIterator.setTerm(is, target);

                scanner.addScanIterator(is);

                Iterator<Map.Entry<Key, Value>> iterator = scanner.iterator();
                boolean entryFound = false;
                while (iterator.hasNext()) {
                    entryFound = true;
                    Map.Entry<Key, Value> entry = iterator.next();
                    Key key = entry.getKey();
                    rv.add(key);
                    recordCount++;
                    if (recordCount > maxRecords) {
                        break;
                    }
                }
                if (not(entryFound)) {
                    rv.setMessage(String.format("target not found [%s].", target));
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

    private boolean not(boolean b) {
        return !b;
    }
}
