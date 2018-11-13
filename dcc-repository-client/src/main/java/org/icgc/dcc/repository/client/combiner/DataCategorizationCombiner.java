package org.icgc.dcc.repository.client.combiner;

import lombok.val;
import org.icgc.dcc.repository.core.model.RepositoryFile.DataCategorization;

import static org.icgc.dcc.repository.client.combiner.Combineable.combineFirstNonNull;

public class DataCategorizationCombiner implements Combineable<DataCategorization> {
  @Override public DataCategorization combine(Iterable<DataCategorization> items) {
    val combinedValue = new DataCategorization();
    combineFirstNonNull(items, DataCategorization::getDataType, DataCategorization::setDataType, combinedValue);
    combineFirstNonNull(items, DataCategorization::getExperimentalStrategy, DataCategorization::setExperimentalStrategy, combinedValue);
    return combinedValue;
  }
}
