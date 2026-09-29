package com.stackera.document.repository;

import java.util.UUID;

public interface SemanticSearchResult {

    UUID getId();

    Double getSimilarity();
}