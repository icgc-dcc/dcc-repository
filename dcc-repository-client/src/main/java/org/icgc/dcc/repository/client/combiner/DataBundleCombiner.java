package org.icgc.dcc.repository.client.combiner;

import lombok.val;
import org.icgc.dcc.repository.core.model.RepositoryFile;

import static org.icgc.dcc.repository.client.combiner.Combineable.accumulateFirstNonNull;

public class DataBundleCombiner implements Combineable<RepositoryFile.DataBundle> {
  @Override public RepositoryFile.DataBundle merge(Iterable<RepositoryFile.DataBundle> items) {
    val d = new RepositoryFile.DataBundle();
    accumulateFirstNonNull(items, RepositoryFile.DataBundle::getDataBundleId, RepositoryFile.DataBundle::setDataBundleId, d);
    return d;
  }
}
