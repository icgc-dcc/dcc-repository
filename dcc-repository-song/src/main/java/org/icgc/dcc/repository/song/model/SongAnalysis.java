/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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



