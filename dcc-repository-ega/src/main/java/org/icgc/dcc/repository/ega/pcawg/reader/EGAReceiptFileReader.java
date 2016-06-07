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
package org.icgc.dcc.repository.ega.pcawg.reader;

import static org.icgc.dcc.repository.ega.pcawg.model.EGAReceiptFile.receiptFile;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.icgc.dcc.repository.ega.pcawg.model.EGAReceiptFile;

public class EGAReceiptFileReader extends EGAFileReader<EGAReceiptFile> {

  /**
   * Constants.
   */
  private static final Pattern RECEIPT_FILE_PATTERN = Pattern.compile(""
      // Template:
      // [projectId]/analysis_[type].[study]_[workflow]/analysis/analysis.[analysisId].submission-[timestamp]_[id].xml
      // Example :
      // LICA-FR/analysis_alignment.PCAWG_WGS_BWA/analysis/analysis.4884bd78-4002-4379-89f5-5855454ff858.submission-1455301216_2e9ffc2d-d824-449a-bb2f-b313f8fda985.xml
      + "([^/]+)" // [projectId]
      + "/analysis_"
      + "([^.]+)" // [type]
      + "\\."
      + "([^_]+)" // [study]
      + "_"
      + "([^/]+)" // [workflow]
      + "/analysis/analysis"
      + "\\."
      + "([^.]+)" // [analysisId]
      + "\\."
      + "submission-"
      + "(\\d+)" // [timestamp]
      + "_"
      + "([^.]+)" // [id]
      + "\\.xml");

  public EGAReceiptFileReader(File repoDir) {
    super(repoDir, RECEIPT_FILE_PATTERN);
  }

  @Override
  protected EGAReceiptFile createFile(Path path, Matcher matcher) {
    // Combine path metadata with file metadata
    return receiptFile()
        .projectId(matcher.group(1))
        .type(matcher.group(2))
        .study(matcher.group(3))
        .workflow(matcher.group(4))
        .analysisId(matcher.group(5))
        .timestamp(Long.parseLong(matcher.group(6)))
        .id(matcher.group(7))
        .build();
  }

}
