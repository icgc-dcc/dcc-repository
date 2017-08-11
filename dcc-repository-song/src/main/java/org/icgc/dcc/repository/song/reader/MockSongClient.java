package org.icgc.dcc.repository.song.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Resources;
import java.net.URL;

public class MockSongClient extends SongClient {
    private String analysesURL;
    private String studyURL;
    private URL studiesURL;

    public MockSongClient(String analysesFile, String studyFile, String studiesFile) {
        analysesURL = analysesFile;
        studyURL = studyFile;
        studiesURL = resourceFile(studiesFile);
    }

    private URL resourceFile(String file) {
        return Resources.getResource(file);
    }

    @Override
    JsonNode getStudy(String study) {
        return readJson(resourceFile(study+ "/" +studyURL));
    }
    @Override
    JsonNode getStudies() {
        return readJson(studiesURL);
    }
    @Override
    JsonNode getAnalyses(String study) {
        return readJson(resourceFile(study+"/"+analysesURL));
    }
}
