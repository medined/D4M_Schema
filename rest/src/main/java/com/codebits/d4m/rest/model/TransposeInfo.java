package com.codebits.d4m.rest.model;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.accumulo.core.data.Key;

public class TransposeInfo extends D4MResponse {

    @Getter
    private final List<TransposeRecord> records = new ArrayList<>();

    protected final Charset charset = Charset.defaultCharset();
    private final String factDelimiter = "|";

    public void add(final Key key) {
        String factInfo = new String(key.getRow().getBytes(), charset);
        int factDelimitorPosition = factInfo.indexOf(factDelimiter);

        TransposeRecord record = new TransposeRecord();
        record.fn = factInfo.substring(0, factDelimitorPosition);
        record.fv = factInfo.substring(factDelimitorPosition + 1);
        record.vis = key.getColumnVisibility().toString();
        record.id = key.getColumnQualifier().toString();
        record.ts = key.getTimestamp();
        records.add(record);
    }

}
