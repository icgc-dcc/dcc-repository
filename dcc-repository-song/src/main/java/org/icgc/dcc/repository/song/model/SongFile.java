package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;

public class SongFile extends JsonContainer {
    public enum Field { analysisId, studyId, objectId, fileName, fileType, fileSize, fileMd5sum}

    SongFile(JsonNode j) {
        super(j);
    }

    public String get(Field f) {
        return get(f.toString());
    }

    public Long getSize() {
        return getLong(Field.fileSize.toString());
    }
}
