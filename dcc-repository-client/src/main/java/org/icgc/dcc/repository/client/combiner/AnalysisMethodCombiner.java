package org.icgc.dcc.repository.client.combiner;

import lombok.val;
import org.icgc.dcc.repository.core.model.RepositoryFile.AnalysisMethod;

import static org.icgc.dcc.repository.client.combiner.Combineable.combineFirstNonNull;

public class AnalysisMethodCombiner implements Combineable<AnalysisMethod> {
  @Override
  public AnalysisMethod combine(Iterable<AnalysisMethod> items) {
    val combinedValue = new AnalysisMethod();
    combineFirstNonNull(items, AnalysisMethod::getAnalysisType, AnalysisMethod::setAnalysisType, combinedValue);
    combineFirstNonNull(items, AnalysisMethod::getSoftware, AnalysisMethod::setSoftware, combinedValue);
    return combinedValue;
  }
}
