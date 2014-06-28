package com.codebits.d4m.rest.model;

import java.nio.charset.Charset;
import lombok.Getter;
import org.apache.accumulo.core.data.Key;

public class EdgeDTO {

    protected final Charset charset = Charset.defaultCharset();
    private final String factDelimiter = "|";

    @Getter
    private String fn = null;
    
    @Getter
    private String fv = null;
    
    @Getter
    private String id = null;
    
    @Getter
    private String vis = null;
    
    @Getter
    long ts = 0;

    public EdgeDTO(final Key key) {
        String factInfo = new String(key.getColumnQualifier().getBytes(), charset);
        int factDelimitorPosition = factInfo.indexOf(factDelimiter);
        
        fn = factInfo.substring(0, factDelimitorPosition);
        fv = factInfo.substring(factDelimitorPosition + 1);
        vis = key.getColumnVisibility().toString();
        id = key.getColumnQualifier().toString();
        ts = key.getTimestamp();
    }
    
}
