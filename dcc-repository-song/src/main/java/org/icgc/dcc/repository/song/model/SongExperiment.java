package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class SongExperiment extends JsonContainer {
    SongExperiment(JsonNode j) {
        super(j);
    }
}

