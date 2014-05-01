package com.codebits.d4m;

import java.util.Collections;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.iterators.LongCombiner;
import org.apache.accumulo.core.iterators.user.SummingCombiner;
import org.apache.commons.lang.Validate;

public class TableManager {
    
    private String baseTableName = "edge";

    private TableOperations tableOperations = null;

    public TableManager() {
    }

    public TableManager(final TableOperations tableOperations) {
        this.tableOperations = tableOperations;
    }
    
    public void createTables(final String baseTableName) throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException {
        setBaseTableName(baseTableName);
        createTables();
    }

    public void createTables() throws AccumuloException, AccumuloSecurityException, TableExistsException, TableNotFoundException {
        Validate.notNull(tableOperations, "tableOperations must not be null");
		
        int isEdgePresent = tableOperations.exists(getEdgeTable()) ? 1 : 0;
        int isTransposePresent = tableOperations.exists(getTransposeTable()) ? 1 : 0;
        int isDegreePresent = tableOperations.exists(getDegreeTable()) ? 1 : 0;
        int isTextPresent = tableOperations.exists(getTextTable()) ? 1 : 0;

        int tableCount = isEdgePresent + isTransposePresent + isDegreePresent + isTextPresent;
        
        if (tableCount > 0 && tableCount < 4) {
            throw new D4MException("D4M: BASE[" + getBaseTableName() + "] Inconsistent state - one more more D4M table is missing.");
        }
        
        if (tableCount == 4) {
            // assume the tables are correct.
            return;
        }
        
        tableOperations.create(getEdgeTable());
        tableOperations.create(getTransposeTable());
        tableOperations.create(getDegreeTable(), false);
        tableOperations.create(getTextTable());

        IteratorSetting is = new IteratorSetting(7, SummingCombiner.class);
        SummingCombiner.setEncodingType(is, LongCombiner.Type.STRING);
        SummingCombiner.setColumns(is, Collections.singletonList(new IteratorSetting.Column("", "degree")));
            
        tableOperations.attachIterator(getDegreeTable(), is);        
    }
    
    public String getEdgeTable() {
        return "T" + getBaseTableName();
    }
    
    public String getTransposeTable() {
        return "T" + getBaseTableName() + "Transpose";
    }
    
    public String getDegreeTable() {
        return "T" + getBaseTableName() + "Degree";
    }
    
    public String getTextTable() {
        return "T" + getBaseTableName() + "Txt";
    }
    
    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    public TableOperations getTableOperations() {
        return tableOperations;
    }

    public void setTableOperations(TableOperations tableOperations) {
        this.tableOperations = tableOperations;
    }
    
}
