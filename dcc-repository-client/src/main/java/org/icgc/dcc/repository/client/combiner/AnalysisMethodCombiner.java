package org.icgc.dcc.repository.client.combiner;

import lombok.val;
import org.icgc.dcc.repository.core.model.RepositoryFile;

import static org.icgc.dcc.repository.client.combiner.Combineable.accumulateFirstNonNull;

public class AnalysisMethodCombiner implements Combineable<RepositoryFile.AnalysisMethod> {
  @Override
  public RepositoryFile.AnalysisMethod merge(Iterable<RepositoryFile.AnalysisMethod> items) {
    val a = new RepositoryFile.AnalysisMethod();
    accumulateFirstNonNull(items, RepositoryFile.AnalysisMethod::getAnalysisType, RepositoryFile.AnalysisMethod::setAnalysisType, a);
    accumulateFirstNonNull(items, RepositoryFile.AnalysisMethod::getSoftware, RepositoryFile.AnalysisMethod::setSoftware, a);
    return a;
  }
}
