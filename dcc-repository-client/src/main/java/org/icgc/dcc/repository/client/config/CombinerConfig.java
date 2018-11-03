package org.icgc.dcc.repository.client.config;

import lombok.NonNull;
import org.icgc.dcc.repository.client.core.combiner.FieldCombiner;
import org.icgc.dcc.repository.client.core.combiner.FieldDef;
import org.icgc.dcc.repository.client.core.combiner.ListFieldCombiner;
import org.icgc.dcc.repository.client.core.combiner.MultiFieldCombiner;
import org.icgc.dcc.repository.core.RepositoryFileContext;
import org.icgc.dcc.repository.core.model.RepositoryFile;
import org.icgc.dcc.repository.core.model.RepositoryFile.AnalysisMethod;
import org.icgc.dcc.repository.core.model.RepositoryFile.DataBundle;
import org.icgc.dcc.repository.core.model.RepositoryFile.DataCategorization;
import org.icgc.dcc.repository.core.model.RepositoryFile.Donor;
import org.icgc.dcc.repository.core.model.RepositoryFile.FileCopy;
import org.icgc.dcc.repository.core.model.RepositoryFile.ReferenceGenome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.isNull;

@Configuration
public class CombinerConfig {

  private RepositoryFileContext context;

  @Autowired
  public CombinerConfig(@NonNull RepositoryFileContext context) {
    this.context = context;
  }

  @Bean
  public MultiFieldCombiner<ReferenceGenome> referenceGenomeAccumulator(){
    return MultiFieldCombiner.<ReferenceGenome>builder()
        .fieldCombiner(
            FieldCombiner.<ReferenceGenome, String>builder()
                .filterCriteria(buildNotNullFilterCriteria())
                .context(context)
                .fieldDef(
                    FieldDef.<ReferenceGenome, String>builder()
                        .fieldName("downloadUrl")
                        .getter(ReferenceGenome::getDownloadUrl)
                        .setter(ReferenceGenome::setDownloadUrl)
                        .build())
                .fieldDef(
                    FieldDef.<ReferenceGenome, String>builder()
                        .fieldName("genomeBuild")
                        .getter(ReferenceGenome::getGenomeBuild)
                        .setter(ReferenceGenome::setGenomeBuild)
                        .build())
                .fieldDef(
                    FieldDef.<ReferenceGenome, String>builder()
                        .fieldName("referenceName")
                        .getter(ReferenceGenome::getReferenceName)
                        .setter(ReferenceGenome::setReferenceName)
                        .build())
                .build())
        .build();
  }

  @Bean
  public MultiFieldCombiner<AnalysisMethod> analysisMethodAccumulator(){
    return MultiFieldCombiner.<AnalysisMethod>builder()
        .fieldCombiner(
            FieldCombiner.<AnalysisMethod, String>builder()
                .context(context)
                .filterCriteria(buildNotNullFilterCriteria())
                .fieldDef(
                    FieldDef.<AnalysisMethod, String>builder()
                        .fieldName("analysisType")
                        .getter(AnalysisMethod::getAnalysisType)
                        .setter(AnalysisMethod::setAnalysisType)
                        .build())
                .fieldDef(
                    FieldDef.<AnalysisMethod, String>builder()
                        .fieldName("software")
                        .getter(AnalysisMethod::getSoftware)
                        .setter(AnalysisMethod::setSoftware)
                        .build())
                .build())
        .build();
  }

  @Bean
  public MultiFieldCombiner<DataBundle> dataBundleAccumulator(){
    return MultiFieldCombiner.<DataBundle>builder()
        .fieldCombiner(
            FieldCombiner.<DataBundle, String>builder()
                .context(context)
                .filterCriteria(buildNotNullFilterCriteria())
                .fieldDef(
                    FieldDef.<DataBundle, String>builder()
                        .fieldName("dataBundleId")
                        .getter(DataBundle::getDataBundleId)
                        .setter(DataBundle::setDataBundleId)
                        .build())
                .build())
        .build();
  }

  @Bean
  public MultiFieldCombiner<DataCategorization> dataCategorizationAccumulator(){
    return MultiFieldCombiner.<DataCategorization>builder()
        .fieldCombiner(
            FieldCombiner.<DataCategorization, String>builder()
                .context(context)
                .filterCriteria(buildNotNullFilterCriteria())
                .fieldDef(
                    FieldDef.<DataCategorization, String>builder()
                        .fieldName("dataType")
                        .getter(DataCategorization::getDataType)
                        .setter(DataCategorization::setDataType)
                        .build())
                .fieldDef(
                    FieldDef.<DataCategorization, String>builder()
                        .fieldName("experimentalStrategy")
                        .getter(DataCategorization::getExperimentalStrategy)
                        .setter(DataCategorization::setExperimentalStrategy)
                        .build())
                .build())
        .build();
  }



