package org.icgc.dcc.repository.client.core;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.repository.client.combiner.AnalysisMethodCombiner;
import org.icgc.dcc.repository.client.combiner.Combineable;
import org.icgc.dcc.repository.client.combiner.DataBundleCombiner;
import org.icgc.dcc.repository.client.combiner.DataCategorizationCombiner;
import org.icgc.dcc.repository.client.combiner.ReferenceGenomeCombiner;
import org.icgc.dcc.repository.core.model.RepositoryFile.AnalysisMethod;
import org.icgc.dcc.repository.core.model.RepositoryFile.DataBundle;
import org.icgc.dcc.repository.core.model.RepositoryFile.DataCategorization;
import org.icgc.dcc.repository.core.model.RepositoryFile.ReferenceGenome;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

/**
 * Tests that each combiner returns the first non-null result in a list of entities.
 *
 *    - each tX is a new object
 *    - t0 is first, t1 is second, t2 third and t3 last
 *   +---------+------+------+------+------+--------+
 *   | seqnum | t0   | t1   | t2   | t3   | result |
 *   +---------+------+------+------+------+--------+
 *   | 1       | null | null | null | A    | A      |
 *   +---------+------+------+------+------+--------+
 *   | 2       | null | null | B    | null | B      |
 *   +---------+------+------+------+------+--------+
 *   | 3       | null | null | B    | A    | B      |
 *   +---------+------+------+------+------+--------+
 *   | 4       | null | C    | null | null | C      |
 *   +---------+------+------+------+------+--------+
 *   | 5       | null | C    | null | A    | C      |
 *   +---------+------+------+------+------+--------+
 *   | 6       | null | C    | B    | null | C      |
 *   +---------+------+------+------+------+--------+
 *   | 7       | null | C    | B    | A    | C      |
 *   +---------+------+------+------+------+--------+
 *   | 8       | D    | null | null | null | D      |
 *   +---------+------+------+------+------+--------+
 *   | 9       | D    | null | null | A    | D      |
 *   +---------+------+------+------+------+--------+
 *   | 10      | D    | null | B    | null | D      |
 *   +---------+------+------+------+------+--------+
 *   | 11      | D    | null | B    | A    | D      |
 *   +---------+------+------+------+------+--------+
 *   | 12      | D    | C    | null | null | D      |
 *   +---------+------+------+------+------+--------+
 *   | 13      | D    | C    | null | A    | D      |
 *   +---------+------+------+------+------+--------+
 *   | 14      | D    | C    | B    | null | D      |
 *   +---------+------+------+------+------+--------+
 *   | 15      | D    | C    | B    | A    | D      |
 *   +---------+------+------+------+------+--------+
 *   | 16      | null | null | null | null | null   |
 *   +---------+------+------+------+------+--------+
 */

@Slf4j
public class FirstNonNullCombinerTest {

  private static final int NUM_POSITIONS = 4;

  private final Generator generator = new Generator(NUM_POSITIONS);

  @Test
  public void testAnalysisMethodCombiner() {
    runTest(generator::generateAnalysisMethodTC, new AnalysisMethodCombiner());
  }

  @Test
  public void testDataCategorizationCombiner() {
    runTest(generator::generateDataCategorizationTC, new DataCategorizationCombiner());
  }

  @Test
  public void testDataBundleCombiner() {
    runTest(generator::generateDataBundleTC, new DataBundleCombiner());
  }

  @Test
  public void testReferenceGenomeCombiner() {
    runTest(generator::generateReferenceGenomeTC, new ReferenceGenomeCombiner());
  }

  private <T> void runTest(Function<Integer, TestCase<T>> function, Combineable<T> combiner) {
    val results = range(1,1<<NUM_POSITIONS)
        .boxed()
        .map(function)
        .collect(toList());
    for (val r : results){
      val out = combiner.merge(r.getInputs());
      assertThat(out).isEqualTo(r.getExpected());
    }
  }

  @Builder
  @Value
  public static class TestCase<T>{
    @NonNull private final T expected;
    @NonNull @Singular private final List<T> inputs;
  }

  @RequiredArgsConstructor
  public static class Generator {

    private final int numPositions;
    private int count=0;
    private final Random r = new Random();

    public TestCase<AnalysisMethod> generateAnalysisMethodTC(int seqNum) {
      return generate(seqNum, AnalysisMethod::new, AnalysisMethod::setAnalysisType, AnalysisMethod::setSoftware);
    }

    public TestCase<DataBundle> generateDataBundleTC(int seqNum) {
      return generate(seqNum, DataBundle::new, DataBundle::setDataBundleId);
    }

    public TestCase<DataCategorization> generateDataCategorizationTC(int seqNum) {
      return generate(seqNum, DataCategorization::new,
          DataCategorization::setDataType, DataCategorization::setExperimentalStrategy);
    }

    public TestCase<ReferenceGenome> generateReferenceGenomeTC(int seqNum) {
      return generate(seqNum, ReferenceGenome::new,
          ReferenceGenome::setDownloadUrl, ReferenceGenome::setReferenceName, ReferenceGenome::setGenomeBuild);
    }

    private <T> TestCase<T> generate(int seqnum, Supplier<T> constructor, BiConsumer<T, String> ...consumers){
      val expectedPos = getExpectedPos(seqnum, numPositions);
      val inputs = range(1, numPositions +1)
          .boxed()
          .map(pos -> internalGenerate(seqnum, pos, constructor, consumers))
          .collect(toImmutableList());
      return TestCase.<T>builder()
          .inputs(inputs)
          .expected(inputs.get(expectedPos-1))
          .build();
    }

    private <T> T internalGenerate(int seqnum, int pos, Supplier<T> constructor, BiConsumer<T, String> ...consumers){
      T d = constructor.get();
      stream(consumers).forEach(c -> c.accept(d, getStringValue(seqnum, pos)));
      return d;
    }

    // highest bit is the lowest position
    private String getStringValue(int seqnum, int pos){
      if (pos < 1){
        return null;
      } else {
        if (((seqnum >> (numPositions - pos)) & 0x1) == 1){
          return r.nextInt()+"something"+(count++);
        } else {
          return null;

        }
      }
    }

    // highest bit is the lowest position
    private int getExpectedPos(final int sequenceNumber, final int bw){
      int pos = bw;
      int mask = 1<<(bw-1);
      while (mask>0){
        if (sequenceNumber < mask){
          mask >>= 1;
          pos--;
        } else {
          return (numPositions - pos +1);
        }
      }
      return -1;
    }

  }

}
