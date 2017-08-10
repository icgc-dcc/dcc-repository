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
package org.icgc.dcc.repository.song;

import org.icgc.dcc.repository.core.RepositoryFileContext;
import org.icgc.dcc.repository.core.model.Repositories;
import org.icgc.dcc.repository.core.model.Repository;
import org.icgc.dcc.repository.core.model.RepositoryFile;
import org.icgc.dcc.repository.core.util.GenericRepositorySourceFileImporter;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.icgc.dcc.repository.song.core.SongProcessor;
import org.icgc.dcc.repository.song.model.SongAnalysis;
import org.icgc.dcc.repository.song.reader.MockSongClient;
import org.icgc.dcc.repository.song.reader.AbstractSongClient;
import org.icgc.dcc.repository.song.reader.RealSongClient;


@Slf4j
public class SongImporter extends GenericRepositorySourceFileImporter {
  @NonNull
  private final AbstractSongClient reader;
  @NonNull
  private final SongProcessor processor;

  public SongImporter(@NonNull Repository repository, @NonNull RepositoryFileContext context,
                      AbstractSongClient reader, SongProcessor processor) {
    super(repository.getSource(), context, log);
    this.reader = reader;
    this.processor = processor;
  }

  public SongImporter(@NonNull RepositoryFileContext context) {
    super(Repositories.getSongRepository().getSource(), context, log);


    this.reader = getDefaultSongClient();
    this.processor = new SongProcessor(Repositories.getSongRepository(), context);
  }

  public AbstractSongClient getDefaultSongClient() {
    log.info("Creating Song Client for URL" + context.getSongUri().toASCIIString());
    return new RealSongClient(context.getSongUri());
  }

  @SneakyThrows
  private Iterable<SongAnalysis> readAnalysis() {
    return reader.readAnalysis();
  }

  private Iterable<RepositoryFile> processAnalysis(Iterable<SongAnalysis> analyses) {
    return processor.getRepositoryFiles(analyses);
  }

  @Override
  protected Iterable<RepositoryFile> readFiles() {
    return processAnalysis(readAnalysis());
  }

}