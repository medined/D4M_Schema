package com.codebits.d4m.service;

import com.codebits.d4m.TableManager;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
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
import org.springframework.stereotype.Service;

@Service
public class FieldPaginationService {

    public final static String END_OF_TABLE = "EOT";
    
    private TableManager tableManager = null;
    
    @Autowired
    private AccumuloService accumuloService = null;
    
    @PostConstruct
    public void setup() {
        tableManager = new TableManager();
        tableManager.setTableOperations(accumuloService.getTableOperations());
    }
    
    //100_per_page num_pages:pages []    1
    public Set<Integer> getPageSizeOptions() {
        Set<Integer> rv = new TreeSet<>();

        Connector connector = accumuloService.getConnector();

        final String tableName = tableManager.getFieldTable();

        Scanner scanner = null;
        try {
            scanner = connector.createScanner(tableName, new Authorizations());
        } catch (TableNotFoundException e) {
            throw new RuntimeException(String.format("Error getting scanning table [%s].", tableName), e);
        }

        IteratorSetting iter = new IteratorSetting(15, "pageSizeOptions", RegExFilter.class);
        String rowRegex = null;
        String colfRegex = "num_pages";
        String colqRegex = "pages";
        String valueRegex = null;
        boolean orFields = false;
        RegExFilter.setRegexs(iter, rowRegex, colfRegex, colqRegex, valueRegex, orFields);
        scanner.addScanIterator(iter);

        Iterator<Map.Entry<Key, Value>> iterator = scanner.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();
            int pageSize = Integer.valueOf(entry.getKey().getRow().toString().split("_")[0]);
            rv.add(pageSize);
        }
        
        scanner.close();

        return rv;
    }

    public int getNumberOfPages(final int pageSize) {
        int numPages = 0;

        Connector connector = accumuloService.getConnector();

        final String tableName = tableManager.getFieldTable();

        Scanner scan = null;
        try {
            scan = connector.createScanner(tableName, new Authorizations());
        } catch (TableNotFoundException e) {
            throw new RuntimeException(String.format("Error getting scanning table [%s].", tableName), e);
        }
        Text row = new Text(String.format("%d_per_page", pageSize));
        Text cf = new Text("num_pages");
        Text cq = new Text("pages");
        scan.setBatchSize(1);
        scan.setRange(Range.exact(row, cf, cq));

        Iterator<Map.Entry<Key, org.apache.accumulo.core.data.Value>> iterator = scan.iterator();
        if (iterator.hasNext()) {
            Map.Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator.next();
            numPages = Integer.valueOf(entry.getValue().toString());
        }
        
        scan.close();

        return numPages;
    }
    
    /* Get the pageSize field names.
     * 
     * ROW         | CF | CQ | VALUE
     * [fieldName[ |    |    | [count]
     * [fieldName] |    |    | [count]
     * 
     */
    public SortedSet<String> getPage(Set<String> flags, final String firstFieldOnPage, final int pageSize, final boolean startRowInclusive) {
        final SortedSet<String> rv = new TreeSet<>();
        Connector connector = accumuloService.getConnector();
        final String tableName = tableManager.getFieldTable();
        
        Scanner scan = null;
        try {
            scan = connector.createScanner(tableName, new Authorizations());
        } catch (TableNotFoundException e) {
            throw new RuntimeException(String.format("Error getting scanning table [%s].", tableName), e);
        }
        Text row = new Text(firstFieldOnPage);
        scan.setBatchSize(pageSize);
        scan.setRange(new Range(row, startRowInclusive, null, true));
        
        int fieldCount = 0;
        Iterator<Map.Entry<Key, Value>> iterator = scan.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();
            rv.add(entry.getKey().getRow().toString());
            fieldCount++;
            if (fieldCount >= pageSize) {
                break;
            }
        }
        if (false == iterator.hasNext()) {
            flags.add(END_OF_TABLE);
        }
        
        scan.close();

        return rv;
    }

    /** Get the first field name on a page.
     * 
     * ROW         | CF | CQ    | VALUE
     * 10_per_page | 1  | pages | a00100
     * 10_per_page | 2  | pages | boiler
     * 
     * CF = page number
     * VALUE = page break (top of page value)
     * 
     * @param pageNumber
     * @param pageSize
     * @return 
     */
    public String getFirstFieldNameOnPage(final int pageNumber, final int pageSize) {
        String fieldName = null;
        Connector connector = accumuloService.getConnector();
        final String tableName = tableManager.getFieldTable();
        Text tPageNumber = new Text(Integer.toString(pageNumber));

        Scanner scan = null;
        try {
            scan = connector.createScanner(tableName, new Authorizations());
        } catch (TableNotFoundException e) {
            throw new RuntimeException(String.format("Error getting scanning table [%s].", tableName), e);
        }
        Text row = new Text(String.format("%d_per_page", pageSize));
        Text cf = tPageNumber;
        Text cq = new Text("pages");
        scan.setBatchSize(1);
        scan.setRange(Range.exact(row, cf, cq));
        
        Iterator<Map.Entry<Key, Value>> iterator = scan.iterator();
        if (iterator.hasNext()) {
            Map.Entry<Key, Value> entry = iterator.next();
            fieldName = entry.getValue().toString();
        }
        
        scan.close();

        return fieldName;
    }

    public void setAccumuloService(AccumuloService accumuloService) {
        this.accumuloService = accumuloService;
    }

}
