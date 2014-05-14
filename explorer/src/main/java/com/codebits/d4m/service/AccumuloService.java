package com.codebits.d4m.service;

import javax.annotation.PostConstruct;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccumuloService {
    
    private Instance instance = null;
    
    @Value("${accumulo.instance.name}")
    private String accumuloInstanceName = null;

    @Value("${accumulo.zookeeper.ensemble}")
    private String accumuloZookeeperEnsemble = null;

    @Value("${accumulo.user}")
    private String accumuloUser = null;

    @Value("${accumulo.password}")
    private String accumuloPassword = null;
    
    public Connector getConnector() {
        return getConnector(accumuloUser, accumuloPassword);
    }

    public Connector getConnector(final String user, final String password) {
        Connector connector = null;
        try {
            connector = instance.getConnector(user, password.getBytes());
        } catch (AccumuloException | AccumuloSecurityException e) {
            throw new RuntimeException("Error getting connector from instance.", e);
        }
        return connector;
    }

    @PostConstruct
    public void postConstruct() {
        Validate.notNull(accumuloInstanceName, "Please specify accumulo.instance.name in property file.");
        Validate.notNull(accumuloZookeeperEnsemble, "Please specify accumulo.zookeeper.ensemble in property file.");
        Validate.notNull(accumuloUser, "Please specify accumulo.user in property file.");
        Validate.notNull(accumuloPassword, "Please specify accumulo.password in property file.");
        instance = new ZooKeeperInstance(accumuloInstanceName, accumuloZookeeperEnsemble);
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

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public TableOperations getTableOperations() {
        return getConnector().tableOperations();
    }

}
