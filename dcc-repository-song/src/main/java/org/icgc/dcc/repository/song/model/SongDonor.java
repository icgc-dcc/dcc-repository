package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;

public class SongDonor extends JsonContainer {
    public enum Field { donorId, donorSubmitterId, donorGender }
    SongDonor(JsonNode j) {
        super(j);
    }
    public String get(Field f) {
        return get(f.toString());
    }
}
