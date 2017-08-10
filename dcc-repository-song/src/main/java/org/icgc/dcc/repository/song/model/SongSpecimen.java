package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;

public class SongSpecimen extends JsonContainer {
    public enum Field {specimenId, specimenSubmitterId, specimenClass, specimenType}

    SongSpecimen(JsonNode j) {
        super(j);
    }
    public String get(Field f) {
        return get(f.toString());
    }
}
