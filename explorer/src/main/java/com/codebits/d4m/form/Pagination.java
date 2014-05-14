package com.codebits.d4m.form;

public class Pagination {
    
    private int pageSize;
    private int pageNumber;

    public int getPageSize() {
        return pageSize;
    }

    @Override
    public String toString() {
        return "Pagination{" + "pageSize=" + pageSize + ", pageNumber=" + pageNumber + '}';
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

}
