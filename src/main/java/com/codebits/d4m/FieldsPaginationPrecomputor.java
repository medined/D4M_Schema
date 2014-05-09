package com.codebits.d4m;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.io.Text;

/** Calculates page breaks for column names in the TedgeFields table.
 *
 */
public class FieldsPaginationPrecomputor {

    private Connector connector = null;
    private String tableName = null;
    private Authorizations authorizations = null;
    private int pageSize = 50;

    public Map<Integer, Text> computePageBreaks() {
        Validate.notNull(getAuthorizations(), "Authorizations must not be null");
        Validate.notNull(getConnector(), "connector must not be null");
        Validate.notNull(getTableName(), "tableName must not be null");

        Map<Integer, Text> pageBreaks = new HashMap<Integer, Text>();

        int pageNumber = 1;
        int entryCount = 0;

        Scanner scanner = null;
        try {
            scanner = getConnector().createScanner(getTableName(), getAuthorizations());

            IteratorSetting iter = new IteratorSetting(15, "fieldNames", RegExFilter.class);
            String rowRegex = null;
            String colfRegex = null;
            String colqRegex = "field";
            String valueRegex = null;
            boolean orFields = false;
            RegExFilter.setRegexs(iter, rowRegex, colfRegex, colqRegex, valueRegex, orFields);
            scanner.addScanIterator(iter);

            Iterator<Map.Entry<Key, Value>> iterator = scanner.iterator();
            while (iterator.hasNext()) {
                Entry<Key, Value> entry = iterator.next();
                if (entryCount == 0 || (entryCount % getPageSize() == 0)) {
                    pageBreaks.put(pageNumber, entry.getKey().getRow());
                    pageNumber++;
                }
                entryCount++;
            }

        } catch (TableNotFoundException e) {
            throw new D4MException("Unable to scan " + getTableName(), e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return pageBreaks;

    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setAuthorizations(Authorizations authorizations) {
        this.authorizations = authorizations;
    }

    public Connector getConnector() {
        return connector;
    }

    public String getTableName() {
        return tableName;
    }

    public Authorizations getAuthorizations() {
        return authorizations;
    }

    public int getPageSize() {
        return pageSize;
    }
}
