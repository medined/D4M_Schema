package com.codebits.d4m.controller;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.io.Text;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Component
public class HomeControllerBackup {

    @Value("${accumulo.instance.name}")
    private String accumuloInstanceName = null;

    @Value("${accumulo.zookeeper.ensemble}")
    private String accumuloZookeeperEnsemble = null;

    @Value("${accumulo.user}")
    private String accumuloUser = null;

    @Value("${accumulo.password}")
    private String accumuloPassword = null;

    private String tableName = null;

    private final int batchSize = 10;

    @RequestMapping(value = "/xhome/prev/{endRow}", method = RequestMethod.GET)
    public String xhomePrev(Model model, @PathVariable String endRow) {
        fetchFields(model, null, endRow);
        return "home";
    }

    @RequestMapping(value = "/xhome/next/{startRow}", method = RequestMethod.GET)
    public String xhomeNext(Model model, @PathVariable String startRow) {
        fetchFields(model, startRow, null);
        return "home";
    }

    @RequestMapping(value = "/xhome", method = RequestMethod.GET)
    public String xhome(Model model) {
        fetchFields(model, null, null);
        return "home";
    }

    private Text rowWrap(final String row) {
        return row == null ? null : new Text(row);
    }

    public void fetchFields(final Model model, final String startRow, final String endRow) {
        Validate.notNull(accumuloInstanceName, "Please specify accumulo.instance.name in property file.");
        Validate.notNull(accumuloZookeeperEnsemble, "Please specify accumulo.zookeeper.ensemble in property file.");
        Validate.notNull(accumuloUser, "Please specify accumulo.user in property file.");
        Validate.notNull(accumuloPassword, "Please specify accumulo.password in property file.");

        Connector connector = null;
        Instance instance = new ZooKeeperInstance(accumuloInstanceName, accumuloZookeeperEnsemble);
        try {
            connector = instance.getConnector(accumuloUser, accumuloPassword.getBytes());
        } catch (AccumuloException | AccumuloSecurityException e) {
            throw new RuntimeException("Error getting connector from instance.", e);
        }

        tableName = "TedgeField";

        String scanStartRow = startRow;
        String scanEndRow = endRow;
        
        boolean startRowInclusive = true;
        boolean endRowInclusive = true;

        if (startRow == null && endRow == null) {
            // start scan at beginning.
        }
        if (startRow == null && endRow != null) {
            // getting previous page. The startRow parameter becomes the last row of the scan.
            // and we don't want to see that row returned.
            //scanEndRow = startRow;
            endRowInclusive = false;
        }
        if (startRow != null && endRow == null) {
            // getting next page. We don't want to see the last row of the previous page.
            startRowInclusive = false;
            //scanStartRow = endRow;
        }
        System.out.println("scanStartRow: " + scanStartRow);
        System.out.println("scanEndRow: " + scanEndRow);

        Scanner scan = null;
        try {
            scan = connector.createScanner(tableName, new Authorizations());
        } catch (TableNotFoundException e) {
            throw new RuntimeException("Error getting scanning table.", e);
        }
        scan.setBatchSize(batchSize);
        scan.setRange(new Range(rowWrap(scanStartRow), startRowInclusive, rowWrap(scanEndRow), endRowInclusive));

        Map<String, Integer> columns = new TreeMap<>();

        IteratorSetting iter = new IteratorSetting(15, "fieldNames", RegExFilter.class);
        String rowRegex = null;
        String colfRegex = null;
        String colqRegex = "field";
        String valueRegex = null;
        boolean orFields = false;
        RegExFilter.setRegexs(iter, rowRegex, colfRegex, colqRegex, valueRegex, orFields);
        scan.addScanIterator(iter);

        Text pageFirstRow = null;
        Text pageLastRow = null;

        CircularFifoBuffer buffer = new CircularFifoBuffer(scan.getBatchSize());

        int fetchCount = 0;
        Iterator<Map.Entry<Key, org.apache.accumulo.core.data.Value>> iterator = scan.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator.next();
            String columnName = entry.getKey().getRow().toString();
            System.out.println("columnName: " + columnName);
            if (pageFirstRow == null) {
                pageFirstRow = entry.getKey().getRow();
            }
            Integer entryCount = Integer.parseInt(entry.getValue().toString());
            columns.put(columnName, entryCount);
            buffer.add(columnName);
            fetchCount++;
            if (fetchCount > scan.getBatchSize()) {
                pageLastRow = entry.getKey().getRow();
                break;
            }
        }

        scan.close();

        if (pageFirstRow != null) {
            model.addAttribute("pageFirstRow", pageFirstRow.toString());
            model.addAttribute("startRow", pageFirstRow.toString());
        }
        if (pageLastRow != null) {
            model.addAttribute("pageLastRow", pageLastRow.toString());
            model.addAttribute("endRow", pageLastRow.toString());
        }

        model.addAttribute("fifoBuffer", buffer);
        model.addAttribute("columns", columns);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root(Model model) {
        return "redirect:/home";
    }

    public void setAccumuloInstanceName(String accumuloInstanceName) {
        this.accumuloInstanceName = accumuloInstanceName;
    }

    public void setAccumuloZookeeperEnsemble(String accumuloZookeeperEnsemble) {
        this.accumuloZookeeperEnsemble = accumuloZookeeperEnsemble;
    }

    public void setAccumuloUser(String accumuloUser) {
        this.accumuloUser = accumuloUser;
    }

    public void setAccumuloPassword(String accumuloPassword) {
        this.accumuloPassword = accumuloPassword;
    }

}
