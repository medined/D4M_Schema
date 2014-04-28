package org.cbda;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyManager {

    private String propertyFilename = null;

    public PropertyManager() {
    }

    public Properties load() throws IOException {
        Properties properties = new Properties();
        InputStream input = null;

        try {
            input = PropertyManager.class.getClassLoader().getResourceAsStream(propertyFilename);
            if (input == null) {
                throw new RuntimeException("Unable to find " + propertyFilename);
            }
            properties.load(input);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return properties;
    }

    public void setPropertyFilename(String propertyFilename) {
        this.propertyFilename = propertyFilename;
    }

}
