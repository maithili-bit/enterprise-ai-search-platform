package com.stackera.document.service;

import com.stackera.document.dto.DocumentSearchResponse;
import com.stackera.document.dto.SemanticSearchResponse;
import com.stackera.document.entity.Document;
import com.stackera.document.entity.DocumentChunk;
import com.stackera.document.extractor.PdfTextExtractor;
import com.stackera.document.repository.ChunkSemanticSearchResult;
import com.stackera.document.repository.DocumentChunkRepository;
import com.stackera.document.repository.DocumentRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final EmbeddingService embeddingService;
    private final TextChunker textChunker;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            PdfTextExtractor pdfTextExtractor,
            EmbeddingService embeddingService,
            TextChunker textChunker) {

        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.pdfTextExtractor = pdfTextExtractor;
        this.embeddingService = embeddingService;
        this.textChunker = textChunker;
    }

    // ============================================================
    // CREATE DOCUMENT
    // ============================================================

    public Document createDocument(
            String title,
            String content,
            String ownerEmail) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Title cannot be empty"
            );
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Content cannot be empty"
            );
        }

        Document document = new Document(
                title,
                content,
                ownerEmail
        );

        // Generate whole-document embedding
        float[] embedding =
                embeddingService.generateEmbedding(content);

        document.setEmbedding(embedding);

        // Save document
        Document savedDocument =
                documentRepository.save(document);

        // Create and save chunks
        createAndSaveChunks(
                savedDocument.getId(),
                content
        );

        return savedDocument;
    }

    // ============================================================
    // UPLOAD PDF
    // ============================================================

    public Document saveUploadedFile(
            MultipartFile file,
            String storedFileName,
            String ownerEmail) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty"
            );
        }

        // --------------------------------------------------------
        // File path
        // --------------------------------------------------------

        String filePath =
                "uploads/" + storedFileName;

        Path pdfPath =
                Paths.get(filePath)
                        .toAbsolutePath()
                        .normalize();

        // --------------------------------------------------------
        // Extract PDF text
        // --------------------------------------------------------

        String extractedText =
                pdfTextExtractor.extractText(pdfPath);

        if (extractedText == null ||
                extractedText.isBlank()) {

            throw new IllegalArgumentException(
                    "No text could be extracted from the PDF"
            );
        }

        // --------------------------------------------------------
        // Create document
        // --------------------------------------------------------

        Document document = new Document();

        document.setTitle(
                file.getOriginalFilename()
        );

        document.setContent(
                extractedText
        );

        document.setOwnerEmail(
                ownerEmail
        );

        document.setFileName(
                file.getOriginalFilename()
        );

        document.setFilePath(
                filePath
        );

        document.setFileType(
                file.getContentType()
        );

        document.setCreatedAt(
                LocalDateTime.now()
        );

        // --------------------------------------------------------
        // Generate whole-document embedding
        // --------------------------------------------------------

        float[] embedding =
                embeddingService.generateEmbedding(
                        extractedText
                );

        document.setEmbedding(
                embedding
        );

        // --------------------------------------------------------
        // Save main document
        // --------------------------------------------------------

        Document savedDocument =
                documentRepository.save(document);

        // --------------------------------------------------------
        // Create and save chunks
        // --------------------------------------------------------

        createAndSaveChunks(
                savedDocument.getId(),
                extractedText
        );

        return savedDocument;
    }

    // ============================================================
    // CREATE CHUNKS + EMBEDDINGS
    // ============================================================

    private void createAndSaveChunks(
            UUID documentId,
            String content) {

        if (content == null || content.isBlank()) {
            return;
        }

        // Split document into chunks
        List<String> chunks =
                textChunker.splitText(content);

        System.out.println(
                "========================================"
        );

        System.out.println(
                "DOCUMENT ID: " + documentId
        );

        System.out.println(
                "TOTAL CHUNKS: " + chunks.size()
        );

        System.out.println(
                "========================================"
        );

        // Process every chunk
        for (int i = 0; i < chunks.size(); i++) {

            String chunkText = chunks.get(i);

            if (chunkText == null ||
                    chunkText.isBlank()) {
                continue;
            }

            System.out.println(
                    "Generating embedding for chunk "
                            + (i + 1)
                            + "/"
                            + chunks.size()
            );

            // Generate embedding
            float[] chunkEmbedding =
                    embeddingService.generateEmbedding(
                            chunkText
                    );

            // Create DocumentChunk
            DocumentChunk chunk =
                    new DocumentChunk(
                            documentId,
                            chunkText,
                            i
                    );

            // Set embedding
            chunk.setEmbedding(
                    chunkEmbedding
            );

            // Save chunk
            documentChunkRepository.save(chunk);
        }

        System.out.println(
                "========================================"
        );

        System.out.println(
                "ALL CHUNKS SAVED SUCCESSFULLY"
        );

        System.out.println(
                "========================================"
        );
    }

    // ============================================================
    // GET MY DOCUMENTS
    // ============================================================

    public List<Document> getMyDocuments(
            String ownerEmail) {

        return documentRepository.findByOwnerEmail(
                ownerEmail
        );
    }

    // ============================================================
    // GET DOCUMENT BY ID
    // ============================================================

    public Document getDocumentById(
            UUID id,
            String ownerEmail) {

        return documentRepository
                .findByIdAndOwnerEmail(
                        id,
                        ownerEmail
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Document not found"
                        )
                );
    }

    // ============================================================
    // DELETE DOCUMENT
    // ============================================================

    public void deleteDocument(
            UUID id,
            String ownerEmail) {

        Document document =
                documentRepository
                        .findByIdAndOwnerEmail(
                                id,
                                ownerEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"
                                )
                        );

        // Delete chunks first
        List<DocumentChunk> chunks =
                documentChunkRepository
                        .findByDocumentIdOrderByChunkIndexAsc(
                                id
                        );

        if (!chunks.isEmpty()) {
            documentChunkRepository.deleteAll(chunks);
        }

        // Delete document
        documentRepository.delete(document);
    }

    // ============================================================
    // KEYWORD SEARCH
    // ============================================================

    public List<DocumentSearchResponse> searchDocuments(
            String keyword,
            String ownerEmail) {

        List<Document> documents =
                documentRepository
                        .findByOwnerEmailAndContentContainingIgnoreCase(
                                ownerEmail,
                                keyword
                        );

        return documents.stream()
                .map(document ->
                        new DocumentSearchResponse(
                                document.getId(),
                                document.getTitle(),
                                document.getFileName(),
                                document.getFileType(),
                                document.getOwnerEmail(),
                                createSnippet(
                                        document.getContent(),
                                        keyword
                                )
                        )
                )
                .collect(Collectors.toList());
    }

    // ============================================================
    // CHUNK-BASED SEMANTIC SEARCH
    // ============================================================

    public List<SemanticSearchResponse> semanticSearch(
            String query,
            String ownerEmail,
            int limit) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Search query cannot be empty"
            );
        }

        if (limit <= 0) {
            limit = 5;
        }

        // --------------------------------------------------------
        // 1. Generate embedding for search query
        // --------------------------------------------------------

        float[] queryEmbedding =
                embeddingService.generateEmbedding(query);

        // --------------------------------------------------------
        // 2. Convert embedding to PostgreSQL vector string
        // --------------------------------------------------------

        String embeddingString =
                Arrays.toString(queryEmbedding);

        // --------------------------------------------------------
        // 3. Search document_chunks
        // --------------------------------------------------------

        List<ChunkSemanticSearchResult> searchResults =
                documentChunkRepository.findSimilarChunks(
                        embeddingString,
                        ownerEmail,
                        limit
                );

        // --------------------------------------------------------
        // 4. Convert results to API response
        // --------------------------------------------------------

        List<SemanticSearchResponse> results =
                new ArrayList<>();

        for (ChunkSemanticSearchResult result :
                searchResults) {

            UUID documentId =
                    result.getDocumentId();

            // Make sure the document belongs to
            // the currently logged-in user
            Document document =
                    documentRepository
                            .findByIdAndOwnerEmail(
                                    documentId,
                                    ownerEmail
                            )
                            .orElse(null);

            if (document == null) {
                continue;
            }

            // Use the matching chunk as the snippet
            String snippet =
                    result.getContent();

            if (snippet != null &&
                    snippet.length() > 500) {

                snippet =
                        snippet.substring(0, 500)
                                + "...";
            }

            SemanticSearchResponse response =
                    new SemanticSearchResponse(
                            document.getId(),
                            document.getTitle(),
                            document.getFileName(),
                            document.getFileType(),
                            document.getOwnerEmail(),
                            snippet,
                            result.getSimilarity()
                    );

            results.add(response);
        }

        return results;
    }

    // ============================================================
    // NORMAL SEARCH SNIPPET
    // ============================================================

    private String createSnippet(
            String content,
            String keyword) {

        if (content == null ||
                content.isBlank()) {

            return "";
        }

        if (keyword == null ||
                keyword.isBlank()) {

            return content.length() > 200
                    ? content.substring(0, 200) + "..."
                    : content;
        }

        String lowerContent =
                content.toLowerCase();

        String lowerKeyword =
                keyword.toLowerCase();

        int index =
                lowerContent.indexOf(lowerKeyword);

        if (index == -1) {

            return content.length() > 200
                    ? content.substring(0, 200) + "..."
                    : content;
        }

        int start =
                Math.max(0, index - 100);

        int end =
                Math.min(
                        content.length(),
                        index +
                                keyword.length() +
                                100
                );

        String snippet =
                content.substring(start, end)
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();

        if (start > 0) {
            snippet = "..." + snippet;
        }

        if (end < content.length()) {
            snippet = snippet + "...";
        }

        return snippet;
    }

    // ============================================================
    // SEMANTIC SEARCH SNIPPET
    // ============================================================

    private String createSemanticSnippet(
            String content) {

        if (content == null ||
                content.isBlank()) {

            return "";
        }

        String cleaned =
                content
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();

        if (cleaned.length() <= 300) {
            return cleaned;
        }

        return cleaned.substring(0, 300)
                + "...";
    }
}