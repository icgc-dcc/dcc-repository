package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;



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



