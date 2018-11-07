package org.icgc.dcc.repository.client.combiner;

import lombok.val;
import org.icgc.dcc.repository.core.model.RepositoryFile.ReferenceGenome;

import static org.icgc.dcc.repository.client.combiner.Combineable.combineFirstNonNull;

public class ReferenceGenomeCombiner implements Combineable<ReferenceGenome> {
  @Override public ReferenceGenome combine(Iterable<ReferenceGenome> items) {
    val combinedValue = new ReferenceGenome();
    combineFirstNonNull(items, ReferenceGenome::getDownloadUrl, ReferenceGenome::setDownloadUrl, combinedValue);
    combineFirstNonNull(items, ReferenceGenome::getGenomeBuild, ReferenceGenome::setGenomeBuild, combinedValue);
    combineFirstNonNull(items, ReferenceGenome::getReferenceName, ReferenceGenome::setReferenceName, combinedValue);
    return combinedValue;
  }
}
