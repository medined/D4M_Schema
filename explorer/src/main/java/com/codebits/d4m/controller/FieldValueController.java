package com.codebits.d4m.controller;

import com.codebits.d4m.model.FieldValuePageInfo;
import com.codebits.d4m.model.UserPreferences;
import com.codebits.d4m.service.FieldValuePaginationService;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Component
public class FieldValueController {

    @Autowired
    private UserPreferences preferences;
    
    @Autowired
    private FieldValuePaginationService fieldValuePaginationService;

    @RequestMapping(value = "/field_values/{fieldName}/", method = RequestMethod.GET)
    public String page(Model model, @PathVariable String fieldName) {
        Set<String> flags = new HashSet<>();
        int pageSize = preferences.getPageSize();
        
        SortedSet<FieldValuePageInfo> items = fieldValuePaginationService.getPage(flags, fieldName, null, pageSize, true);
        model.addAttribute("items", items);

        // FieldValuePagination
        //   first
        //   next

        model.addAttribute("fieldName", fieldName);
        model.addAttribute("items", items);

        return "field_values";
    }
    
    
    @ModelAttribute("preferences")
    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

    public FieldValuePaginationService getFieldValuePaginationService() {
        return fieldValuePaginationService;
    }

    public void setFieldValuePaginationService(FieldValuePaginationService fieldValuePaginationService) {
        this.fieldValuePaginationService = fieldValuePaginationService;
    }

}
