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
package org.icgc.dcc.repository.client.core;

import static org.icgc.dcc.repository.core.model.RepositorySource.*;
import static org.icgc.dcc.repository.core.util.RepositoryFileContexts.newLocalRepositoryFileContext;

import java.io.IOException;

import org.icgc.dcc.common.core.mail.Mailer;
import org.icgc.dcc.repository.core.model.RepositorySource;
import org.junit.Ignore;
import org.junit.Test;

import lombok.val;

@Ignore("For development only")
public class RepositoryImporterTest {

  @Test
  public void testExecuteAll() throws IOException {
    val importer = createImporter();
    importer.execute();
  }

  @Test
  public void testExecuteSomeFast() throws IOException {
    val importer = createImporter(AWS, PCAWG);
    importer.execute();
  }

  @Test
  public void testSong() throws IOException {
    val importer = createImporter(COLLAB);
    importer.execute();
  }

  @Test
  public void testExecuteGDCFiltering() throws IOException {
    val importer = createImporter(GDC);
    importer.execute();
  }

  private static RepositoryImporter createImporter(RepositorySource... sources) {
    val context = newLocalRepositoryFileContext(sources);
    return new RepositoryImporter(context, Mailer.builder().enabled(false).build());
  }

}
