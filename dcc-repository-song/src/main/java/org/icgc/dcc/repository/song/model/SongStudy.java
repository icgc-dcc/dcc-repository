package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;

public class SongStudy extends JsonContainer {
    public enum Field {studyId,name,organization, description}

    SongStudy(JsonNode j) {
        super(j);
    }

    public String get(Field f) {
        return get(f.toString());
    }
}
