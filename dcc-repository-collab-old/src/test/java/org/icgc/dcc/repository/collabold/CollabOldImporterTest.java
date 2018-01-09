package org.icgc.dcc.repository.collabold;

import static org.icgc.dcc.repository.core.util.RepositoryFileContexts.newLocalRepositoryFileContext;

import org.icgc.dcc.repository.collabold.CollabOldImporter;
import org.junit.Ignore;
import org.junit.Test;

import lombok.val;

@Ignore("For development only")
public class CollabOldImporterTest {

  @Test
  public void testExecute() {
    val context = newLocalRepositoryFileContext();
    val importer = new CollabOldImporter(context);
    importer.execute();
  }

}
