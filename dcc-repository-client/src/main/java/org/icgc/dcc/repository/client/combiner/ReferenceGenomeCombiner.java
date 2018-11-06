package org.icgc.dcc.repository.client.combiner;

import lombok.val;
import org.icgc.dcc.repository.core.model.RepositoryFile.ReferenceGenome;

import static org.icgc.dcc.repository.client.combiner.Combineable.accumulateFirstNonNull;

public class ReferenceGenomeCombiner implements Combineable<ReferenceGenome> {
  @Override public ReferenceGenome merge(Iterable<ReferenceGenome> items) {
    val r = new ReferenceGenome();
    accumulateFirstNonNull(items, ReferenceGenome::getDownloadUrl, ReferenceGenome::setDownloadUrl, r);
    accumulateFirstNonNull(items, ReferenceGenome::getGenomeBuild, ReferenceGenome::setGenomeBuild, r);
    accumulateFirstNonNull(items, ReferenceGenome::getReferenceName, ReferenceGenome::setReferenceName, r);
    return r;
  }
}
