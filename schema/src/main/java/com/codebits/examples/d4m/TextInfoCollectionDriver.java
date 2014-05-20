package com.codebits.examples.d4m;

import com.codebits.d4m.D4MException;
import com.codebits.d4m.PropertyManager;
import com.codebits.d4m.TableManager;
import com.codebits.d4m.overview.TextInfo;
import com.codebits.d4m.overview.TextInfoCollection;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.security.Authorizations;

public class TextInfoCollectionDriver {

    private final Charset charset = Charset.defaultCharset();

    public static void main(String[] args) {
        TextInfoCollectionDriver driver = new TextInfoCollectionDriver();
        driver.process();
    }

    public void process() {

        PropertyManager propertyManager = new PropertyManager();
        propertyManager.setPropertyFilename("d4m.properties");
        Properties properties;
        try {
            properties = propertyManager.load();
        } catch (IOException e) {
            throw new D4MException(String.format("Unable to load property file [%s].", propertyManager.getPropertyFilename()), e);
        }

        String instanceName = properties.getProperty("accumulo.instance.name");
        String zooKeepers = properties.getProperty("accumulo.zookeeper.ensemble");
        String user = properties.getProperty("accumulo.user");
        byte[] pass = properties.getProperty("accumulo.password").getBytes(charset);

        ZooKeeperInstance instance = new ZooKeeperInstance(instanceName, zooKeepers);
        Connector connector;
        Authorizations authorizations;
        try {
            connector = instance.getConnector(user, pass);
            authorizations = connector.securityOperations().getUserAuthorizations(user);
        } catch (AccumuloException e) {
            throw new D4MException(String.format("Exception connecting to Accumulo."), e);
        } catch (AccumuloSecurityException e) {
            throw new D4MException(String.format("Security exception connecting to Accumulo."));
        }

        TableManager tableManager = new TableManager(connector.tableOperations());

        TextInfoCollection textInfoCollection = new TextInfoCollection(tableManager, connector, authorizations);
        while (textInfoCollection.hasNext()) {
            TextInfo ti = textInfoCollection.next();
            String id = ti.getId();
            String text = ti.getText().substring(0, Math.min(25, ti.getText().length()));
            System.out.println(String.format("%s -> %s", id, text));
        }
        textInfoCollection.close();
    }
}
