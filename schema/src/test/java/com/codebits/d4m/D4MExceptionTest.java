package com.codebits.d4m;

import org.junit.Test;
import static org.junit.Assert.*;

public class D4MExceptionTest {
    
    private final static Throwable throwable = new Throwable();
        
    @Test
    public void testConstructor() {
        assertNotNull(new D4MException());
    }
    
    @Test
    public void testConstructor_with_string() {
        assertNotNull(new D4MException("message"));
    }
    
    @Test
    public void testConstructor_with_string_and_throwable() {
        assertNotNull(new D4MException("message", throwable));
    }
    
    @Test
    public void testConstructor_with_throwable() {
        assertNotNull(new D4MException(throwable));
    }
    
}
