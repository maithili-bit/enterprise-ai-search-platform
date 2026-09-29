package com.stackera.document.kafka;

import java.util.UUID;

public class DocumentUploadedEvent {

    private UUID documentId;
    private String fileName;
    private String ownerEmail;

    public DocumentUploadedEvent() {
    }

    public DocumentUploadedEvent(
            UUID documentId,
            String fileName,
            String ownerEmail
    ) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.ownerEmail = ownerEmail;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}