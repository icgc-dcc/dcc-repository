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
package org.icgc.dcc.repository.core.meta;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class MetadataService {

  private final MetadataClient metadataClient;

  public List<Entity> getEntities() {
    return metadataClient.findEntities();
  }

  public Optional<Entity> getEntity(String objectId) {
    try {
      return Optional.of(metadataClient.findEntity(objectId));
    } catch (EntityNotFoundException e) {
      return Optional.empty();
    }
  }

  public Optional<Entity> getIndexEntity(Entity entity) {
    val entities = metadataClient.findEntitiesByGnosId(entity.getGnosId());
    return entities
        .stream()
        .filter(e -> isIndexFile(e, entity.getFileName()))
        .findFirst();
  }

  public Optional<Entity> getXmlEntity(Entity entity) {
    val entities = metadataClient.findEntitiesByGnosId(entity.getGnosId());
    return entities
        .stream()
        .filter(e -> iXmlFile(e, entity.getGnosId()))
        .findFirst();
  }

  private static boolean isIndexFile(Entity e, String fileName) {
    return isBaiFile(e, fileName) || isTbiFile(e, fileName);
  }

  private static boolean isTbiFile(Entity e, String fileName) {
    return isMatch(e, fileName + ".tbi");
  }

  private static boolean isBaiFile(Entity e, String fileName) {
    return isMatch(e, fileName + ".bai");
  }

  private static boolean iXmlFile(Entity e, String gnosId) {
    return isMatch(e, gnosId + ".xml");
  }

  private static boolean isMatch(Entity e, String indexFileName) {
    return e.getFileName().compareToIgnoreCase(indexFileName) == 0;
  }

}
