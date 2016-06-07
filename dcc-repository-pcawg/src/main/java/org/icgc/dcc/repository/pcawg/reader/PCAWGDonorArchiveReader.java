package org.icgc.dcc.repository.pcawg.reader;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static org.icgc.dcc.common.core.util.URLs.getUrl;
import static org.icgc.dcc.repository.pcawg.util.PCAWGArchives.PCAWG_ARCHIVE_BASE_URL;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PCAWGDonorArchiveReader {

  /**
   * JSONL (new line delimited JSON file) dump of clean, curated PCAWG donor information.
   * 
   * @see http://jsonlines.org/
   */
  public static final URL PREVIOUS_DEFAULT_PCAWG_DONOR_ARCHIVE_URL =
      getUrl(PCAWG_ARCHIVE_BASE_URL + "/data_releases/mar2016/release_mar2016.v1.jsonl");
  public static final URL DEFAULT_PCAWG_DONOR_ARCHIVE_URL =
      getUrl(PCAWG_ARCHIVE_BASE_URL + "/data_releases/latest/release_may2016.v1.jsonl");

  /**
   * Constants.
   */
  private static final ObjectMapper MAPPER = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, false);
  private static final ObjectReader READER = MAPPER.reader(ObjectNode.class);

  /**
   * State.
   */
  @NonNull
  private final URL donorArchiveUrl;

  public PCAWGDonorArchiveReader() {
    this.donorArchiveUrl = DEFAULT_PCAWG_DONOR_ARCHIVE_URL;
  }

  public Iterable<ObjectNode> readDonors() throws IOException {
    log.info("Reading donors from '{}'...", donorArchiveUrl);

    @Cleanup
    val iterator = readValues();
    return ImmutableList.copyOf(iterator);
  }

  private MappingIterator<ObjectNode> readValues() throws IOException {
    return READER.readValues(openStream());
  }

  @SneakyThrows
  private InputStream openStream() {
    return donorArchiveUrl.openStream();
  }

}