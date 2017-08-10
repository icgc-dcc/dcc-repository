package org.icgc.dcc.repository.song.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Resources;
import java.net.URL;

public class MockSongClient extends SongClient {
    private URL analysesURL;
    private URL studyURL;
    private URL studiesURL;

    public MockSongClient(String analysesFile, String studyFile, String studiesFile) {
        analysesURL = resourceFile(analysesFile);
        studyURL = resourceFile(studyFile);
        studiesURL = resourceFile(studiesFile);
    }

    private URL resourceFile(String file) {
        return Resources.getResource(file);
    }

    @Override
    JsonNode getStudy(String study) {
        return readJson(studyURL);
    }
    JsonNode getStudies() {
        return readJson(studiesURL);
    }
    JsonNode getAnalyses(String study) {
        return readJson(analysesURL);
    }

}
