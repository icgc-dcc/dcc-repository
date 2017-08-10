package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;

public class SongSample extends JsonContainer {
    SongSpecimen specimen;
    SongDonor donor;

    public enum Field { sampleId, sampleSubmitterId, sampleType}

    SongSample(JsonNode j) {
        super(j);
        specimen = new SongSpecimen(from("specimen"));
        donor = new SongDonor(from("donor"));
    }

    public SongSpecimen getSpecimen() {
        return specimen;
    }

    public SongDonor getDonor() {
        return donor;
    }

    public String get(Field f) {
        return get(f.toString());
    }
}

