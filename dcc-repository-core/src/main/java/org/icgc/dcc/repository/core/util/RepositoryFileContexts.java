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
package org.icgc.dcc.repository.core.util;

import static lombok.AccessLevel.PRIVATE;

import java.util.Set;

import org.icgc.dcc.repository.core.RepositoryFileContext;
import org.icgc.dcc.repository.core.RepositoryFileContextBuilder;
import org.icgc.dcc.repository.core.RepositoryIdResolver;
import org.icgc.dcc.repository.core.model.RepositorySource;

import com.google.common.collect.ImmutableSet;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class RepositoryFileContexts {

  public static RepositoryFileContext newLocalRepositoryFileContext(RepositorySource... sources) {
    return newLocalRepositoryFileContext(
        sources.length == 0 ? RepositorySource.all() : ImmutableSet.copyOf(sources));
  }

  public static RepositoryFileContext newLocalRepositoryFileContext(Set<RepositorySource> sources) {
    return RepositoryFileContextBuilder
        .builder()
        .realIds(false)
        .sources(sources)
        .indexAlias("dev-icgc-repository")
        .pcawgIdResolver(newEmptyIdResovler())
        .build();
  }

  private static RepositoryIdResolver newEmptyIdResovler() {
    return () -> ImmutableSet.of();
  }

}
