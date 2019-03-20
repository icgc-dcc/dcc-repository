/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.                             
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
package org.icgc.dcc.repository.song.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import lombok.val;
import org.icgc.dcc.repository.core.model.Repositories;
import org.icgc.dcc.repository.song.core.SongProcessor;
import org.icgc.dcc.repository.song.model.SongAnalysis;
import org.icgc.dcc.repository.song.reader.MockSongClient;
import org.icgc.dcc.repository.song.reader.SongClient;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static org.icgc.dcc.repository.core.util.RepositoryFileContexts.newLocalRepositoryFileContext;
import static org.icgc.dcc.repository.song.model.AnalysisStates.PUBLISHED;
import static org.icgc.dcc.repository.song.model.AnalysisStates.UNPUBLISHED;

public class SongProcessorTest {
  private static final ObjectMapper MAPPER = new ObjectMapper().
    configure(AUTO_CLOSE_SOURCE, false);
  SongProcessor songProcessor;
  SongClient songClient;

  @Before
  public void testExecute() throws IOException {
    val context = newLocalRepositoryFileContext();
    val repository = Repositories.getCollabRepository();

    songProcessor = new SongProcessor(context, repository);
    songClient = new MockSongClient("analyses.json", "studies.json");
  }

  @Test
  public void testSequencingReadDataType() throws IOException {
    assert "Aligned Reads".equals(songProcessor.sequencingReadDataType("BAM", true));
    assert "Unaligned Reads".equals(songProcessor.sequencingReadDataType("BAM", false));
    assert "Sequencing Reads".equals(songProcessor.sequencingReadDataType("BAM", null));

    assert "Unaligned Reads".equals(songProcessor.sequencingReadDataType("FASTA", true));
    assert "Unaligned Reads".equals(songProcessor.sequencingReadDataType("FASTQ", false));
    assert "Unaligned Reads".equals(songProcessor.sequencingReadDataType("FASTQ", null));
  }

  @Test
  public void testIsIndexable() {
      val analyses = songClient.readAnalyses(ImmutableSet.of(UNPUBLISHED));
      for(SongAnalysis a:analyses) {
        System.err.printf("Checking analysis '%s'\n",a);
        if (a.get(SongAnalysis.Field.analysisId).equals("AN1")) {
          indexable(a,false);
        } else {
          indexable(a, true);
        }
      }
  }

  private void indexable(SongAnalysis analysis, boolean expected) {
    assert songProcessor.isIndexable(analysis) == expected;
  }

  public JsonNode toJSON(String jsonString) throws IOException{
    return MAPPER.readTree(jsonString.replace('\'','"'));
  }

}
