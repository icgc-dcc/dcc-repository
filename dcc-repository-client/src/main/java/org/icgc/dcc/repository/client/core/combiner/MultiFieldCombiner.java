package org.icgc.dcc.repository.client.core.combiner;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;

import java.util.List;

@Builder
@RequiredArgsConstructor
public class MultiFieldCombiner<T> implements Combiner<T> {

  @NonNull
  @Singular
  private final List<? extends Combiner<T>> fieldCombiners;

  @Override
  @SneakyThrows
  public void combineInPlace(Iterable<T> items, T object) {
    fieldCombiners.forEach(x -> x.combineInPlace(items, object));
  }

}
