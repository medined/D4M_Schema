package com.codebits.d4m.rest.model;

import lombok.Getter;
import lombok.Setter;

public class D4MResponse {

    @Setter
    @Getter
    private String message = null;
    
    @Setter
    @Getter
    private Throwable throwable = null;
    
}
