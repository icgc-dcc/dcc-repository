/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.                             
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
package org.icgc.dcc.repository.song.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.sun.org.apache.bcel.internal.generic.IMUL;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.repository.core.RepositoryFileContext;
import org.icgc.dcc.repository.core.model.Repositories;
import org.icgc.dcc.repository.core.RepositoryFileProcessor;

import org.icgc.dcc.repository.core.model.Repository;
import org.icgc.dcc.repository.core.model.RepositoryFile;
import org.icgc.dcc.repository.core.model.RepositoryFile.*;
import org.icgc.dcc.repository.song.model.*;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.Collections.singletonList;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

import static org.icgc.dcc.repository.song.model.SongAnalysis.Field.*;
import static org.icgc.dcc.repository.song.model.SongAnalysis.Field.analysisId;

import static org.icgc.dcc.repository.song.model.SongFile.Field.*;
import static org.icgc.dcc.repository.song.model.SongSample.Field.*;
import static org.icgc.dcc.repository.song.model.SongSpecimen.Field.*;
import static org.icgc.dcc.repository.song.model.SongDonor.Field.*;
import static org.icgc.dcc.repository.song.model.SongSequencingRead.Field.*;
import static org.icgc.dcc.repository.song.model.SongStudy.Field.studyId;
import static org.icgc.dcc.repository.song.model.SongVariantCall.Field.variantCallingTool;

@Slf4j
public class SongProcessor extends RepositoryFileProcessor {
		@NonNull
		private final Repository repository;

		public SongProcessor(RepositoryFileContext c, Repository r) {
				super(c);
				repository = r;
		}

		public Iterable<RepositoryFile> getRepositoryFiles(Iterable<SongAnalysis> analyses) {
				val files= stream(analyses.spliterator(), false).
						flatMap(this::convertFiles).collect(Collectors.toList());

						log.info("Translating TCGC UUIDs...");
						translateUUIDs(files);

						log.info("Assigning ICGC IDs...");
						assignIds(files);


				return files;
		}

		public Stream<RepositoryFile> convertFiles(SongAnalysis analysis) {
				return analysis.getFiles().stream().
						filter(f -> !isIndexFile(f.get(fileName))).
						map(f -> convert(f, analysis));
		}

		private RepositoryFile convert(SongFile f, SongAnalysis a) {
				val id = f.get(objectId);


				val repoFile = new RepositoryFile()
						.setId(context.ensureFileId(id))
						.setObjectId(id)
						.setStudy(ImmutableList.of("PCAWG"))
						.setAccess(FileAccess.CONTROLLED)
						.setDataBundle(getDataBundle(a))
						.setAnalysisMethod(getAnalysisMethod(a))
						.setDataCategorization(getDataCategorization(a,f))
						.setReferenceGenome(ReferenceGenome.PCAWG)
						.setFileCopies(getFileCopies(a,f))
						.setDonors(getDonors(a));

				return repoFile;
		}

		List<String> getStudies(SongAnalysis a) {
				return ImmutableList.of(a.getStudy().get(studyId));
		}

		DataBundle getDataBundle(SongAnalysis a) {
				return new DataBundle().setDataBundleId(a.get(analysisId));
		}

		AnalysisMethod getAnalysisMethod(SongAnalysis a) {
				return new AnalysisMethod()
						.setAnalysisType(getAnalysisType(a))
						.setSoftware(getSoftware(a));
		}

		String getAnalysisType(SongAnalysis a) {
				if (isSequencingRead(a)) {
						return "Reference alignment";
				}
				if (isVariantCall(a)) {
						return "Variant calling";
				}

				log.warn("Invalid analysis type for " +a+", setting analysis type to null");
				return null;
		}


		String getSoftware(SongAnalysis a) {

				if (isSequencingRead(a)) {
						return getSequencingRead(a).get(alignmentTool);
				}
				if (isVariantCall(a)) {
						return getVariantCall(a).get(variantCallingTool);
				}
				log.warn("Invalid  analysis type for " + a + "setting software to null");
				return null;
		}

		boolean isVariantCall(SongAnalysis a) {
				return SongVariantCall.TYPE.equals(a.get(analysisType));
		}

		SongVariantCall getVariantCall(SongAnalysis a) {
				return (SongVariantCall) a.getExperiment();
		}

		SongSequencingRead getSequencingRead(SongAnalysis a) {
				return (SongSequencingRead) a.getExperiment();
		}

		boolean isSequencingRead(SongAnalysis a) {
				return SongSequencingRead.TYPE.equals(a.get(analysisType));
		}

		DataCategorization getDataCategorization(SongAnalysis a,SongFile f) {
				return new DataCategorization()
						.setDataType(getDataType(a,f))
						.setExperimentalStrategy(getExperimentalStrategy(a));
		}
		String getDataType(SongAnalysis a, SongFile f) {
				if (isSequencingRead(a)) {
						return RepositoryFile.DataType.ALIGNED_READS;
				}
				if (isVariantCall(a)) {
						return resolveVariantCallingDataType(f.get(fileName));
				}

				log.warn("Invalid analysis type for " + a + ", setting data type to null");

				return null;
		}
		String getExperimentalStrategy(SongAnalysis a) {
				if (isSequencingRead(a)) {
						return getSequencingRead(a).get(libraryStrategy);
				}
				if (isVariantCall(a)) {
						return RepositoryFile.ExperimentalStrategy.WGS;
				}

				log.warn("Invalid analysis type for" + a + ", setting experimentalStrategy to null");
				return null;
		}
		List<FileCopy> getFileCopies(SongAnalysis a, SongFile f) {
				return ImmutableList.of(getFileCopy(a,f));
		}

