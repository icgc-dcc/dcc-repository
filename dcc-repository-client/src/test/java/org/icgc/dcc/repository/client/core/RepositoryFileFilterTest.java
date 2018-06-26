package org.icgc.dcc.repository.client.core;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.repository.core.model.RepositoryFile;
import org.icgc.dcc.repository.core.model.RepositorySource;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.repository.core.util.RepositoryFileContexts.newLocalRepositoryFileContext;

@Slf4j
public class RepositoryFileFilterTest {

  private RepositoryFileFilter repositoryFileFilter;
  private ArrayList<RepositoryFile> files;

  @Before
  public void setup() {
    repositoryFileFilter = createFileFilter();
    files = makeFiles(5);
  }

  @Test
  public void noFilesWithNoFileCopies() {
    val noFileCopies = repositoryFileFilter.filterFiles(files);
    assertThat(noFileCopies).isEmpty();
  }

  @Test
  public void filesWithCopiesSuccess() {
    val withFileCopies = files.stream().map(this::addFileCopies).collect(Collectors.toList());
    assertThat(withFileCopies.size()).isEqualTo(5);
  }

  private ArrayList<RepositoryFile> makeFiles(int numFiles) {
    ArrayList<RepositoryFile> files = new ArrayList<>();
    for (int i = 0; i < numFiles; i++) {
      val file = new RepositoryFile();
      file.setId(Integer.toString(i));
      file.setObjectId(Integer.toString(10 + i));
      file.setAccess("protected");
      files.add(file);
    }
    return files;
  }

  private static RepositoryFileFilter createFileFilter(RepositorySource... sources) {
    val context = newLocalRepositoryFileContext(sources);
    return new RepositoryFileFilter(context);
  }

  private RepositoryFile addFileCopies(RepositoryFile file) {
    file.addFileCopy();
    return file;
  }
}
