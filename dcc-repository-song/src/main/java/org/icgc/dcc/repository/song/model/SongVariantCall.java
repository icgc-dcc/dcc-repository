package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;

public class SongVariantCall extends SongExperiment {
    public final static String TYPE="variantCall";
    public enum Field {variantCallingTool}

    SongVariantCall(JsonNode j) {
        super(j);
    }

    public String get(Field f) {
        return get(f.toString());
    }
}
