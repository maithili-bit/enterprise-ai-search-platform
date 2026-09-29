package com.stackera.document.repository;

import com.stackera.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository
        extends JpaRepository<Document, UUID> {

    List<Document> findByOwnerEmail(
            String ownerEmail
    );

    Optional<Document> findByIdAndOwnerEmail(
            UUID id,
            String ownerEmail
    );

    List<Document>
    findByOwnerEmailAndContentContainingIgnoreCase(
            String ownerEmail,
            String keyword
    );

    @Query(value = """
        SELECT
            id,
            1 - (
                embedding <=> CAST(:embedding AS vector)
            ) AS similarity
        FROM documents
        WHERE owner_email = :ownerEmail
          AND embedding IS NOT NULL
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """,
            nativeQuery = true)
    List<SemanticSearchResult> findSimilarDocuments(
            @Param("ownerEmail")
            String ownerEmail,

            @Param("embedding")
            String embedding,

            @Param("limit")
            int limit
    );
}