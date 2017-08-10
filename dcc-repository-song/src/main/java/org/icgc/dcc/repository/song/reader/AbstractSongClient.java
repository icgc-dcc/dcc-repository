package org.icgc.dcc.repository.song.reader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.repository.song.model.SongAnalysis;

import java.net.MalformedURLException;
import java.net.URL;

@RequiredArgsConstructor
public abstract class AbstractSongClient {
    public abstract Iterable<SongAnalysis> readAnalysis();
}
