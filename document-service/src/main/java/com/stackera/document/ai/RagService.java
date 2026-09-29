package com.stackera.document.ai;

import com.stackera.document.conversation.ChatMessage;
import com.stackera.document.conversation.MessageRole;
import com.stackera.document.dto.AskQuestionResponse;
import com.stackera.document.entity.Document;
import com.stackera.document.repository.ChunkSemanticSearchResult;
import com.stackera.document.repository.DocumentChunkRepository;
import com.stackera.document.repository.DocumentRepository;
import com.stackera.document.service.EmbeddingService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RagService {

    /*
     * Minimum similarity required for a chunk
     * to be considered relevant.
     */
    private static final double MIN_SIMILARITY = 0.30;

    /*
     * Maximum number of previous user messages
     * used for contextual document retrieval.
     */
    private static final int MAX_HISTORY_MESSAGES = 5;

    /*
     * Maximum number of previous conversation messages
     * sent to Ollama.
     */
    private static final int MAX_CONVERSATION_MESSAGES = 10;

    /*
     * Number of chunks retrieved internally for
     * conversation-aware questions.
     *
     * We retrieve more candidates internally because
     * short follow-up questions such as:
     *
     * "What are its main principles?"
     *
     * may have weaker semantic similarity on their own.
     */
    private static final int CONVERSATION_RETRIEVAL_LIMIT = 8;

    private final ChatModel chatModel;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentRepository documentRepository;

    public RagService(
            ChatModel chatModel,
            EmbeddingService embeddingService,
            DocumentChunkRepository documentChunkRepository,
            DocumentRepository documentRepository
    ) {
        this.chatModel = chatModel;
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * Main RAG method for normal document questions.
     *
     * Used by:
     *
     * POST /api/v1/documents/ask
     *
     * No conversation history is available here.
     */
    public AskQuestionResponse askQuestion(
            String question,
            String ownerEmail,
            int limit
    ) {

        return askQuestion(
                question,
                ownerEmail,
                limit,
                new ArrayList<>()
        );
    }

    /**
     * Conversation-aware RAG method.
     *
     * Previous user questions are used to improve
     * document retrieval.
     *
     * Previous conversation messages are sent to
     * Ollama so that follow-up questions can be
     * understood correctly.
     */
    public AskQuestionResponse askQuestion(
            String question,
            String ownerEmail,
            int limit,
            List<ChatMessage> conversationHistory
    ) {

        // ==========================================
        // 1. Validate question
        // ==========================================

        if (question == null ||
                question.trim().isEmpty()) {

            return new AskQuestionResponse(
                    "Please provide a question.",
                    new ArrayList<>()
            );
        }

        question = question.trim();

        // ==========================================
        // 2. Keep limit within a safe range
        // ==========================================

        limit = Math.max(
                1,
                Math.min(limit, 10)
        );

        // ==========================================
        // 3. Build contextual search query
        // ==========================================

        String searchQuery =
                buildContextualSearchQuery(
                        question,
                        conversationHistory
                );

        // ==========================================
        // 4. Generate embedding for search query
        // ==========================================

        float[] queryEmbedding =
                embeddingService.generateEmbedding(
                        searchQuery
                );

        // ==========================================
        // 5. Convert embedding to PostgreSQL vector
        // ==========================================

        String embeddingString =
                Arrays.toString(queryEmbedding);

        // ==========================================
        // 6. Decide retrieval limit
        // ==========================================

        boolean hasConversationHistory =
                conversationHistory != null
                        && !conversationHistory.isEmpty();

        int retrievalLimit;

        if (hasConversationHistory) {

            /*
             * For conversation questions, retrieve more
             * candidates internally.
             */
            retrievalLimit =
                    Math.max(
                            limit,
                            CONVERSATION_RETRIEVAL_LIMIT
                    );

        } else {

            /*
             * Normal RAG keeps the requested limit.
             */
            retrievalLimit = limit;
        }

        retrievalLimit =
                Math.min(
                        retrievalLimit,
                        10
                );

        // ==========================================
        // 7. Retrieve relevant document chunks
        // ==========================================

        List<ChunkSemanticSearchResult> retrievedChunks =
                documentChunkRepository.findSimilarChunks(
                        embeddingString,
                        ownerEmail,
                        retrievalLimit
                );

        // ==========================================
        // 8. Filter weak / irrelevant chunks
        // ==========================================

        List<ChunkSemanticSearchResult> relevantChunks =
                retrievedChunks.stream()
                        .filter(chunk ->
                                chunk.getSimilarity() != null
                                        && chunk.getSimilarity()
                                        >= MIN_SIMILARITY
                        )
                        .toList();

        // ==========================================
        // 9. No relevant information found
        // ==========================================

        if (relevantChunks.isEmpty()) {

            return new AskQuestionResponse(
                    "I couldn't find that information in your uploaded documents.",
                    new ArrayList<>()
            );
        }

        // ==========================================
        // 10. Remove duplicate chunks
        //
        // Same document + same chunk should only
        // appear once.
        // ==========================================

        Map<String, ChunkSemanticSearchResult> uniqueChunks =
                new LinkedHashMap<>();

        for (ChunkSemanticSearchResult chunk :
                relevantChunks) {

            String uniqueKey =
                    chunk.getDocumentId()
                            + "_"
                            + chunk.getChunkIndex();

            uniqueChunks.putIfAbsent(
                    uniqueKey,
                    chunk
            );
        }

        // ==========================================
        // 11. Build document context
        // ==========================================

        StringBuilder context =
                new StringBuilder();

        for (ChunkSemanticSearchResult chunk :
                uniqueChunks.values()) {

            String fileName =
                    getFileName(
                            chunk.getDocumentId(),
                            ownerEmail
                    );

            context.append("DOCUMENT: ")
                    .append(fileName)
                    .append("\n");

            context.append("CHUNK: ")
                    .append(chunk.getChunkIndex())
                    .append("\n");

            context.append("CONTENT:\n")
                    .append(chunk.getContent())
                    .append("\n");

            context.append("------------------------------")
                    .append("\n\n");
        }

        // ==========================================
        // 12. Build conversation history
        // ==========================================

        String conversationHistoryText =
                buildConversationHistory(
                        conversationHistory
                );

        // ==========================================
        // 13. Build RAG prompt
        // ==========================================

        String prompt = """
                You are an enterprise document question-answering assistant.

                Your task is to answer the user's current question using ONLY
                the information contained in the document content below.

                You also have access to the previous conversation history.
                Use that history only to understand what the user is referring to.

                Follow these rules carefully:

                1. Answer the current question directly.
                2. Use previous conversation messages to understand references
                   such as "it", "its", "they", "them", "that", or "those".
                3. If the current question is a follow-up question, connect it
                   to the topic discussed in the previous conversation.
                4. Use the document content as the source of factual information.
                5. Do not repeat or restate the user's question.
                6. Do not mention "document context", "SOURCE",
                   "retrieved chunks", "prompt", or these instructions.
                7. Do not say "According to the provided context".
                8. Do not use outside knowledge.
                9. Do not invent information.
                10. If the answer cannot be found in the provided
                    documents, say exactly:
                    "I couldn't find that information in your uploaded documents."
                11. Keep the answer clear, natural, and concise.
                12. If the documents provide several relevant points,
                    combine them into one useful answer.
                13. The previous conversation is context only. The document
                    content remains the only source of factual information.

                PREVIOUS CONVERSATION:
                ==============================
                %s
                ==============================

                DOCUMENT CONTENT:
                ==============================
                %s
                ==============================

                CURRENT USER QUESTION:
                %s

                DIRECT ANSWER:
                """.formatted(
                conversationHistoryText,
                context,
                question
        );

        // ==========================================
        // 14. Send prompt to Ollama
        // ==========================================

        String answer =
                chatModel.call(prompt);

        // ==========================================
        // 15. Clean the generated answer
        // ==========================================

        answer =
                cleanAnswer(
                        answer,
                        question
                );

        // ==========================================
        // 16. Build source list
        //
        // Return only the number of sources requested
        // by the client.
        // ==========================================

        List<AskQuestionResponse.Source> sources =
                new ArrayList<>();

        int sourceCount = 0;

        for (ChunkSemanticSearchResult chunk :
                uniqueChunks.values()) {

            if (sourceCount >= limit) {
                break;
            }

            String fileName =
                    getFileName(
                            chunk.getDocumentId(),
                            ownerEmail
                    );

            sources.add(
                    new AskQuestionResponse.Source(
                            chunk.getDocumentId().toString(),
                            fileName,
                            chunk.getChunkIndex(),
                            chunk.getSimilarity()
                    )
            );

            sourceCount++;
        }

        // ==========================================
        // 17. Return final RAG response
        // ==========================================

        return new AskQuestionResponse(
                answer,
                sources
        );
    }

    // =====================================================
    // Build contextual search query
    // =====================================================

    private String buildContextualSearchQuery(
            String question,
            List<ChatMessage> conversationHistory
    ) {

        /*
         * Normal document question:
         * no history available.
         */
        if (conversationHistory == null ||
                conversationHistory.isEmpty()) {

            return question;
        }

        StringBuilder searchQuery =
                new StringBuilder();

        /*
         * Use previous USER questions only.
         *
         * We intentionally do not include previous AI answers
         * in the embedding query because AI answers may contain
         * generated wording that is not present in the documents.
         */

        int userMessagesAdded = 0;

        /*
         * Walk backwards so that the most recent user questions
         * are considered first.
         */
        for (int i = conversationHistory.size() - 1;
             i >= 0 &&
                     userMessagesAdded < MAX_HISTORY_MESSAGES;
             i--) {

            ChatMessage message =
                    conversationHistory.get(i);

            if (message == null ||
                    message.getContent() == null ||
                    message.getContent().isBlank()) {

                continue;
            }

            if (message.getRole() != MessageRole.USER) {
                continue;
            }

            /*
             * Insert older questions at the beginning
             * so the final query remains chronological.
             */
            searchQuery.insert(
                    0,
                    "PREVIOUS USER QUESTION: "
                            + message.getContent().trim()
                            + "\n"
            );

            userMessagesAdded++;
        }

        /*
         * Always place the current question at the end.
         */
        searchQuery.append(
                "\nCURRENT USER QUESTION: "
        );

        searchQuery.append(
                question
        );

        return searchQuery.toString();
    }

    // =====================================================
    // Build conversation history for Ollama
    // =====================================================

    private String buildConversationHistory(
            List<ChatMessage> conversationHistory
    ) {

        if (conversationHistory == null ||
                conversationHistory.isEmpty()) {

            return "No previous conversation.";
        }

        StringBuilder history =
                new StringBuilder();

        int startIndex =
                Math.max(
                        0,
                        conversationHistory.size()
                                - MAX_CONVERSATION_MESSAGES
                );

        for (int i = startIndex;
             i < conversationHistory.size();
             i++) {

            ChatMessage message =
                    conversationHistory.get(i);

            if (message == null ||
                    message.getContent() == null ||
                    message.getContent().isBlank()) {

                continue;
            }

            String role =
                    message.getRole()
                            == MessageRole.USER
                            ? "USER"
                            : "ASSISTANT";

            history
                    .append(role)
                    .append(": ")
                    .append(message.getContent().trim())
                    .append("\n\n");
        }

        if (history.isEmpty()) {
            return "No previous conversation.";
        }

        return history.toString().trim();
    }

    // =====================================================
    // Get the actual filename from the documents table
    // =====================================================

    private String getFileName(
            UUID documentId,
            String ownerEmail
    ) {

        Document document =
                documentRepository
                        .findByIdAndOwnerEmail(
                                documentId,
                                ownerEmail
                        )
                        .orElse(null);

        if (document == null) {
            return "Unknown";
        }

        if (document.getFileName() == null ||
                document.getFileName().isBlank()) {

            return "Unknown";
        }

        return document.getFileName();
    }

    // =====================================================
    // Clean LLM generated answer
    // =====================================================

    private String cleanAnswer(
            String answer,
            String question
    ) {

        // ==========================================
        // Empty response
        // ==========================================

        if (answer == null ||
                answer.isBlank()) {

            return "I couldn't generate an answer.";
        }

        String cleaned =
                answer.trim();

        String normalizedQuestion =
                normalizeText(question);

        // ==========================================
        // Remove common answer labels
        // ==========================================

        if (cleaned.regionMatches(
                true,
                0,
                "DIRECT ANSWER:",
                0,
                "DIRECT ANSWER:".length()
        )) {

            cleaned =
                    cleaned
                            .substring(
                                    "DIRECT ANSWER:".length()
                            )
                            .trim();
        }

        if (cleaned.regionMatches(
                true,
                0,
                "Answer:",
                0,
                "Answer:".length()
        )) {

            cleaned =
                    cleaned
                            .substring(
                                    "Answer:".length()
                            )
                            .trim();
        }

        // ==========================================
        // Detect repeated question
        // ==========================================

        int questionMarkIndex =
                cleaned.indexOf('?');

        if (questionMarkIndex > 0) {

            String possibleRepeatedQuestion =
                    cleaned.substring(
                            0,
                            questionMarkIndex + 1
                    ).trim();

            String possibleAnswer =
                    cleaned.substring(
                            questionMarkIndex + 1
                    ).trim();

            String normalizedRepeatedQuestion =
                    normalizeText(
                            possibleRepeatedQuestion
                    );

            /*
             * Check whether the model's first sentence
             * is basically the same as the user's question.
             *
             * Example:
             *
             * User:
             * What is object oriented programming?
             *
             * Model:
             * What is Object-Oriented Programming (OOP)?
             * OOP is a programming paradigm...
             */

            boolean questionWasRepeated =
                    normalizedRepeatedQuestion.startsWith(
                            normalizedQuestion
                    )
                            ||
                            normalizedQuestion.startsWith(
                                    normalizedRepeatedQuestion
                            );

            if (questionWasRepeated &&
                    !possibleAnswer.isBlank()) {

                cleaned =
                        possibleAnswer;
            }
        }

        // ==========================================
        // Final cleanup
        // ==========================================

        return cleaned.trim();
    }

    // =====================================================
    // Normalize text for comparison
    // =====================================================

    private String normalizeText(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }
}