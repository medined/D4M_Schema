package com.codebits.d4m.model;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class UserPreferences {

    private int pageNumber;
    private int pageSize;
    private String lastFieldOnPage;

    @PostConstruct
    public void setup() {
        pageNumber = 1;
        setPageSize(10);
    }
    
    public int getPreviousPageNumber() {
        return pageNumber > 2 ? pageNumber - 1 : 1;
    }
    
    public int getNextPageNumber(final int maxPage) {
        return pageNumber < maxPage ? pageNumber + 1 : maxPage;
    }
    
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getLastFieldOnPage() {
        return lastFieldOnPage;
    }

    public void setLastFieldOnPage(String lastFieldOnPage) {
        this.lastFieldOnPage = lastFieldOnPage;
    }
    
}
