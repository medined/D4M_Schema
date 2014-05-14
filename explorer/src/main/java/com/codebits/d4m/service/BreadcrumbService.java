package com.codebits.d4m.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class BreadcrumbService {
    
    private final Map<String, String> breadcrumbs = new LinkedHashMap<>();

    public Map<String, String> getBreadcrumbs() {
        return breadcrumbs;
    }

    public void clear() {
        breadcrumbs.clear();
    }

    public void put(String key, String value) {
        breadcrumbs.put(key, value);
    }

}
