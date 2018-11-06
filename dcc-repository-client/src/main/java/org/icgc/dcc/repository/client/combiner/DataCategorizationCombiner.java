package org.icgc.dcc.repository.client.combiner;

import lombok.val;
import org.icgc.dcc.repository.core.model.RepositoryFile.DataCategorization;

import static org.icgc.dcc.repository.client.combiner.Combineable.accumulateFirstNonNull;

public class DataCategorizationCombiner implements Combineable<DataCategorization> {
  @Override public DataCategorization merge(Iterable<DataCategorization> items) {
    val d = new DataCategorization();
    accumulateFirstNonNull(items, DataCategorization::getDataType, DataCategorization::setDataType, d);
    accumulateFirstNonNull(items, DataCategorization::getExperimentalStrategy, DataCategorization::setExperimentalStrategy, d);
    return d;
  }
}
