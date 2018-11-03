package org.icgc.dcc.repository.client.core.combiner;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Builder
@Value
public class FieldDef<F, T> {

  @NonNull private final String fieldName;
  @NonNull private final Function<F, T> getter;
  @NonNull private final BiConsumer<F,T> setter;

}
