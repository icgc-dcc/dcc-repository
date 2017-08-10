package org.icgc.dcc.repository.song.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.repository.song.model.SongAnalysis;

import java.net.URI;
import java.net.URL;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;

public class RealSongClient extends AbstractSongClient {

    URI songPath;
    private static final ObjectMapper MAPPER = new ObjectMapper().
            configure(AUTO_CLOSE_SOURCE, false);

    private <T> Stream<T> stream(Iterable<T> o) {
        return StreamSupport.stream(o.spliterator(), false);
    }

    @SneakyThrows
    public RealSongClient(URI path) {
        songPath=path;
    }

    @SneakyThrows
    @Override
    public Iterable<SongAnalysis> readAnalysis() {
        val studies = getStudies();
        return stream(studies).flatMap(s->readStudy(s)).collect(Collectors.toList());
    }

    public Stream<SongAnalysis> readStudy(String id) {
        val study=getStudyNode(id);
        val analysis = getAnalysis(id);
        return createAnalysis(analysis, study);
    }

    public Stream<SongAnalysis> createAnalysis(JsonNode analysis, JsonNode study) {
        assert(analysis.isArray());
        return stream(analysis).map(a -> createSongAnalysis(a, study));
    }

    List<String> getStudies() {
        // FIXME: Create a SONG ENDPOINT, and read from that URL HERE...
        val studies = Arrays.asList("ABC123".split(","));
        return studies;
    }

    public SongAnalysis createSongAnalysis(JsonNode analysis, JsonNode study) {
        return new SongAnalysis(analysis, study);
    }

     public JsonNode getAnalysis(String study) {
        return readJson(songPath + "/studies/" + study + "/analysis");
    }

    public JsonNode getStudyNode(String study) {
        return readJson(songPath + "/studies/" + study + "/all");
    }

    @SneakyThrows
    public JsonNode readJson(String path) {
        return MAPPER.readTree(new URL(path));
    }

}
