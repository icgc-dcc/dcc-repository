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
package org.icgc.dcc.repository.tcga.core;

import static java.util.stream.Collectors.toMap;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.icgc.dcc.repository.tcga.model.TCGAArchiveClinicalFile;
import org.icgc.dcc.repository.tcga.model.TCGAArchiveManifestEntry;
import org.icgc.dcc.repository.tcga.model.TCGAArchivePageEntry;
import org.icgc.dcc.repository.tcga.reader.TCGAArchiveManifestReader;
import org.icgc.dcc.repository.tcga.reader.TCGAArchivePageReader;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TCGAArchiveClinicalFileProcessor {

  /**
   * Constants.
   */
  private static final Pattern CLINICAL_FILENAME_PATTERN = Pattern.compile(".*_clinical.([^.]+).xml");

  public List<TCGAArchiveClinicalFile> process(@NonNull String archiveUrl) {
    log.info("Processing archive url '{}'...", archiveUrl);
    val archiveFolderUrl = resolveArchiveFolderUrl(archiveUrl);
    val md5s = resolveArchiveFileMD5Sums(archiveFolderUrl);

    val clinicalFiles = ImmutableList.<TCGAArchiveClinicalFile> builder();
    for (val entry : TCGAArchivePageReader.readEntries(archiveFolderUrl)) {

      val clinical = matchClinicalFileName(entry.getFileName());
      if (!clinical.isPresent()) {
        continue;
      }

      val donorId = clinical.get();
      val url = archiveFolderUrl + "/" + entry.getFileName();
      val md5 = md5s.get(entry.getFileName());
      val clinicalFile =
          new TCGAArchiveClinicalFile(
              donorId, entry.getFileName(), resolveLastModified(entry),
              entry.getFileSize(), md5, url);

      clinicalFiles.add(clinicalFile);
    }

    return clinicalFiles.build();
  }

  private static long resolveLastModified(TCGAArchivePageEntry entry) {
    return entry.getLastModified().getEpochSecond();
  }

  private static Map<String, String> resolveArchiveFileMD5Sums(String archiveFolderUrl) {
    val entries = TCGAArchiveManifestReader.readEntries(archiveFolderUrl);
    val md5ByFileName = toMap(TCGAArchiveManifestEntry::getFileName, TCGAArchiveManifestEntry::getMd5);
    return stream(entries).collect(md5ByFileName);
  }

  private static String resolveArchiveFolderUrl(String archiveUrl) {
    return archiveUrl.replaceFirst(".tar.gz$", "");
  }

  private static Optional<String> matchClinicalFileName(String fileName) {
    val matcher = CLINICAL_FILENAME_PATTERN.matcher(fileName);
    return Optional.ofNullable(matcher.matches() ? matcher.group(1) : null);
  }

}
