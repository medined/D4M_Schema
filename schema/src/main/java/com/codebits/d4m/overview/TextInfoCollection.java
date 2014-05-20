package com.codebits.d4m.overview;

import com.codebits.d4m.D4MException;
import com.codebits.d4m.TableManager;
import java.util.Iterator;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.KeyValue;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;

public class TextInfoCollection implements Iterator {

    private Scanner scanner = null;
    private Iterator iterator = null;

    public TextInfoCollection(final TableManager tableManager, final Connector connector, final Authorizations authorizations) {
        try {
            scanner = connector.createScanner(tableManager.getTextTable(), authorizations);
        } catch (TableNotFoundException e) {
            throw new D4MException(String.format("Table [%s] not found.", tableManager.getTextTable()), e);
        }

        IteratorSetting iteratorSetting = new IteratorSetting(15, "fieldNames", RegExFilter.class);
        String rowRegex = null;
        String colfRegex = null;
        String colqRegex = "text";
        String valueRegex = null;
        boolean orFields = false;
        RegExFilter.setRegexs(iteratorSetting, rowRegex, colfRegex, colqRegex, valueRegex, orFields);
        scanner.addScanIterator(iteratorSetting);

        iterator = scanner.iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public TextInfo next() {
        KeyValue kv = (KeyValue)iterator.next();
        String id = kv.getKey().getRow().toString();
        String text = kv.getValue().toString();
        return new TextInfo(id, text);
    }

    public void remove() {
        iterator.remove();
    }
    
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }

}
