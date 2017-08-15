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

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.repository.core.RepositoryFileContext;
import org.icgc.dcc.repository.core.model.Repository;
import org.icgc.dcc.repository.core.model.RepositoryFile;
import org.icgc.dcc.repository.song.model.*;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

import static org.icgc.dcc.repository.song.model.SongAnalysis.Field.*;
import static org.icgc.dcc.repository.song.model.SongAnalysis.Field.analysisId;

import static org.icgc.dcc.repository.song.model.SongStudy.Field.*;
import static org.icgc.dcc.repository.song.model.SongFile.Field.*;
import static org.icgc.dcc.repository.song.model.SongSample.Field.*;
import static org.icgc.dcc.repository.song.model.SongSpecimen.Field.*;
import static org.icgc.dcc.repository.song.model.SongDonor.Field.*;
import static org.icgc.dcc.repository.song.model.SongSequencingRead.Field.*;
import static org.icgc.dcc.repository.song.model.SongStudy.Field.studyId;
import static org.icgc.dcc.repository.song.model.SongVariantCall.Field.variantCallingTool;

@RequiredArgsConstructor
public class SongProcessor {
		@NonNull
		private final Repository repository;
		@NonNull
		private final RepositoryFileContext context;

		public Iterable<RepositoryFile> getRepositoryFiles(Iterable<SongAnalysis> analyses) {
				return stream(analyses.spliterator(), false).
						flatMap(this::convertFiles).collect(Collectors.toList());
		}

		public Stream<RepositoryFile> convertFiles(SongAnalysis analysis) {
				return analysis.getFiles().stream().
						filter(f -> !isIndexFile(f.get(fileName))).
						map(f -> convert(f, analysis));
		}

		private RepositoryFile convert(SongFile f, SongAnalysis a) {
				val fileId = f.get(objectId);
				val study = a.getStudy();

				val sample = a.getFirstSample();
				val donor = sample.getDonor();

				val specimen = sample.getSpecimen();

				val repoFile = new RepositoryFile()
						.setId(context.ensureFileId(fileId))
						.setObjectId(fileId)
						.setStudy(ImmutableList.of(study.get(name)))
						.setAccess(RepositoryFile.FileAccess.CONTROLLED);
				val type = a.get(analysisType);

				repoFile.getDataBundle().setDataBundleId(a.get(analysisId));

				val analysisMethod = repoFile.getAnalysisMethod();
				analysisMethod.setAnalysisType(type);

				val experiment = a.getExperiment();

				String software = null;
				if (type.equals(SongVariantCall.TYPE)) {
						val variantCall = (SongVariantCall) experiment;
						software = variantCall.get(variantCallingTool);
				} else if (type.equals(SongSequencingRead.TYPE)) {
						val sequencingRead = (SongSequencingRead) experiment;

						software = sequencingRead.get(alignmentTool);

						repoFile.getDataCategorization().
								setExperimentalStrategy(sequencingRead.get(libraryStrategy));

						repoFile.getReferenceGenome().setReferenceName(sequencingRead.get(referenceGenome));
				}
				analysisMethod.setSoftware(software);

				val fileCopy = repoFile.addFileCopy()
						.setFileName(f.get(fileName))
						.setFileFormat(f.get(fileType))
						.setFileSize(f.getSize())
						.setFileMd5sum(f.get(fileMd5sum))
						.setRepoDataBundleId(a.get(analysisId))
						.setRepoFileId(fileId)
						.setRepoType(repository.getType().getId())
						.setRepoOrg(repository.getSource().getId())
						.setRepoName(repository.getName())
						.setRepoCode(repository.getCode())
						.setRepoCountry(repository.getCountry())
						.setRepoBaseUrl(repository.getBaseUrl())
						.setRepoDataPath(repository.getType().getDataPath() + "/" + fileId);

				val indexFile = getIndexFile(fileCopy, a.getFiles());
				if (indexFile != null) {
						fileCopy.setIndexFile(indexFile);
				}

				repoFile.addDonor()
						.setStudy(study.get(name))
						.setProjectCode(study.get(studyId))
						.setDonorId(donor.get(donorId))
						.setSpecimenId(singletonList(specimen.get(specimenId)))
						.setSpecimenType(singletonList(specimen.get(specimenType)))
						.setSampleId(singletonList(sample.get(sampleId)))
						.setSubmittedDonorId(donor.get(donorSubmitterId))
						.setSubmittedSpecimenId(singletonList(specimen.get(specimenSubmitterId)))
						.setSubmittedSampleId(singletonList(sample.get(sampleSubmitterId)));

				return repoFile;
		}

		RepositoryFile.IndexFile getIndexFile(RepositoryFile.FileCopy fileCopy, List<SongFile> files) {
				for (val f : files) {
						val filename = f.get(fileName);
						if (isXMLFile(filename)) {
								fileCopy.setRepoMetadataPath(
										repository.getType().getMetadataPath() + "/" + f.get(objectId));
						} else if (isBAIFile(filename)) {
								return createIndexFile(f, "BAI");
						} else if (isTBIFile(filename)) {
								return createIndexFile(f, "TBI");
						} else if (isIDXFile(filename)) {
								return createIndexFile(f, "IDX");
						}
				}
				return null;
		}

		RepositoryFile.IndexFile createIndexFile(SongFile file, String fileFormat) {
				val indexFile = new RepositoryFile.IndexFile();
				val fileId = file.get(objectId);
				indexFile.
						setId(context.ensureFileId(fileId))
						.setObjectId(fileId)
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
}
