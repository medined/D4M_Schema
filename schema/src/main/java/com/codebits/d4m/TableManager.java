package com.codebits.d4m;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.Getter;
import lombok.Setter;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.iterators.LongCombiner;
import org.apache.accumulo.core.iterators.user.SummingCombiner;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.io.Text;

public class TableManager {
    
    @Setter @Getter private String baseTableName = "edge";
    @Setter @Getter private TableOperations tableOperations = null;
    @Setter private boolean sha1 = false;
    private final Charset charset = Charset.defaultCharset();

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
        int isFieldPresent = tableOperations.exists(getFieldTable()) ? 1 : 0;
        int isTextPresent = tableOperations.exists(getTextTable()) ? 1 : 0;

        int tableCount = isEdgePresent + isTransposePresent + isDegreePresent + isFieldPresent + isTextPresent;
        
        if (tableCount > 0 && tableCount < 5) {
            throw new D4MException("D4M: BASE[" + getBaseTableName() + "] Inconsistent state - one or more D4M tables is missing.");
        }
        
        if (tableCount == 5) {
            // assume the tables are correct.
            return;
        }
        
        tableOperations.create(getEdgeTable());
        tableOperations.create(getTransposeTable());
        tableOperations.create(getDegreeTable());
        tableOperations.create(getFieldTable());
        tableOperations.create(getTextTable());
        
        if (sha1) {
            // Pre-split the Tedge and TedgeText tables.
            // helpful when sha1 is used as row value.
            String hexadecimal = "123456789abcde";
            SortedSet<Text> splits = new TreeSet<Text>();
            for (byte b : hexadecimal.getBytes(charset)) {
                splits.add(new Text(new byte[] {b}));
            }
            tableOperations.addSplits(getEdgeTable(), splits);
            tableOperations.addSplits(getTextTable(), splits);
        }
        
        IteratorSetting degreeIteratorSetting = new IteratorSetting(7, SummingCombiner.class);
        SummingCombiner.setEncodingType(degreeIteratorSetting, LongCombiner.Type.STRING);
        SummingCombiner.setColumns(degreeIteratorSetting, Collections.singletonList(new IteratorSetting.Column("", "degree")));            
        tableOperations.attachIterator(getDegreeTable(), degreeIteratorSetting);
        
        IteratorSetting fieldIteratorSetting = new IteratorSetting(7, SummingCombiner.class);
        SummingCombiner.setEncodingType(fieldIteratorSetting, LongCombiner.Type.STRING);
        SummingCombiner.setColumns(fieldIteratorSetting, Collections.singletonList(new IteratorSetting.Column("", "field")));
        tableOperations.attachIterator(getFieldTable(), fieldIteratorSetting);        
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
        return "T" + getBaseTableName() + "Text";
    }
    
    public String getFieldTable() {
        return "T" + getBaseTableName() + "Field";
    }
    
}
