package com.codebits.d4m.rest.service;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccumuloService {
    
    @Setter
    @Getter
    private Instance instance = null;
    
    @Setter
    @Getter
    @Value("${accumulo.instance.name}")
    private String instanceName = null;

    @Setter
    @Getter
    @Value("${accumulo.zookeeper.ensemble}")
    private String zookeeperEnsemble = null;
    
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
        instance = new ZooKeeperInstance(getInstanceName(), getZookeeperEnsemble());
    }
    
}
