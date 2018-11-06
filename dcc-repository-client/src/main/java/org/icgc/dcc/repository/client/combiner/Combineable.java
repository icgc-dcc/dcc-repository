package org.icgc.dcc.repository.client.combiner;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

public interface Combineable<T>{

  static <T,R> void accumulateFirstNonNull(Iterable<T> items, Function<T, R> getter, BiConsumer<T, R> setter, T accumulator){
    setter.accept(accumulator,
        stream(items)
            .map(getter)
            .filter(x -> !isNull(x))
            .findFirst()
            .orElse(null));
  }

  T merge(Iterable<T> items);
}
