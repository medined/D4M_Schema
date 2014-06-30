package com.codebits.d4m.rest.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FieldsetService {

    private static final Map<String, String> lists = new HashMap<>();

    public void put(final String listName, final String fieldList) {
        lists.put(listName, fieldList);
    }
    
    public Map<String, String> getLists() {
        return Collections.unmodifiableMap(lists);
    }
    
    public String getList(final String listName) {
        return lists.get(listName);
    }
    
    public void clear() {
        lists.clear();
    }
    
    public void delete(final String listName) {
        lists.remove(listName);
    }

}
