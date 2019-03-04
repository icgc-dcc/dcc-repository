package org.icgc.dcc.repository.collab;

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.icgc.dcc.repository.core.RepositoryFileContext;
import org.icgc.dcc.repository.core.model.Repositories;
import org.icgc.dcc.repository.song.SongImporter;

import static org.icgc.dcc.repository.song.model.AnalysisStates.SUPPRESSED;
import static org.icgc.dcc.repository.song.model.AnalysisStates.UNPUBLISHED;

public class SONGPDCImporter extends SongImporter {
    public SONGPDCImporter(@NonNull RepositoryFileContext context) {
        super(context,
                Repositories.getPdcSongRepository(),
                context.getSongPDCUrl(),
                context.getSongPDCToken(),
                ImmutableSet.of(SUPPRESSED, UNPUBLISHED));
    }
}