  @Bean
  public MultiFieldCombiner<RepositoryFile> repositoryFileAccumulator(){
    return MultiFieldCombiner.<RepositoryFile>builder()
        .fieldCombiner(
            FieldCombiner.<RepositoryFile, AnalysisMethod>builder()
                .context(context)
                .filterCriteria(buildNotNullFilterCriteria())
                .fieldDef(
                    FieldDef.<RepositoryFile, AnalysisMethod>builder()
                        .fieldName("analysisMethod")
                        .getter(RepositoryFile::getAnalysisMethod)
                        .setter(RepositoryFile::setAnalysisMethod)
                        .build())
                .build())
        .fieldCombiner(
            FieldCombiner.<RepositoryFile, ReferenceGenome>builder()
                .context(context)
                .filterCriteria(buildNotNullFilterCriteria())
                .fieldDef(
                    FieldDef.<RepositoryFile, ReferenceGenome>builder()
                        .fieldName("referenceGenome")
                        .getter(RepositoryFile::getReferenceGenome)
                        .setter(RepositoryFile::setReferenceGenome)
                        .build())
                .build())
        .fieldCombiner(
            FieldCombiner.<RepositoryFile, DataBundle>builder()
                .context(context)
                .filterCriteria(buildNotNullFilterCriteria())
                .fieldDef(
                    FieldDef.<RepositoryFile, DataBundle>builder()
                        .fieldName("dataBundle")
                        .getter(RepositoryFile::getDataBundle)
                        .setter(RepositoryFile::setDataBundle)
                        .build())
                .build())
        .fieldCombiner(
            FieldCombiner.<RepositoryFile, DataCategorization>builder()
                .context(context)
                .filterCriteria(buildNotNullFilterCriteria())
                .fieldDef(
                    FieldDef.<RepositoryFile, DataCategorization>builder()
                        .fieldName("dataCategorization")
                        .getter(RepositoryFile::getDataCategorization)
                        .setter(RepositoryFile::setDataCategorization)
                        .build())
                .build())
        .fieldCombiner(
            FieldCombiner.<RepositoryFile, List<String>>builder()
                .context(context)
                .filterCriteria(buildNotNullFilterCriteria())
                .fieldDef(
                    FieldDef.<RepositoryFile, List<String>>builder()
                        .fieldName("study")
                        .getter(RepositoryFile::getStudy)
                        .setter(RepositoryFile::setStudy)
                        .build())
                .build())
        .fieldCombiner(
            FieldCombiner.<RepositoryFile, String>builder()
                .context(context)
                .filterCriteria(buildNotNullFilterCriteria())
                .fieldDef(
                    FieldDef.<RepositoryFile, String>builder()
                        .fieldName("access")
                        .getter(RepositoryFile::getAccess)
                        .setter(RepositoryFile::setAccess)
                        .build())
                .fieldDef(
                    FieldDef.<RepositoryFile, String>builder()
                        .fieldName("id")
                        .getter(RepositoryFile::getId)
                        .setter(RepositoryFile::setId)
                        .build())
                .fieldDef(
                    FieldDef.<RepositoryFile, String>builder()
                        .fieldName("objectId")
                        .getter(RepositoryFile::getObjectId)
                        .setter(RepositoryFile::setObjectId)
                        .build())
                .build())
        .fieldCombiner(
            ListFieldCombiner.<RepositoryFile, Donor>builder()
                .fieldDef(
                    FieldDef.<RepositoryFile, List<Donor>>builder()
                        .fieldName("donors")
                        .getter(RepositoryFile::getDonors)
                        .setter(RepositoryFile::setDonors)
                        .build())
                .build())
        .fieldCombiner(
            ListFieldCombiner.<RepositoryFile, FileCopy>builder()
                .fieldDef(
                    FieldDef.<RepositoryFile, List<FileCopy>>builder()
                        .fieldName("fileCopies")
                        .getter(RepositoryFile::getFileCopies)
                        .setter(RepositoryFile::setFileCopies)
                        .build())
                .build())
        .build();
  }

  private static <T> Predicate<T> buildNotNullFilterCriteria(){
    return x -> !isNull(x);
  }


}
