package com.stackera.document.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(
            name = "embedding",
            columnDefinition = "vector(384)"
    )
    private float[] embedding;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    // ============================================================
    // DEFAULT CONSTRUCTOR
    // ============================================================

    public DocumentChunk() {
        this.createdAt = LocalDateTime.now();
    }


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public DocumentChunk(
            UUID documentId,
            String content,
            Integer chunkIndex) {

        this.documentId = documentId;
        this.content = content;
        this.chunkIndex = chunkIndex;
        this.createdAt = LocalDateTime.now();
    }


    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }


    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }


    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}