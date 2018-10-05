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
package org.icgc.dcc.repository.song.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.repository.song.model.AnalysisStates;
import org.icgc.dcc.repository.song.model.SongAnalysis;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.repository.song.model.AnalysisStates.PUBLISHED;

@Slf4j
public class SongClient {
  private static final ObjectMapper MAPPER = new ObjectMapper().
    configure(AUTO_CLOSE_SOURCE, false);
  private final String songToken;
  private final URL songPath;

  public SongClient(URL path, String token) {
    songPath = path;
    songToken = token;
  }

  SongClient() {
    songPath = url("http://localhost:8080");
    songToken = null;
  }

  public Iterable<SongAnalysis> readAnalyses(Set<AnalysisStates> analysisStates) {
    val studies = getStudies();
    return stream(studies).
      map(JsonNode::asText).
      flatMap(s -> readStudy(s, analysisStates)).
      collect(Collectors.toList());
  }

  private Stream<SongAnalysis> readStudy(String studyId, Set<AnalysisStates> analysisStates) {
    val analyses = getAnalyses(studyId, analysisStates);
    return createAnalyses(analyses);
  }

  private Stream<SongAnalysis> createAnalyses(JsonNode analyses) {
    assert (analyses.isArray());
    return stream(analyses).map(this::createSongAnalysis);
  }

  private SongAnalysis createSongAnalysis(JsonNode analysis) {
    return new SongAnalysis(analysis);
  }

  JsonNode getStudies() {
    return readJson(songPath + "/studies/all");
  }

  JsonNode getAnalyses(String study, Set<AnalysisStates> analysisStates) {
    String analysisStateParamValue;
    if (analysisStates.isEmpty()){
      analysisStateParamValue = PUBLISHED.name();
    } else {
      analysisStateParamValue = COMMA.join(analysisStates);
    }
    return readJson(songPath + "/studies/" + study + "/analysis?analysisStates="+analysisStateParamValue);
  }

  JsonNode readJson(String path) {
    return readJson(url(path));
  }

  @SneakyThrows
  JsonNode readJson(URL url) {
    val connection = url.openConnection();
    if (songToken != null) {
      connection.setRequestProperty("Authorization", "Bearer " + songToken);
    }
    log.info("Reading from " + url.toExternalForm());
    return MAPPER.readTree(connection.getInputStream());
  }

  @SneakyThrows
  JsonNode readJson(InputStream s) {
    return MAPPER.readTree(s);
  }

  <T> Stream<T> stream(Iterable<T> o) {
    return StreamSupport.stream(o.spliterator(), false);
  }

  @SneakyThrows
  URL url(String path) {
    return new URL(path);
  }

}
