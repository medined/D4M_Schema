package com.codebits.d4m;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;

public class PropertyManager {

    @Setter @Getter private String propertyFilename = null;

    public PropertyManager() {
    }

    public Properties load() throws IOException {
        Properties properties = new Properties();
        InputStream input = null;

        try {
            input = PropertyManager.class.getClassLoader().getResourceAsStream(getPropertyFilename());
            if (input == null) {
                throw new RuntimeException("Unable to find " + getPropertyFilename());
            }
            properties.load(input);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return properties;
    }

}
