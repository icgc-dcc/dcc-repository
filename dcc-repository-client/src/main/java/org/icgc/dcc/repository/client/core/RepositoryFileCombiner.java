/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
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
package org.icgc.dcc.repository.client.core;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.icgc.dcc.repository.core.util.RepositoryFiles.inPCAWGOrder;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.icgc.dcc.repository.core.RepositoryFileContext;
import org.icgc.dcc.repository.core.model.RepositoryFile;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RepositoryFileCombiner {

  /**
   * Dependencies.
   */
  @NonNull
  private final RepositoryFileContext context;

  public Iterable<RepositoryFile> combineFiles(Iterable<Set<RepositoryFile>> files) {
    log.info("Lazily combining files...");
    return new Iterable<RepositoryFile>() {

      @Override
      public Iterator<RepositoryFile> iterator() {

        Iterator<Set<RepositoryFile>> delegate = files.iterator();

        return new Iterator<RepositoryFile>() {

          @Override
          public RepositoryFile next() {
            // Main call
            return combineFiles(delegate.next());
          }

          @Override
          public boolean hasNext() {
            return delegate.hasNext();
          }

        };
      }

    };
  }

  private RepositoryFile combineFiles(Set<RepositoryFile> files) {
    // TODO: Add checks for all root fields and very least add reporting for inconsistent fields, if not fail processing
    val prioritizedFiles = prioritize(files);

    val combinedFile = new RepositoryFile();

    //
    // Select One
    //

    val ids = get(prioritizedFiles, RepositoryFile::getId);
    analyzeField(files, "id", ids);
    combinedFile.setId(combineField(ids));

    val fileIds = get(prioritizedFiles, RepositoryFile::getFileId);
    analyzeField(files, "fileId", fileIds);
    combinedFile.setFileId(combineField(fileIds));

    val studies = get(prioritizedFiles, RepositoryFile::getStudy);
    analyzeField(files, "study", studies);
    combinedFile.setStudy(combineField(studies));

    val accesses = get(prioritizedFiles, RepositoryFile::getAccess);
    analyzeField(files, "access", accesses);
    combinedFile.setAccess(combineField(accesses));

    val dataBundles = get(prioritizedFiles, RepositoryFile::getDataBundle);
    analyzeField(files, "dataBundle", dataBundles);
    combinedFile.setDataBundle(combineField(dataBundles));

    val analysisMethods = get(prioritizedFiles, RepositoryFile::getAnalysisMethod);
    analyzeField(files, "analysisMethod", analysisMethods);
    combinedFile.setAnalysisMethod(combineField(analysisMethods));

    val dataCategorizations = get(prioritizedFiles, RepositoryFile::getDataCategorization);
    analyzeField(files, "dataCategorization", dataCategorizations);
    combinedFile.setDataCategorization(combineField(dataCategorizations));

    val referenceGenomes = get(prioritizedFiles, RepositoryFile::getReferenceGenome);
    analyzeField(files, "referenceGenome", referenceGenomes);
    combinedFile.setReferenceGenome(combineField(referenceGenomes));

    //
    // Combine All
    //

    val fileCopies = getAll(prioritizedFiles, RepositoryFile::getFileCopies);
    combinedFile.setFileCopies(fileCopies);

    val donors = getAll(prioritizedFiles, RepositoryFile::getDonors);
    combinedFile.setDonors(donors);

    return combinedFile;
  }

  private <T> void analyzeField(Set<RepositoryFile> files, String fieldName, Collection<T> values) {
    val uniqueCount = values.stream().filter(value -> value != null).distinct().count();
    if (uniqueCount > 1) {
      context.reportWarning("Found %s distinct values in %s for field '%s' of files %s",
          uniqueCount, values, fieldName, files);
    }
  }

  private static <T> T combineField(Collection<T> values) {
    // Try to find first non-null
    return values.stream().filter(value -> value != null).findFirst().orElse(null);
  }

  private static Set<RepositoryFile> prioritize(Set<RepositoryFile> files) {
    // Prioritize PCAWG ahead of others since it carries the most information
    return files.stream().sorted(inPCAWGOrder()).collect(toSet());
  }

  private static <T> List<T> get(Collection<RepositoryFile> files, Function<RepositoryFile, T> getter) {
    return files.stream().map(getter).collect(toList());
  }

  private static <T> List<T> getAll(Collection<RepositoryFile> files, Function<RepositoryFile, List<T>> getter) {
    return files.stream().flatMap(file -> getter.apply(file).stream()).collect(toList());
  }

}
