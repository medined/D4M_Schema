package com.codebits.d4m.model;

import java.util.Objects;

public class FieldValuePageInfo implements Comparable {
    
    private String fieldValue;
    private int entryCount;
    private long timestamp;

    @Override
    public int compareTo(Object o) {
        return getFieldValue().compareTo(((FieldValuePageInfo)o).getFieldValue());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.getFieldValue());
        return hash;
    }

    @Override
    public String toString() {
        return "FieldPageInfo{" + "fieldName=" + getFieldValue() + ", entryCount=" + getEntryCount() + ", timestamp=" + getTimestamp() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldValuePageInfo other = (FieldValuePageInfo) obj;
        if (!Objects.equals(this.fieldValue, other.fieldValue)) {
            return false;
        }
        return true;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