		FileCopy getFileCopy(SongAnalysis a, SongFile f) {
				val fileId = f.get(objectId);
				return new FileCopy()
						.setFileName(f.get(fileName))
						.setFileFormat(f.get(fileType))
						.setFileSize(f.getSize())
						.setFileMd5sum(f.get(fileMd5sum))
						.setLastModified(null)
						.setRepoDataBundleId(a.get(analysisId))
						.setRepoFileId(fileId)
						.setRepoType(repository.getType().getId())
						.setRepoOrg(repository.getSource().getId())
						.setRepoName(repository.getName())
						.setRepoCode(repository.getCode())
						.setRepoCountry(repository.getCountry())
						.setRepoBaseUrl(repository.getBaseUrl())
						.setRepoDataPath(repository.getType().getDataPath() + "/" + fileId)
						.setRepoMetadataPath(getRepoMetaDataPath(f))
						.setIndexFile(getIndexFile(a.getFiles()))
						;
		}
		List<Donor>	getDonors(SongAnalysis a) {
				return ImmutableList.of(getDonor(a));
		}
		Donor getDonor(SongAnalysis a) {
				val study = a.getStudy();
				val sample = a.getFirstSample();
				val donor = sample.getDonor();
				val specimen = sample.getSpecimen();
				return new Donor()
						.setStudy("PCAWG")
						.setProjectCode(study.get(studyId))
						.setPrimarySite(context.getPrimarySite(study.get(studyId)))
						.setDonorId(null)
						.setSpecimenId(null)
						.setSpecimenType(singletonList(specimen.get(specimenType)))
						.setSampleId(null)
						.setSubmittedDonorId(donor.get(donorSubmitterId))
						.setSubmittedSpecimenId(singletonList(specimen.get(specimenSubmitterId)))
						.setSubmittedSampleId(singletonList(sample.get(sampleSubmitterId)));
		}

		String getRepoMetaDataPath(SongFile f) {
				if (isXMLFile(f.get(fileName))) {
						return repository.getType().getMetadataPath() + "/" + f.get(objectId);
				}
				return null;
		}

		IndexFile getIndexFile( List<SongFile> files) {
				for (val f : files) {
						val filename = f.get(fileName);
						if (isBAIFile(filename)) {
								return createIndexFile(f, "BAI");
						} else if (isTBIFile(filename)) {
								return createIndexFile(f, "TBI");
						} else if (isIDXFile(filename)) {
								return createIndexFile(f, "IDX");
						}
				}
				return new IndexFile();
		}

		IndexFile createIndexFile(SongFile file, String fileFormat) {
				val indexFile = new RepositoryFile.IndexFile();
				val id = file.get(objectId);
				indexFile.
						setId(context.ensureFileId(id))
						.setObjectId(id)
						.setFileName(file.get(fileName))
						.setFileFormat(fileFormat)
						.setFileSize(file.getSize())
						.setFileMd5sum(file.get(fileMd5sum));
				return indexFile;
		}


		boolean hasExtension(String filename, String extension) {
				String[] suffixes = { "", ".gz", ".zip", ".b2zip" };

				val f = filename.toLowerCase();
				val ext = extension.toLowerCase();

				for (val s : suffixes) {
						if (f.endsWith(ext + s)) {
								return true;
						}

						if (f.endsWith(s + ext)) {
								return true;
						}
				}
				return false;
		}

		boolean isXMLFile(String filename) {
				return hasExtension(filename, "XML");
		}

		boolean isBAIFile(String filename) {
				return hasExtension(filename, "BAI");
		}

		boolean isTBIFile(String filename) {
				return hasExtension(filename, "TBI");
		}

		boolean isIDXFile(String filename) {
				return hasExtension(filename, "IDX");
		}

		boolean isIndexFile(String filename) {
				if (isXMLFile(filename) || isBAIFile(filename) || isIDXFile(filename) ||
						isTBIFile(filename)) {
						return true;
				}
				return false;
		}

		public static String resolveVariantCallingDataType(String fileName) {
				if (fileName.endsWith(".somatic.snv_mnv.vcf.gz")) {
						return RepositoryFile.DataType.SSM;
				} else if (fileName.endsWith(".somatic.cnv.vcf.gz")) {
						return RepositoryFile.DataType.CNSM;
				} else if (fileName.endsWith(".somatic.sv.vcf.gz")) {
						return RepositoryFile.DataType.STSM;
				} else if (fileName.endsWith(".somatic.indel.vcf.gz")) {
						return RepositoryFile.DataType.SSM;
				} else if (fileName.endsWith(".germline.snv_mnv.vcf.gz")) {
						return RepositoryFile.DataType.SGV;
				} else if (fileName.endsWith(".germline.cnv.vcf.gz")) {
						return RepositoryFile.DataType.CNGV;
				} else if (fileName.endsWith(".germline.sv.vcf.gz")) {
						return RepositoryFile.DataType.STGV;
				} else if (fileName.endsWith(".germline.indel.vcf.gz")) {
						return RepositoryFile.DataType.SGV;
				} else {
						return null;
				}
		}

}
