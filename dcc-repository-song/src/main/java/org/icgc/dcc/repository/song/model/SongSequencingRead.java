package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;

public class SongSequencingRead extends SongExperiment {
    public final static String TYPE="sequencingRead";
    public enum Field {
            alignmentTool,
            libraryStrategy,
            referenceGenome,
            }
    SongSequencingRead(JsonNode j) { super(j); }
    public boolean isAligned() {
        return getBoolean("aligned");
    }

    public Long getInsertSize() {
        return getLong("insertSize");
    }

    public boolean isPairedEnd() {
        return getBoolean("pairedEnd");
    }

    public String get(Field f) {
        return get(f.toString());
    }
}
