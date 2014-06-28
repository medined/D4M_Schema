package com.codebits.d4m.rest.model;

import java.nio.charset.Charset;
import lombok.Getter;
import org.apache.accumulo.core.data.Key;

/*
 * This class is serialized into JSON so the attribute names are very short.
 */
public class TransposeDTO {
    
    protected final Charset charset = Charset.defaultCharset();
    private final String factDelimiter = "|";

    @Getter
    private final String fn; // field name
    
    @Getter
    private final String fv; // field value

    @Getter
    private final String vis;
    
    @Getter
    private final String id;
    
    @Getter
    private final long ts;

    public TransposeDTO(final Key key) {
        String factInfo = new String(key.getRow().getBytes(), charset);
        int factDelimitorPosition = factInfo.indexOf(factDelimiter);
    
        this.fn = factInfo.substring(0, factDelimitorPosition);
        this.fv = factInfo.substring(factDelimitorPosition + 1);
        this.vis = key.getColumnVisibility().toString();
        this.id = key.getColumnQualifier().toString();
        this.ts = key.getTimestamp();
    }
}
