package org.cbda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Mutation;
import org.apache.hadoop.io.Text;

public class TestableMutation extends Mutation {
    
    private final List<String> differences = new ArrayList<String>();

    public TestableMutation(CharSequence row) {
        super(row);
    }

    TestableMutation(Text row) {
        super(row);
    }

    @Override
    public boolean equals(Mutation other) {
        if (false == Arrays.equals(getRow(), other.getRow())) {
            getDifferences().add(String.format("Different rows. [%s] vs [%s]", new String(getRow()), new String(other.getRow())));
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
                getDifferences().add(String.format("Different CF at index %d. [%s] vs [%s].", i, new String(thisCF), new String(otherCF)));
                return false;
            }
            byte[] thisCQ = thisColumnUpdate.getColumnQualifier();
            byte[] otherCQ = otherColumnUpdate.getColumnQualifier();
            if (false == Arrays.equals(thisCQ, otherCQ)) {
                getDifferences().add(String.format("Different CQ at index %d. [%s] vs [%s].", i, new String(thisCQ), new String(otherCQ)));
                return false;
            }
            byte[] thisValue = thisColumnUpdate.getValue();
            byte[] otherValue = otherColumnUpdate.getValue();
            if (false == Arrays.equals(thisValue, otherValue)) {
                getDifferences().add(String.format("Different Value at index %d. [%s] vs [%s].", i, new String(thisValue), new String(otherValue)));
                return false;
            }
        }
        return true;
    }

    /**
     * @return the differences
     */
    public List<String> getDifferences() {
        return differences;
    }
}
