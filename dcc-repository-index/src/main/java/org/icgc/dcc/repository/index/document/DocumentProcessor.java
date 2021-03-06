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
package org.icgc.dcc.repository.index.document;

import static org.icgc.dcc.common.core.json.Jackson.DEFAULT;

import java.util.function.Consumer;

import org.icgc.dcc.dcc.common.es.core.DocumentWriter;
import org.icgc.dcc.dcc.common.es.impl.IndexDocumentType;
import org.icgc.dcc.dcc.common.es.model.IndexDocument;
import org.icgc.dcc.repository.core.model.RepositoryCollection;
import org.icgc.dcc.repository.core.util.AbstractJongoComponent;
import org.icgc.dcc.repository.index.util.TarArchiveDocumentWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClientURI;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public abstract class DocumentProcessor extends AbstractJongoComponent {

  /**
   * Configuration.
   */
  @NonNull
  private final IndexDocumentType indexType;

  /**
   * Dependencies.
   */
  @NonNull
  private final TarArchiveDocumentWriter archiveWriter;
  @NonNull
  private final DocumentWriter documentWriter;

  public DocumentProcessor(MongoClientURI mongoUri, IndexDocumentType type, DocumentWriter documentWriter,
      TarArchiveDocumentWriter archiveWriter) {
    super(mongoUri);
    this.documentWriter = documentWriter;
    this.archiveWriter = archiveWriter;
    this.indexType = type;
  }

  abstract public int process();

  protected int eachFile(Consumer<ObjectNode> consumer) {
    return eachDocument(RepositoryCollection.FILE, consumer);
  }

  protected IndexDocument createDocument(@NonNull String id) {
    return createDocument(id, DEFAULT.createObjectNode());
  }

  protected IndexDocument createDocument(@NonNull String id, @NonNull ObjectNode source) {
    return new IndexDocument(id, source, indexType);
  }

  @SneakyThrows
  protected void addDocument(IndexDocument document) {
    // Need to remove this as to not conflict with Elasticsearch
    val source = document.getSource();
    source.remove("_id");

    documentWriter.write(document);
    archiveWriter.write(document);
  }

  protected static String getId(ObjectNode file) {
    return file.get("id").textValue();
  }

  protected static ArrayNode getDonors(ObjectNode file) {
    return file.withArray("donors");
  }

  protected static String getDonorId(JsonNode donor) {
    return donor.get("donor_id").textValue();
  }

  protected static String getSubmittedDonorId(JsonNode donor) {
    return donor.get("submitted_donor_id").textValue();
  }

}
