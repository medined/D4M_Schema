package com.codebits.d4m.model;

import java.util.Objects;

public class FieldPageInfo implements Comparable {
    
    private String fieldName;
    private int entryCount;
    private long timestamp;

    @Override
    public int compareTo(Object o) {
        return getFieldName().compareTo(((FieldPageInfo)o).getFieldName());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.getFieldName());
        return hash;
    }

    @Override
    public String toString() {
        return "FieldPageInfo{" + "fieldName=" + getFieldName() + ", entryCount=" + getEntryCount() + ", timestamp=" + getTimestamp() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldPageInfo other = (FieldPageInfo) obj;
        if (!Objects.equals(this.fieldName, other.fieldName)) {
            return false;
        }
        return true;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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
