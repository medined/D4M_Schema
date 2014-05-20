package com.codebits.d4m.service;

import com.codebits.d4m.TableManager;
import com.codebits.d4m.ingest.MutationFactory;
import com.codebits.d4m.model.FieldValuePageInfo;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FieldValuePaginationService {

    public final static String END_OF_TABLE = "EOT";

    private TableManager tableManager = null;

    @Autowired
    private AccumuloService accumuloService = null;

    @PostConstruct
    public void setup() {
        tableManager = new TableManager();
        tableManager.setTableOperations(accumuloService.getTableOperations());
    }

    /* Get a page of field values.
     * 
     * root@instance TedgeTranspose> table TedgeDegree
     * root@instance TedgeDegree> scan
     * a00100|0.0000 :degree []    1200
     * a00100|0.0001 :degree []    20258
     */
    public SortedSet<FieldValuePageInfo> getPage(Set<String> flags, final String fieldName, final String firstFieldOnPage, final int pageSize, final boolean startRowInclusive) {
        final SortedSet<FieldValuePageInfo> rv = new TreeSet<>();
        Connector connector = accumuloService.getConnector();
        final String tableName = tableManager.getDegreeTable();

        Scanner scan = null;
        try {
            scan = connector.createScanner(tableName, new Authorizations());
        } catch (TableNotFoundException e) {
            throw new RuntimeException(String.format("Error getting scanning table [%s].", tableName), e);
        }
        scan.setBatchSize(pageSize);
        scan.setRange(Range.prefix(new Text(fieldName)));

        if (firstFieldOnPage != null) {
            Text row = new Text(firstFieldOnPage);
            scan.setRange(new Range(row, startRowInclusive, null, true));
        }

        int fieldCount = 0;
        Iterator<Map.Entry<Key, Value>> iterator = scan.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();

            String row = entry.getKey().getRow().toString();
            int delimiterPos = row.indexOf("|");
            if (delimiterPos != -1) {
                String fieldValue = row.substring(delimiterPos + 1);

                FieldValuePageInfo info = new FieldValuePageInfo();
                info.setFieldValue(fieldValue);
                info.setEntryCount(Integer.valueOf(entry.getValue().toString()));
                info.setTimestamp(entry.getKey().getTimestamp());
                rv.add(info);
                fieldCount++;
                if (fieldCount >= pageSize) {
                    break;
                }
            }
        }
        if (false == iterator.hasNext()) {
            flags.add(END_OF_TABLE);
        }

        scan.close();

        return rv;
    }

    public void setAccumuloService(AccumuloService accumuloService) {
        this.accumuloService = accumuloService;
    }

}
