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

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.common.core.json.Jackson.DEFAULT;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

import java.util.List;

import org.icgc.dcc.dcc.common.es.core.DocumentWriter;
import org.icgc.dcc.dcc.common.es.model.IndexDocument;
import org.icgc.dcc.repository.index.model.DocumentType;
import org.icgc.dcc.repository.index.util.TarArchiveDocumentWriter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClientURI;

import lombok.val;

public class FileTextDocumentProcessor extends DocumentProcessor {

  public FileTextDocumentProcessor(MongoClientURI mongoUri, DocumentWriter documentWriter,
      TarArchiveDocumentWriter archiveWriter) {
    super(mongoUri, () -> DocumentType.FILE_TEXT.getId(), documentWriter, archiveWriter);
  }

  @Override
  public int process() {
    return eachFile(this::addDocument);
  }

  private void addDocument(ObjectNode file) {
    val id = getId(file);
    val document = createFileText(file, id);

    addDocument(document);
  }

  private IndexDocument createFileText(ObjectNode file, String id) {
    val document = createDocument(id);

    val text = DEFAULT.createObjectNode();
    text.put("type", "file");
    text.put("id", id);
    text.put("object_id", file.path("object_id").textValue());
    text.putPOJO("file_name", arrayTextValues(file, "file_copies", "file_name"));
    text.put("data_type", file.path("data_categorization").path("data_type").textValue());
    text.putPOJO("donor_id", arrayTextValues(file, "donors", "donor_id"));
    text.putPOJO("project_code", arrayTextValues(file, "donors", "project_code"));
    text.put("data_bundle_id", file.path("data_bundle").path("data_bundle_id").textValue());

    document.getSource().put("text", text);
    return document;
  }

  private static List<String> arrayTextValues(ObjectNode objectNode, String arrayPath, String fileName) {
    return stream(objectNode.path(arrayPath)).map(element -> element.path(fileName).textValue()).collect(toList());
  }

}
