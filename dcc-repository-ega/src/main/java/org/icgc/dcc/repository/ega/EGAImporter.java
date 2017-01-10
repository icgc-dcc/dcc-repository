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
package org.icgc.dcc.repository.ega;

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.repository.core.model.Repositories.getEGARepository;
import static org.icgc.dcc.repository.core.model.RepositorySource.EGA;

import java.util.stream.Stream;

import org.icgc.dcc.common.ega.client.EGAAPIClient;
import org.icgc.dcc.common.ega.client.EGAFTPClient;
import org.icgc.dcc.common.ega.dataset.EGADatasetMetaArchiveResolver;
import org.icgc.dcc.common.ega.dataset.EGADatasetMetaReader;
import org.icgc.dcc.common.ega.dump.EGADatasetDump;
import org.icgc.dcc.repository.core.RepositoryFileContext;
import org.icgc.dcc.repository.core.model.RepositoryFile;
import org.icgc.dcc.repository.core.util.GenericRepositorySourceFileImporter;
import org.icgc.dcc.repository.ega.core.EGAFileProcessor;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * @see https://www.ebi.ac.uk/ega/dacs/EGAC00001000010
 */
@Slf4j
public class EGAImporter extends GenericRepositorySourceFileImporter {

  public EGAImporter(RepositoryFileContext context) {
    super(EGA, context, log);
  }

  @Override
  @SneakyThrows
  protected Iterable<RepositoryFile> readFiles() {
    val api = createAPIClient();
    val ftp = createFTPClient();
    val datasets = readDatasets(api, ftp);
    val files = processFiles(datasets);

    return files.collect(toList());
  }

  private Stream<EGADatasetDump> readDatasets(EGAAPIClient api, EGAFTPClient ftp) {
    return new EGADatasetMetaReader(api, new EGADatasetMetaArchiveResolver(api, ftp)).readDatasets();
  }

  private Stream<RepositoryFile> processFiles(Stream<EGADatasetDump> datasets) {
    return new EGAFileProcessor(context, getEGARepository()).process(datasets);
  }

  private EGAAPIClient createAPIClient() {
    return new EGAAPIClient().login();
  }

  public EGAFTPClient createFTPClient() {
    return new EGAFTPClient();
  }

}
