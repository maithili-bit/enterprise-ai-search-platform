package com.stackera.document.repository;

import java.util.UUID;

public interface ChunkSemanticSearchResult {

    UUID getId();

    UUID getDocumentId();

    String getContent();

    Integer getChunkIndex();

    Double getSimilarity();
}