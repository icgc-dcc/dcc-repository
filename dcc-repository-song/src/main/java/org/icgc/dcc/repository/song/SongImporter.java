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
package org.icgc.dcc.repository.song;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.icgc.dcc.repository.core.RepositoryFileContext;
import org.icgc.dcc.repository.core.model.Repository;
import org.icgc.dcc.repository.core.model.RepositoryFile;
import org.icgc.dcc.repository.core.util.GenericRepositorySourceFileImporter;
import org.icgc.dcc.repository.song.core.SongProcessor;
import org.icgc.dcc.repository.song.model.AnalysisStates;
import org.icgc.dcc.repository.song.model.SongAnalysis;
import org.icgc.dcc.repository.song.reader.SongClient;

import java.net.URL;
import java.util.Set;

@Slf4j
public abstract class SongImporter extends GenericRepositorySourceFileImporter {

  @NonNull
  private final SongClient reader;
  @NonNull
  private final SongProcessor processor;
  @NonNull
  private final Set<AnalysisStates> analysisStates;

  public SongImporter(@NonNull RepositoryFileContext context, @NonNull Repository repository,
      @NonNull URL songUrl, @NonNull String songToken, @NonNull Set<AnalysisStates> analysisStates) {
    super(repository.getSource(), context, log);
    this.reader = new SongClient(songUrl, songToken);
    this.processor = new SongProcessor(context, repository);
    this.analysisStates = analysisStates;
  }

  public SongImporter(@NonNull Repository repository, @NonNull RepositoryFileContext context,
    SongClient reader, SongProcessor processor, @NonNull Set<AnalysisStates> analysisStates) {
    super(repository.getSource(), context, log);
    this.reader = reader;
    this.processor = processor;
    this.analysisStates = analysisStates;
  }

  @Override
  protected Iterable<RepositoryFile> readFiles() {
    return processAnalysis(readAnalysis());
  }

  private Iterable<RepositoryFile> processAnalysis(Iterable<SongAnalysis> analyses) {
    return processor.getRepositoryFiles(analyses);
  }

  @SneakyThrows
  private Iterable<SongAnalysis> readAnalysis() {
    return reader.readAnalyses(analysisStates);
  }

}
