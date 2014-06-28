package com.codebits.d4m.rest.model;

import java.nio.charset.Charset;
import lombok.Getter;
import lombok.Setter;
import org.apache.accumulo.core.data.Key;

public class EdgeModel {

    protected final Charset charset = Charset.defaultCharset();
    private final String factDelimiter = "|";

    @Setter
    @Getter
    private String fieldName = null;
    
    @Setter
    @Getter
    private String fieldValue = null;
    
    @Setter
    @Getter
    private String recordId = null;
    
    @Setter
    @Getter
    private String visibility = null;
    
    @Setter
    @Getter
    long timestamp = 0;

    public EdgeModel(final Key key) {
        String factInfo = new String(key.getColumnQualifier().getBytes(), charset);
        int factDelimitorPosition = factInfo.indexOf(factDelimiter);
        fieldName = factInfo.substring(0, factDelimitorPosition);
        fieldValue = factInfo.substring(factDelimitorPosition + 1);
        visibility = key.getColumnVisibility().toString();
        recordId = key.getColumnQualifier().toString();
        timestamp = key.getTimestamp();
    }
    
}
