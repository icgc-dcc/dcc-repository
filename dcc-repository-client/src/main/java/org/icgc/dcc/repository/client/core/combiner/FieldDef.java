package org.icgc.dcc.repository.client.core.combiner;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.function.BiConsumer;
import java.util.function.Function;

//TODO: 2 types of fields: leafs and non-leafs. leafs do not
@Builder
@Value
public class FieldDef<F, T> {

  @NonNull private final String fieldName;
  @NonNull private final Function<F, T> getter;
  @NonNull private final BiConsumer<F,T> setter;
  // private final Combiner<T> combiner; //TODO this would be for a non-leaf node. For example FieldDef<RepositoryFile, AnalysisMethod>
  // for non-leaf, can just create a dummy, or skip if null

}
