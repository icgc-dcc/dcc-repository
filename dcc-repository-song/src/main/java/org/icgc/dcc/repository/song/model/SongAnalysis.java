package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class JsonContainer {
   private final JsonNode json;
   JsonContainer(JsonNode json) {
       assert json.isObject();
       this.json=json;
   }

    String get(String key) {
        return json.at("/"+key).asText();
    }

    public long getLong(String key) {
        return json.at("/" + key).asLong(0);
    }

    public JsonNode from(String key) {
        return json.at("/"+key);
    }

    public JsonNode getInfo() {
       return from("info");
    }

    @Override
    public String toString() {
        return json.toString();
    }
}

abstract class SongExperiment extends JsonContainer {
    SongExperiment(JsonNode j) {
        super(j);
    }
}

class SongVariantCall extends SongExperiment {
    public final static String TYPE="variantCall";
    enum Field {}
    SongVariantCall(JsonNode j) {
        super(j);
    }
}

class SongSequencingRead extends SongExperiment {
    public final static String TYPE="sequencingRead";
    enum Field {}
    SongSequencingRead(JsonNode j) { super(j); }
}

public class SongAnalysis extends JsonContainer {
    public enum Field { study, analysisId, analysisType, analysisState}

    List<SongFile> files;
    List<SongSample> samples;
    SongExperiment experiment;
    SongStudy study;

    private final static String SAMPLES ="sample";
    private final static String EXPERIMENT="experiment";
    private final static String FILES="file";


    public SongAnalysis(JsonNode json, JsonNode study) {
        super(json);
        setFiles(from(FILES));
        setSamples(from(SAMPLES));
        setExperiment(get(Field.analysisType), from(EXPERIMENT));
        this.study=new SongStudy(study);
    }

    private void setFiles(JsonNode f) {
        assert f.isArray();

        files = new ArrayList<>();
        f.elements().forEachRemaining(node->files.add(new SongFile(node)));
    }

    private void setSamples(JsonNode s) {
        assert s.isArray();
        samples = new ArrayList<>();
        s.elements().forEachRemaining(node->samples.add(new SongSample(node)));
    }

    public List<SongSample> getSamples() {
        return Collections.unmodifiableList(samples);
    }

    public List<SongFile> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public SongStudy getStudy() { return study; }

    public SongExperiment getExperiment() {
        return experiment;
    }

    private void setExperiment(String type, JsonNode e) {
        if (type.equals(SongSequencingRead.TYPE)) {
            experiment = new SongSequencingRead(e);
        } else if (type.equals(SongVariantCall.TYPE)) {
            experiment = new SongVariantCall(e);
        }
    }

    public SongSample getFirstSample()  {
        return samples.get(0);
    }

    public String get(Field f) {
        return get(f.toString());
    }
}



