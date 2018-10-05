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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

public class SongAnalysis extends JsonContainer {
  private final static String SAMPLES = "sample";
  private final static String EXPERIMENT = "experiment";
  private final static String FILES = "file";
  private final static String INFO = "info";
  private static final String IS_PCAWG = "isPcawg";
  private static final String STUDY = "study";

  private List<SongFile> files;
  private List<SongSample> samples;
  private SongExperiment experiment;
  private Boolean pcawg;

  public SongAnalysis(JsonNode json) {
    super(json);
    setFiles(from(FILES));
    setSamples(from(SAMPLES));
    setExperiment(get(Field.analysisType), from(EXPERIMENT));
    setPcawg(from(INFO));
  }

  public List<SongSample> getSamples() {
    return Collections.unmodifiableList(samples);
  }

  private void setSamples(JsonNode s) {
    assert s.isArray();
    samples = new ArrayList<>();
    s.elements().forEachRemaining(node -> samples.add(new SongSample(node)));
  }

  public SongSample getFirstSample() {
    return samples.get(0);
  }

  public List<SongFile> getFiles() {
    return Collections.unmodifiableList(files);
  }

  public Optional<Boolean> isPcawg(){
    return Optional.ofNullable(this.pcawg);
  }

  private void setPcawg(JsonNode info){
    if (isNull(info) || info.isNull() || !info.has(IS_PCAWG)) {
      this.pcawg = null;
    }else {
      this.pcawg = info.path(IS_PCAWG).asBoolean();
    }
  }
  private void setFiles(JsonNode f) {
    assert f.isArray();
    files = new ArrayList<>();
    f.elements().forEachRemaining(node -> files.add(new SongFile(node)));
  }

  public String getStudyId() {
    return get(STUDY);
  }

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

  public String get(Field f) {
    return get(f.toString());
  }

  public enum Field {analysisId, analysisType, analysisState}

}



