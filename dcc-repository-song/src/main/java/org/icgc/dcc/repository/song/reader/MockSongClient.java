package org.icgc.dcc.repository.song.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.icgc.dcc.repository.song.model.SongAnalysis;

import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;

@Slf4j
public class MockSongClient extends AbstractSongClient {
    URL json;
    URL study;
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, false);
    private static final ObjectReader READER = MAPPER.reader(ObjectNode.class);

    @SneakyThrows
    public MockSongClient() {
        json = readJson("analyses.json");
        study = readJson("study.json");
    }
    @SneakyThrows
    public MockSongClient(String jsonFile) {
        json = readJson(jsonFile);
    }

    public Iterable<SongAnalysis> readAnalysis(JsonNode analyses) {
        assert(analyses.isArray());
        return StreamSupport.stream(analyses.spliterator(),false).
                map(analysis->createSongAnalysis(analysis, readStudy())).
                collect(Collectors.toList());
    }

    @SneakyThrows
    public JsonNode readStudy() {
        return MAPPER.readTree(study);
    }
    @Override
    @SneakyThrows
    public Iterable<SongAnalysis> readAnalysis() {
        val root = MAPPER.readTree(json);
        return readAnalysis(root);
    }

    @SneakyThrows
    private URL readJson(String jsonFile) {
        val resource = Resources.getResource(jsonFile);
        return resource;
    }
    private SongAnalysis createSongAnalysis(JsonNode json, JsonNode study) {
        return new SongAnalysis(json,study);
    }

}
