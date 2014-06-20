package com.codebits.d4m.rest.service;

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
    private String instanceName = null;

    @Value("${accumulo.zookeeper.ensemble}")
    private String zookeeperEnsemble = null;

    @Value("${accumulo.user}")
    private String user = null;

    @Value("${accumulo.password}")
    private String password = null;
    
    public Connector getConnector() {
        return getConnector(getUser(), password);
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
        Validate.notNull(getInstanceName(), "Please specify accumulo.instance.name in property file.");
        Validate.notNull(getZookeeperEnsemble(), "Please specify accumulo.zookeeper.ensemble in property file.");
        Validate.notNull(getUser(), "Please specify accumulo.user in property file.");
        Validate.notNull(password, "Please specify accumulo.password in property file.");
        instance = new ZooKeeperInstance(getInstanceName(), getZookeeperEnsemble());
    }
    
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setZookeeperEnsemble(String zookeeperEnsemble) {
        this.zookeeperEnsemble = zookeeperEnsemble;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getInstanceName() {
        return instanceName;
    }

    public String getZookeeperEnsemble() {
        return zookeeperEnsemble;
    }

    public String getUser() {
        return user;
    }

}
