package com.codebits.d4m.controller;

import com.codebits.d4m.form.Pagination;
import com.codebits.d4m.model.FieldPageInfo;
import com.codebits.d4m.model.UserPreferences;
import com.codebits.d4m.service.FieldPaginationService;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// TODO: mark records added since last pagination
@Controller
@Component
public class HomeController {

    @Autowired
    private UserPreferences preferences;

    @Autowired
    private FieldPaginationService fieldPaginationService = null;

    @RequestMapping(value = "/home/changePageSize", method = RequestMethod.POST)
    public String changePaginationInfo(@ModelAttribute("pagination") Pagination form, Model model) {
        if (form.getPageSize() != preferences.getPageSize()) {
            preferences.setPageSize(form.getPageSize());
            preferences.setPageNumber(1);
        } else {
            preferences.setPageNumber(form.getPageNumber());
        }
        String firstFieldName = fieldPaginationService.getFirstFieldNameOnPage(preferences.getPageNumber(), preferences.getPageSize());
        fetchFields(model, firstFieldName, true);
        return "home";
    }

    @RequestMapping(value = "/home/{pageNumber}", method = RequestMethod.GET)
    public String page(Model model, @PathVariable int pageNumber) {
        preferences.setPageNumber(pageNumber);
        String firstFieldName = fieldPaginationService.getFirstFieldNameOnPage(preferences.getPageNumber(), preferences.getPageSize());
        fetchFields(model, firstFieldName, true);
        return "home";
    }

    @RequestMapping(value = "/home/last", method = RequestMethod.GET)
    public String last(Model model) {
        preferences.setPageNumber(fieldPaginationService.getNumberOfPages(preferences.getPageSize()));
        String firstFieldName = fieldPaginationService.getFirstFieldNameOnPage(preferences.getPageNumber(), preferences.getPageSize());
        fetchFields(model, firstFieldName, true);
        return "home";
    }

    @RequestMapping(value = "/home/previous", method = RequestMethod.GET)
    public String previous(Model model) {
        preferences.setPageNumber(preferences.getPreviousPageNumber());
        String firstFieldName = fieldPaginationService.getFirstFieldNameOnPage(preferences.getPageNumber(), preferences.getPageSize());
        fetchFields(model, firstFieldName, true);
        return "home";
    }

    @RequestMapping(value = "/home/next", method = RequestMethod.GET)
    public String next(Model model) {
        SortedSet<FieldPageInfo> fields = fetchFields(model, preferences.getLastFieldOnPage(), false);

        // get the first field on the next page.
        int numPages = fieldPaginationService.getNumberOfPages(preferences.getPageSize());
        int nextPageNumber = preferences.getNextPageNumber(numPages);
        String firstFieldName = fieldPaginationService.getFirstFieldNameOnPage(nextPageNumber, preferences.getPageSize());

        // if the last field in the fields to be displayed 'larger' than the 
        // first field on the next page, then increment page number.
        if (fields.last().getFieldName().compareTo(firstFieldName) >= 0) {
            preferences.setPageNumber(nextPageNumber);
        }

        return "home";
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home(Model model, HttpSession session) {
        preferences.setPageNumber(1);
        String firstFieldName = fieldPaginationService.getFirstFieldNameOnPage(preferences.getPageNumber(), preferences.getPageSize());
        fetchFields(model, firstFieldName, true);
        return "home";
    }
    
    private static final int NUMBER_OF_PAGES_IN_PAGINATION = 6;

    private SortedSet<FieldPageInfo> fetchFields(final Model model, final String firstFieldName, final boolean startRowInclusive) {
        Set<String> flags = new HashSet<>();

        int numPages = fieldPaginationService.getNumberOfPages(preferences.getPageSize());
        model.addAttribute("numPages", numPages);

        int startPage = 1;
        int endPage = Math.min(NUMBER_OF_PAGES_IN_PAGINATION, numPages);
        
        Set<Integer> pageList = new TreeSet<>();
        if (numPages > NUMBER_OF_PAGES_IN_PAGINATION) {
            startPage = Math.max(1, preferences.getPageNumber() - (NUMBER_OF_PAGES_IN_PAGINATION / 3));
            endPage = Math.min(numPages + 1, startPage + NUMBER_OF_PAGES_IN_PAGINATION);
            // ensure there are always NUMBER_OF_PAGES_IN_PAGINATION on display.
            if ((endPage - startPage) < NUMBER_OF_PAGES_IN_PAGINATION) {
                startPage -= (NUMBER_OF_PAGES_IN_PAGINATION - (endPage - startPage));
            }
        }
        for (int i = startPage; i < endPage; i++) {
            pageList.add(i);
        }
        model.addAttribute("pageList", pageList);
        
        Set<Integer> pageSizes = fieldPaginationService.getPageSizeOptions();
        model.addAttribute("pageSizes", pageSizes);

        SortedSet<FieldPageInfo> fieldPageInfoSet = fieldPaginationService.getPage(flags, firstFieldName, preferences.getPageSize(), startRowInclusive);
        model.addAttribute("fieldPageInfoSet", fieldPageInfoSet);
        model.addAttribute("firstFieldName", fieldPageInfoSet.first().getFieldName());
        model.addAttribute("lastFieldName", fieldPageInfoSet.last().getFieldName());
        model.addAttribute("endOfTable", flags.contains(FieldPaginationService.END_OF_TABLE));
        preferences.setLastFieldOnPage(fieldPageInfoSet.last().getFieldName());

        return fieldPageInfoSet;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root(Model model) {
        return "redirect:/home";
    }

    @ModelAttribute("preferences")
    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setFieldPaginationService(FieldPaginationService fieldPaginationService) {
        this.fieldPaginationService = fieldPaginationService;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

}
