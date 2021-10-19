package org.icgc.dcc.repository.azure;

import static org.icgc.dcc.repository.core.util.RepositoryFileContexts.newLocalRepositoryFileContext;

import org.junit.Ignore;
import org.junit.Test;

import lombok.val;

public class AzureImporterTest {

  @Test
  @Ignore("For development only")
  public void testExecute() {
    val context = newLocalRepositoryFileContext();
    val azureImporter = new AzureImporter(context);
    azureImporter.execute();
  }

}
