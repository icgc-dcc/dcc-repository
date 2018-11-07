package org.icgc.dcc.repository.client.combiner;

import lombok.val;
import org.icgc.dcc.repository.core.model.RepositoryFile.DataBundle;

import static org.icgc.dcc.repository.client.combiner.Combineable.combineFirstNonNull;

public class DataBundleCombiner implements Combineable<DataBundle> {
  @Override public DataBundle combine(Iterable<DataBundle> items) {
    val combinedValue = new DataBundle();
    combineFirstNonNull(items, DataBundle::getDataBundleId, DataBundle::setDataBundleId, combinedValue);
    return combinedValue;
  }
}
