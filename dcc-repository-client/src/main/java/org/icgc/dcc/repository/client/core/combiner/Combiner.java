package org.icgc.dcc.repository.client.core.combiner;

public interface Combiner<T> {

  void combineInPlace(Iterable<T> iterable, T object);

}
