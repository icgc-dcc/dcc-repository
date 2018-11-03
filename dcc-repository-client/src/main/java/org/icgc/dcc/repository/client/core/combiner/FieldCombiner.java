package org.icgc.dcc.repository.client.core.combiner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.val;
import org.icgc.dcc.repository.core.RepositoryFileContext;

import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.Iterables.getFirst;
import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@Builder
@RequiredArgsConstructor
public class FieldCombiner<F, T> implements Combiner<F> {

  @NonNull @Singular private final List<FieldDef<F, T>> fieldDefs;
  @NonNull private final Predicate<T> filterCriteria;
  @NonNull private final RepositoryFileContext context;

  @Override public void combineInPlace(Iterable<F> items, F object) {
    for (val fieldDef : fieldDefs){
      val orderedValues = stream(items).map(fieldDef.getGetter()).collect(toList());
      val distinctFilteredItems = filterDistinct(orderedValues);
      checkForConflicts(context, fieldDef.getFieldName(), orderedValues, distinctFilteredItems);
      fieldDef.getSetter().accept(object, getFirst(distinctFilteredItems, null));
    }
  }

  private void checkForConflicts(RepositoryFileContext context, String fieldName, Iterable<T> items, List<T> reducedValues){
    val uniqueCount = reducedValues.size();
    if (uniqueCount > 1) {
      context.reportWarning("Found %s distinct values in %s for field '%s' of items %s",
          uniqueCount, reducedValues, fieldName, items);
    }
  }

  private List<T> filterDistinct(Iterable<T> items){
    val orderedSet = Sets.<T>newLinkedHashSet();
    stream(items)
        .filter(filterCriteria)
        .forEach(orderedSet::add);
    return ImmutableList.copyOf(orderedSet);
  }

}
