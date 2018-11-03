package org.icgc.dcc.repository.client.core.combiner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@Builder
@RequiredArgsConstructor
public class ListFieldCombiner<F, T>  implements Combiner<F> {

  @NonNull @Singular private final List<FieldDef<F, List<T>>> fieldDefs;
  @NonNull private final Predicate<T> filterCriteria;

  @Override public void combineInPlace(Iterable<F> items, F object) {
    for (val fieldDef : fieldDefs){
      val distinctFilteredItems = distinctFilter(items, fieldDef);
      fieldDef.getSetter().accept(object, distinctFilteredItems);
    }
  }

  // TODO: add distinct-by option. for example, distinct by donor id
  private List<T> distinctFilter(Iterable<F> items, FieldDef<F, List<T>> fieldDef) {
    val orderedSet = Sets.<T>newLinkedHashSet();
    stream(items)
        .map(fieldDef.getGetter())
        .flatMap(Collection::stream)
        .filter(filterCriteria)
        .forEach(orderedSet::add);
    return ImmutableList.copyOf(orderedSet);
  }

}
