package com.codebits.d4m;

import java.io.IOException;
import java.util.Properties;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class PropertyManagerTest {

    private PropertyManager instance = null;
    private Properties properties = null;
    
    @Before
    public void setup() throws IOException {
        instance = new PropertyManager();
        instance.setPropertyFilename("PropertyManagerTest.properties");
        properties = instance.load();
    }
    
    @Test
    public void testSomeMethod() {
        assertEquals("a_value", properties.getProperty("my.test.property"));
    }
    
}
