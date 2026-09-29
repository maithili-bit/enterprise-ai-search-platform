package com.stackera.document.controller;

import com.stackera.document.dto.DocumentSearchResponse;
import com.stackera.document.entity.Document;
import com.stackera.document.kafka.DocumentEventProducer;
import com.stackera.document.kafka.DocumentUploadedEvent;
import com.stackera.document.service.DocumentService;
import com.stackera.document.storage.FileStorageService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final DocumentEventProducer documentEventProducer;

    public DocumentController(
            DocumentService documentService,
            FileStorageService fileStorageService,
            DocumentEventProducer documentEventProducer) {

        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
        this.documentEventProducer = documentEventProducer;
    }

    // Create a text document
    @PostMapping
    public ResponseEntity<Document> createDocument(
            @RequestParam String title,
            @RequestParam String content,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        Document document = documentService.createDocument(
                title,
                content,
                ownerEmail
        );

        return ResponseEntity.ok(document);
    }

    // Upload a file
    @PostMapping("/upload")
    public ResponseEntity<Document> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        String storedFileName =
                fileStorageService.storeFile(file);

        Document document =
                documentService.saveUploadedFile(
                        file,
                        storedFileName,
                        ownerEmail
                );

        // Publish document uploaded event to Kafka
        DocumentUploadedEvent event =
                new DocumentUploadedEvent(
                        document.getId(),
                        document.getFileName(),
                        ownerEmail
                );

        documentEventProducer.publishDocumentUploaded(event);

        return ResponseEntity.ok(document);
    }

    // Search documents
    @GetMapping("/search")
    public ResponseEntity<List<DocumentSearchResponse>> searchDocuments(
            @RequestParam String keyword,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        List<DocumentSearchResponse> results =
                documentService.searchDocuments(
                        keyword,
                        ownerEmail
                );

        return ResponseEntity.ok(results);
    }

    // Get all documents belonging to logged-in user
    @GetMapping
    public ResponseEntity<List<Document>> getMyDocuments(
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        return ResponseEntity.ok(
                documentService.getMyDocuments(ownerEmail)
        );
    }

    // Get one document
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(
            @PathVariable UUID id,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        return ResponseEntity.ok(
                documentService.getDocumentById(
                        id,
                        ownerEmail
                )
        );
    }

    // Delete document
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable UUID id,
            Authentication authentication) {

        String ownerEmail = authentication.getName();

        documentService.deleteDocument(
                id,
                ownerEmail
        );

        return ResponseEntity.ok(
                "Document deleted successfully"
        );
    }
}