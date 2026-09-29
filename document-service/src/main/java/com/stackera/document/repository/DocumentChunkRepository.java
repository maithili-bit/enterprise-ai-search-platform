package com.stackera.document.repository;

import com.stackera.document.entity.DocumentChunk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, UUID> {


    // ============================================================
    // GET CHUNKS OF A DOCUMENT
    // ============================================================

    List<DocumentChunk>
    findByDocumentIdOrderByChunkIndexAsc(
            UUID documentId
    );


    // ============================================================
    // SEMANTIC SEARCH ON DOCUMENT CHUNKS
    // ============================================================

    @Query(value = """
    SELECT
        c.id,
        c.document_id AS documentId,
        c.content,
        c.chunk_index AS chunkIndex,
        1 - (
            c.embedding <=> CAST(:embedding AS vector)
        ) AS similarity
    FROM document_chunks c
    JOIN documents d
        ON d.id = c.document_id
    WHERE c.embedding IS NOT NULL
      AND d.owner_email = :ownerEmail
    ORDER BY c.embedding <=> CAST(:embedding AS vector)
    LIMIT :limit
    """, nativeQuery = true)
    List<ChunkSemanticSearchResult> findSimilarChunks(
            @Param("embedding") String embedding,
            @Param("ownerEmail") String ownerEmail,
            @Param("limit") int limit
    );
}