package com.stackera.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String ownerEmail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private String fileName;

    @Column
    private String filePath;

    @Column
    private String fileType;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector(384)")
    private float[] embedding;


    // =========================================================
    // Default constructor - required by JPA
    // =========================================================

    public Document() {
    }


    // =========================================================
    // Constructor used for basic documents
    // =========================================================

    public Document(
            String title,
            String content,
            String ownerEmail
    ) {
        this.title = title;
        this.content = content;
        this.ownerEmail = ownerEmail;
        this.createdAt = LocalDateTime.now();
    }


    // =========================================================
    // Constructor used when embedding is available
    // =========================================================

    public Document(
            String title,
            String content,
            String ownerEmail,
            String fileName,
            String filePath,
            float[] embedding
    ) {
        this.title = title;
        this.content = content;
        this.ownerEmail = ownerEmail;
        this.fileName = fileName;
        this.filePath = filePath;
        this.embedding = embedding;
        this.createdAt = LocalDateTime.now();
    }


    // =========================================================
    // Getters
    // =========================================================

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public float[] getEmbedding() {
        return embedding;
    }


    // =========================================================
    // Setters
    // =========================================================

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}