package com.stackera.document.dto;

import java.util.UUID;

public class DocumentSearchResponse {

    private UUID id;
    private String title;
    private String fileName;
    private String fileType;
    private String ownerEmail;
    private String snippet;

    public DocumentSearchResponse() {
    }

    public DocumentSearchResponse(
            UUID id,
            String title,
            String fileName,
            String fileType,
            String ownerEmail,
            String snippet) {

        this.id = id;
        this.title = title;
        this.fileName = fileName;
        this.fileType = fileType;
        this.ownerEmail = ownerEmail;
        this.snippet = snippet;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}