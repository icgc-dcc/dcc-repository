package org.icgc.dcc.repository.client.core;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.util.Lists;
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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

/**
 * Tests that each combiner returns the first non-null result in a list of entities.
 *
 *    - each tX is a new object
 *    - p1 is first position, p2 is second, p3 third and p4 is the last position
 *   +---------+------+------+------+------+--------+
 *   | seqnum  | p1   | p2   | p3   | p4   | result |
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

  @Test
  public void testExpectedPos(){
    val data = new String[]{"D", "C", "B", "A"};
    val expectedValues = new String[]{
        "A", // Seq 1
        "B", // Seq 2
        "B", // Seq 3
        "C", // Seq 4
        "C", // Seq 5
        "C", // Seq 6
        "C", // Seq 7
        "D", // Seq 8
        "D", // Seq 9
        "D", // Seq 10
        "D", // Seq 11
        "D", // Seq 12
        "D", // Seq 13
        "D", // Seq 14
        "D", // Seq 15
        null // Seq 16
    };

    val total = expectedValues.length;
    for (int seqnum = 1; seqnum<=total; seqnum++){
      val expectedValue = expectedValues[seqnum-1];
      val actualPos = generator.getExpectedPos(seqnum, NUM_POSITIONS);
      val actualIndex = actualPos - 1;
      val actualValue = actualIndex < 0 ? (String)null: data[actualIndex];
      assertThat(actualValue)
          .as(format("Failed for seqnum %s, actualPos = %s, actualValue = %s but expectedValue = %s",
              seqnum, actualPos, actualValue, expectedValue)).isEqualTo(expectedValue);
    }

  }

  @Test
  public void testGenerator(){
    val nullList = Lists.newArrayList(
        new int[]{1,2,3,4,5,6,7,16}, // Pos 1
        new int[]{1,2,3,8,9,10,11,16}, // Pos 2
        new int[]{1,4,5,8,9,12,13,16}, // Pos 3
        new int[]{2,4,6,8,10,12,14,16} // Pos 4
    );
    val nonNullList = Lists.newArrayList(
        new int[]{8,9,10,11,12,13,14,15}, // Pos 1
        new int[]{4,5,6,7,12,13,14,15}, // Pos 2
        new int[]{2,3,6,7,10,11,14,15}, // Pos 3
        new int[]{1,3,5,7,9,11,13,15} // Pos 4
    );

    for (int i=0; i<NUM_POSITIONS; i++){
      val pos = i+1;
      assertGenerator(pos, nullList.get(i), true);
      assertGenerator(pos, nonNullList.get(i), false);
    }
  }

  private void assertGenerator(int pos, int[] seqnums, boolean expectedNull){
    for (int seqnum : seqnums){
      val value = generator.getStringValue(seqnum, pos);
      if (expectedNull){
        assertThat(value).as(format("Failed at pos %s for seqnum %s and expectednull = %s", pos, seqnum, expectedNull)).isNull();
      } else {
        assertThat(value).as(format("Failed at pos %s for seqnum %s and expectednull = %s", pos, seqnum, expectedNull)).isNotNull();
      }
    }
  }

  private <T> void runTest(Function<Integer, TestCase<T>> function, Combineable<T> combiner) {
    val results = range(1,1<<NUM_POSITIONS)
        .boxed()
        .map(function)
        .collect(toList());
    for (val r : results){
      val out = combiner.combine(r.getInputs());
      assertThat(out).isEqualTo(r.getExpected());
    }
  }

  @Value
  @Builder
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
    // highest bit is the lowest position
    public String getStringValue(int seqnum, int pos){
      if (pos < 1){
        return null;
      } else {
        val isNonNull = ((seqnum >> (numPositions - pos)) & 0x1) == 1;
        if (isNonNull){
          return r.nextInt()+"something"+(count++);
        } else {
          return null;

        }
      }
    }

    // highest bit is the lowest position
    public int getExpectedPos(final int sequenceNumber, final int bw){
      int pos = bw;
      int mask = 1<<(bw-1);
      val isToBig = sequenceNumber >= 1<<bw;
      while (mask>0 && !isToBig){
        if (sequenceNumber < mask){
          mask >>= 1;
          pos--;
        } else {
          return (numPositions - pos +1);
        }
      }
      return -1;
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

  }

}
