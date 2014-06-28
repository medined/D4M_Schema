package com.codebits.d4m.rest.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.accumulo.core.data.Key;

public class TransposeInfoModel extends D4MResponse {

    @Getter
    private final List<TransposeDTO> records = new ArrayList<>();

    public void add(final Key key) {
        records.add(new TransposeDTO(key));
    }

}
