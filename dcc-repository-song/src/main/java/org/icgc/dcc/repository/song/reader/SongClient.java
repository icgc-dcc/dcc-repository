package org.icgc.dcc.repository.song.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.repository.song.model.SongAnalysis;

import java.net.URI;
import java.net.URL;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;

public class SongClient {

    private final URL songPath;
    private static final ObjectMapper MAPPER = new ObjectMapper().
            configure(AUTO_CLOSE_SOURCE, false);

    public SongClient(URL path) {
        songPath=path;
    }

    SongClient() { songPath=url("http://localhost:8080"); }

    public Iterable<SongAnalysis> readAnalyses() {
        val studies = getStudies();
        return  stream(studies).
                map(JsonNode::asText).
                flatMap(this::readStudy).
                collect(Collectors.toList());
    }

    private Stream<SongAnalysis> readStudy(String id) {
        val study=getStudy(id);
        val analyses=getAnalyses(id);
        return createAnalyses(analyses, study);
    }

    private Stream<SongAnalysis> createAnalyses(JsonNode analyses, JsonNode study) {
        assert(analyses.isArray());
        return stream(analyses).map(a->createSongAnalysis(a, study));
    }

    private SongAnalysis createSongAnalysis(JsonNode analysis, JsonNode study) {
        return new SongAnalysis(analysis, study);
    }

   JsonNode getStudies() {
        return readJson(songPath + "/studies/all");
    }

    JsonNode getStudy(String study) {
        return readJson(songPath + "/studies/" + study + "/all");
    }

    JsonNode getAnalyses(String study) {
        return readJson(songPath + "/studies/" + study + "/analysis");
    }

    JsonNode readJson(String path) {
        return readJson(url(path));
    }

    @SneakyThrows
    JsonNode readJson(URL url) {
        return MAPPER.readTree(url);
    }

   <T> Stream<T> stream(Iterable<T> o) {
        return StreamSupport.stream(o.spliterator(), false);
    }

    @SneakyThrows
    URL url(String path) {
        return new URL(path);
    }

}
