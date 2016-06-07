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
package org.icgc.dcc.repository.core;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.common.core.tcga.TCGAIdentifiers.isUUID;
import static org.icgc.dcc.common.core.util.Formats.formatCount;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.icgc.dcc.repository.core.model.RepositoryProjects.getTCGAProjects;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.icgc.dcc.common.core.meta.Resolver.CodeListsResolver;
import org.icgc.dcc.common.core.meta.RestfulCodeListsResolver;
import org.icgc.dcc.common.core.util.UUID5;
import org.icgc.dcc.repository.core.meta.Entity;
import org.icgc.dcc.repository.core.meta.MetadataClient;
import org.icgc.dcc.repository.core.meta.MetadataService;
import org.icgc.dcc.repository.core.model.RepositoryFile;
import org.icgc.dcc.repository.core.model.RepositoryFile.Donor;
import org.icgc.dcc.repository.core.model.RepositoryFile.Study;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class RepositoryFileProcessor {

  /**
   * Dependencies.
   */
  @NonNull
  protected final RepositoryFileContext context;
  private final MetadataService metadataService = new MetadataService(new MetadataClient());
  private final CodeListsResolver codeListsResolver =
      new RestfulCodeListsResolver("https://submissions.dcc.icgc.org/ws");

  protected void assignStudy(Iterable<RepositoryFile> files) {
    eachFileDonor(files, donor -> {
      boolean pcawg = context.isPCAWGSubmittedDonorId(donor.getProjectCode(), donor.getSubmittedDonorId());
      if (pcawg) {
        donor.setStudy(Study.PCAWG);
      }
    });
  }

  protected void assignIds(Iterable<RepositoryFile> donorFiles) {
    val tcgaProjectCodes = resolveTCGAProjectCodes();

    for (val donorFile : donorFiles) {
      for (val donor : donorFile.getDonors()) {
        val projectCode = donor.getProjectCode();

        // Special case for TCGA who submits barcodes to DCC but UUIDs to PCAWG
        val tcga = tcgaProjectCodes.contains(donor.getProjectCode());
        val submittedDonorId =
            tcga ? donor.getOtherIdentifiers().getTcgaParticipantBarcode() : donor.getSubmittedDonorId();
        val submittedSpecimenId =
            tcga ? donor.getOtherIdentifiers().getTcgaSampleBarcode() : donor.getSubmittedSpecimenId();
        val submittedSampleId =
            tcga ? donor.getOtherIdentifiers().getTcgaAliquotBarcode() : donor.getSubmittedSampleId();

        // Get IDs or create if they don't exist. This is different than the other repos.
        donor
            .setDonorId(
                submittedDonorId == null ? null : context.ensureDonorId(submittedDonorId, projectCode))
            .setSpecimenId(
                normalizeIds(submittedSpecimenId).stream()
                    .map(s -> context.ensureSpecimenId(s, projectCode)).collect(toList()))
            .setSampleId(
                normalizeIds(submittedSampleId).stream()
                    .map(s -> context.ensureSampleId(s, projectCode)).collect(toList()));
      }
    }
  }

  protected void translateTCGAUUIDs(Iterable<RepositoryFile> donorFiles) {
    log.info("Collecting TCGA barcodes...");
    val uuids = resolveTCGAUUIDs(donorFiles);

    log.info("Translating {} TCGA barcodes to TCGA UUIDs...", formatCount(uuids));
    val barcodes = context.getTCGABarcodes(uuids);
    eachFileDonor(donorFiles, donor -> donor.getOtherIdentifiers()
        .setTcgaParticipantBarcode(barcodes.get(donor.getSubmittedDonorId()))
        .setTcgaSampleBarcode(donor.getSubmittedSpecimenId().stream().map(barcodes::get).collect(toList()))
        .setTcgaAliquotBarcode(donor.getSubmittedSampleId().stream().map(barcodes::get).collect(toList())));
  }

  protected Optional<Entity> findEntity(@NonNull String objectId) {
    return metadataService.getEntity(objectId);
  }

  protected Optional<Entity> findIndexEntity(@NonNull Entity entity) {
    return metadataService.getIndexEntity(entity);
  }

  protected Optional<Entity> findXmlEntity(@NonNull Entity entity) {
    return metadataService.getXmlEntity(entity);
  }

  protected Optional<ObjectNode> findCodeList(@NonNull String name) {
    for (val codeList : codeListsResolver.get()) {
      val currentName = codeList.get("name").textValue();
      if (currentName.equals(name)) {
        return Optional.of((ObjectNode) codeList);
      }
    }

    return Optional.empty();
  }

  protected static Set<String> resolveTCGAUUIDs(Iterable<RepositoryFile> donorFiles) {
    val tcgaProjectCodes = resolveTCGAProjectCodes();
    val uuids = Sets.<String> newHashSet();
    for (val donorFile : donorFiles) {
      for (val donor : donorFile.getDonors()) {
        val donorId = donor.getSubmittedDonorId();
        val specimenId = donor.getSubmittedSpecimenId();
        val sampleId = donor.getSubmittedSampleId();

        val tcga = tcgaProjectCodes.contains(donor.getProjectCode());
        if (!tcga) {
          continue;
        }

        if (isUUID(donorId)) {
          uuids.add(donorId);
        }
        for (val value : specimenId) {
          if (isUUID(value)) {
            uuids.add(value);
          }
        }
        for (val value : sampleId) {
          if (isUUID(value)) {
            uuids.add(value);
          }
        }
      }
    }

    return uuids;
  }

  //
  // Utilities
  //

  protected static List<String> studies(String... values) {
    return ImmutableList.copyOf(values);
  }

  protected static Stream<Donor> streamFileDonors(@NonNull Iterable<RepositoryFile> files) {
    return stream(files).flatMap(file -> file.getDonors().stream());
  }

  protected static void eachFileDonor(@NonNull Iterable<RepositoryFile> files, @NonNull Consumer<Donor> consumer) {
    streamFileDonors(files).forEach(consumer);
  }

  protected static Predicate<? super RepositoryFile> hasDonorId() {
    return (RepositoryFile file) -> file.getDonors().stream().anyMatch(donor -> donor.hasDonorId());
  }

  protected static Predicate<? super RepositoryFile> hasDataType() {
    return donorFile -> !isNullOrEmpty(donorFile.getDataCategorization().getDataType());
  }

  protected static String resolveObjectId(String... parts) {
    return UUID5.fromUTF8(UUID5.getNamespace(), Joiner.on('/').join(parts)).toString();
  }

  protected static Set<String> resolveTCGAProjectCodes() {
    return stream(getTCGAProjects()).map(project -> project.getProjectCode()).collect(toImmutableSet());
  }

  private static List<String> normalizeIds(List<String> ids) {
    if (ids == null) {
      return emptyList();
    }
  
    if (ids.contains(null)) {
      ids = Lists.newArrayList(ids);
      ids.remove(null);
    }
  
    return ids;
  }

}
