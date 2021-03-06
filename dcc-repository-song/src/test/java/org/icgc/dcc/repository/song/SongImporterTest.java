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

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.repository.core.RepositoryFileContext;
import org.icgc.dcc.repository.core.model.Repositories;
import org.icgc.dcc.repository.core.model.Repository;
import org.icgc.dcc.repository.song.core.SongProcessor;
import org.icgc.dcc.repository.song.model.AnalysisStates;
import org.icgc.dcc.repository.song.reader.MockSongClient;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.icgc.dcc.repository.core.util.RepositoryFileContexts.newLocalRepositoryFileContext;
import static org.icgc.dcc.repository.song.model.AnalysisStates.PUBLISHED;

@Ignore("For development only -- requires mongod to be running on localhost")
public class SongImporterTest {

  @Test
  public void testExecute() throws IOException {
    val context = newLocalRepositoryFileContext();
    val repository = Repositories.getCollabRepository();

    val reader = new MockSongClient("analyses.json", "studies.json");
    val processor = new SongProcessor(context, repository);
    val analysisStates = ImmutableSet.of(PUBLISHED);
    val importer = new TestImporter(repository, context, reader, processor, analysisStates);
    importer.execute();
  }

  private class TestImporter extends SongImporter {
    public TestImporter(@NonNull Repository repository, @NonNull RepositoryFileContext context,
                        MockSongClient reader, SongProcessor processor, @NonNull Set<AnalysisStates> analysisStates) {
      super(repository, context, reader, processor, analysisStates);
    }
  }

}