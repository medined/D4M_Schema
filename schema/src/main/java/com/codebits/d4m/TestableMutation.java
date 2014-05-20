package com.codebits.d4m;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Mutation;
import org.apache.hadoop.io.Text;

public class TestableMutation extends Mutation {
    
    @Getter private final List<String> differences = new ArrayList<String>();
    private final Charset charset = Charset.defaultCharset();

    public TestableMutation(CharSequence row) {
        super(row);
    }

    public TestableMutation(Text row) {
        super(row);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Mutation == false) {
            getDifferences().add("Other should be Mutatation not " + o.getClass().getName());
            return false;
        }
        
        Mutation other = (Mutation)o;
        
        if (false == Arrays.equals(getRow(), other.getRow())) {
            String a = new String(getRow(), charset);
            String b = new String(other.getRow(), charset);
            getDifferences().add(String.format("Different rows. [%s] vs [%s]", a, b));
            return false;
        }
        if (this.getUpdates().size() != other.getUpdates().size()) {
            getDifferences().add(String.format("Different entry counts. [%d] vs [%d]", this.getUpdates().size(), other.getUpdates().size()));
            return false;
        }
        List thisList = this.getUpdates();
        List otherList = other.getUpdates();
        for (int i = 0; i < thisList.size(); i++) {
            ColumnUpdate thisColumnUpdate = (ColumnUpdate) thisList.get(i);
            ColumnUpdate otherColumnUpdate = (ColumnUpdate) otherList.get(i);
            byte[] thisCF = thisColumnUpdate.getColumnFamily();
            byte[] otherCF = otherColumnUpdate.getColumnFamily();
            if (false == Arrays.equals(thisCF, otherCF)) {
                String a = new String(thisCF, charset);
                String b = new String(otherCF, charset);
                getDifferences().add(String.format("Different CF at index %d. [%s] vs [%s].", i, a, b));
                return false;
            }
            byte[] thisCQ = thisColumnUpdate.getColumnQualifier();
            byte[] otherCQ = otherColumnUpdate.getColumnQualifier();
            if (false == Arrays.equals(thisCQ, otherCQ)) {
                String a = new String(thisCQ, charset);
                String b = new String(otherCQ, charset);
                getDifferences().add(String.format("Different CQ at index %d. [%s] vs [%s].", i, a, b));
                return false;
            }
            byte[] thisValue = thisColumnUpdate.getValue();
            byte[] otherValue = otherColumnUpdate.getValue();
            if (false == Arrays.equals(thisValue, otherValue)) {
                String a = new String(thisValue, charset);
                String b = new String(otherValue, charset);
                getDifferences().add(String.format("Different Value at index %d. [%s] vs [%s].", i, a, b));
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Arrays.hashCode(getRow());
        List thisList = this.getUpdates();
        for (Object columnUpdate : thisList) {
            ColumnUpdate thisColumnUpdate = (ColumnUpdate) columnUpdate;
            byte[] thisCF = thisColumnUpdate.getColumnFamily();
            byte[] thisCQ = thisColumnUpdate.getColumnQualifier();
            byte[] thisValue = thisColumnUpdate.getValue();
            hash = 47 * hash + Arrays.hashCode(thisCF);
            hash = 47 * hash + Arrays.hashCode(thisCQ);
            hash = 47 * hash + Arrays.hashCode(thisValue);
        }
        return hash;
    }

}
