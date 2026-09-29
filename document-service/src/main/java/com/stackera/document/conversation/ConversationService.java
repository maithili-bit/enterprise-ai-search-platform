package com.stackera.document.conversation;

import com.stackera.document.ai.RagService;
import com.stackera.document.dto.AskQuestionResponse;
import com.stackera.document.repository.ChatMessageRepository;
import com.stackera.document.repository.ConversationRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RagService ragService;
    private final ChatModel chatModel;

    public ConversationService(
            ConversationRepository conversationRepository,
            ChatMessageRepository chatMessageRepository,
            RagService ragService,
            ChatModel chatModel
    ) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.ragService = ragService;
        this.chatModel = chatModel;
    }

    // =====================================================
    // CREATE CONVERSATION
    // =====================================================

    @Transactional
    public Conversation createConversation(
            String ownerEmail,
            String title
    ) {

        boolean titleGenerated = true;

        if (title == null || title.isBlank()) {
            title = "New Conversation";
            titleGenerated = false;
        }

        Conversation conversation =
                new Conversation(
                        ownerEmail,
                        title.trim()
                );

        conversation.setTitleGenerated(titleGenerated);

        return conversationRepository.save(conversation);
    }

    // =====================================================
    // GET ALL CONVERSATIONS
    // =====================================================

    public List<Conversation> getMyConversations(
            String ownerEmail
    ) {

        return conversationRepository
                .findByOwnerEmailOrderByUpdatedAtDesc(ownerEmail);
    }

    // =====================================================
    // GET ONE CONVERSATION
    // =====================================================

    public Conversation getConversation(
            UUID conversationId,
            String ownerEmail
    ) {

        return conversationRepository
                .findByIdAndOwnerEmail(
                        conversationId,
                        ownerEmail
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Conversation not found"
                        )
                );
    }

    // =====================================================
    // GET CONVERSATION MESSAGES
    // =====================================================

    public List<ChatMessage> getMessages(
            UUID conversationId,
            String ownerEmail
    ) {

        getConversation(
                conversationId,
                ownerEmail
        );

        return chatMessageRepository
                .findByConversationIdAndOwnerEmailOrderByCreatedAtAsc(
                        conversationId,
                        ownerEmail
                );
    }

    // =====================================================
    // ASK QUESTION INSIDE CONVERSATION
    // =====================================================

    @Transactional
    public AskQuestionResponse askQuestion(
            UUID conversationId,
            String question,
            String ownerEmail,
            int limit
    ) {

        // -------------------------------------------------
        // 1. Verify conversation ownership
        // -------------------------------------------------

        Conversation conversation =
                getConversation(
                        conversationId,
                        ownerEmail
                );

        // -------------------------------------------------
        // 2. Validate question
        // -------------------------------------------------

        if (question == null || question.isBlank()) {

            throw new IllegalArgumentException(
                    "Question cannot be empty"
            );
        }

        question = question.trim();

        // -------------------------------------------------
        // 3. Get previous conversation history
        // -------------------------------------------------

        List<ChatMessage> previousMessages =
                chatMessageRepository
                        .findByConversationIdAndOwnerEmailOrderByCreatedAtAsc(
                                conversationId,
                                ownerEmail
                        );

        // -------------------------------------------------
        // 4. Generate RAG answer
        // -------------------------------------------------

        AskQuestionResponse ragResponse =
                ragService.askQuestion(
                        question,
                        ownerEmail,
                        limit,
                        previousMessages
                );

        // -------------------------------------------------
        // 5. Generate AI title for FIRST question only
        // -------------------------------------------------

        if (Boolean.FALSE.equals(
                conversation.getTitleGenerated()
        )) {

            generateConversationTitle(
                    conversation,
                    question
            );
        }

        // -------------------------------------------------
        // 6. Save USER message
        // -------------------------------------------------

        ChatMessage userMessage =
                new ChatMessage(
                        conversationId,
                        ownerEmail,
                        MessageRole.USER,
                        question
                );

        chatMessageRepository.save(
                userMessage
        );

        // -------------------------------------------------
        // 7. Save ASSISTANT response
        // -------------------------------------------------

        ChatMessage assistantMessage =
                new ChatMessage(
                        conversationId,
                        ownerEmail,
                        MessageRole.ASSISTANT,
                        ragResponse.getAnswer()
                );

        chatMessageRepository.save(
                assistantMessage
        );

        // -------------------------------------------------
        // 8. Update conversation timestamp
        // -------------------------------------------------

        conversation.setUpdatedAt(
                LocalDateTime.now()
        );

        conversationRepository.save(
                conversation
        );

        // -------------------------------------------------
        // 9. Return response
        // -------------------------------------------------

        return ragResponse;
    }

    // =====================================================
    // AI CONVERSATION TITLE GENERATION
    // =====================================================

    private void generateConversationTitle(
            Conversation conversation,
            String question
    ) {

        try {

            String prompt = """
                    Generate a very short title for a conversation
                    based on the user's question below.

                    Rules:
                    - Maximum 6 words
                    - Do not use quotation marks
                    - Do not use prefixes such as "Title:"
                    - Do not explain anything
                    - Return only the title
                    - Make it clear and professional

                    User question:
                    %s
                    """.formatted(question);

            String generatedTitle =
                    chatModel.call(prompt);

            if (generatedTitle == null ||
                    generatedTitle.isBlank()) {

                return;
            }

            generatedTitle =
                    cleanGeneratedTitle(
                            generatedTitle
                    );

            if (generatedTitle.isBlank()) {
                return;
            }

            conversation.setTitle(
                    generatedTitle
            );

            conversation.setTitleGenerated(
                    true
            );

        } catch (Exception e) {

            // -------------------------------------------------
            // Title generation must NEVER break the chat.
            // -------------------------------------------------

            System.out.println(
                    "AI title generation failed: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // CLEAN AI GENERATED TITLE
    // =====================================================

    private String cleanGeneratedTitle(
            String title
    ) {

        title = title.trim();

        if (title.startsWith("\"") &&
                title.endsWith("\"") &&
                title.length() >= 2) {

            title = title.substring(
                    1,
                    title.length() - 1
            );
        }

        if (title.startsWith("Title:")) {

            title = title.substring(
                    "Title:".length()
            ).trim();
        }

        if (title.startsWith("title:")) {

            title = title.substring(
                    "title:".length()
            ).trim();
        }

        // Remove accidental line breaks
        title = title
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();

        // Prevent excessively long titles
        if (title.length() > 100) {

            title = title.substring(
                    0,
                    100
            ).trim();
        }

        return title;
    }

    // =====================================================
    // RENAME CONVERSATION
    // =====================================================

    @Transactional
    public Conversation renameConversation(
            UUID conversationId,
            String ownerEmail,
            String title
    ) {

        System.out.println(
                "========== RENAME CONVERSATION =========="
        );

        System.out.println(
                "Conversation ID: "
                        + conversationId
        );

        System.out.println(
                "Owner email: "
                        + ownerEmail
        );

        System.out.println(
                "New title: "
                        + title
        );

        if (title == null || title.isBlank()) {

            throw new IllegalArgumentException(
                    "Conversation title cannot be empty"
            );
        }

        // -------------------------------------------------
        // Direct UUID lookup.
        // Do NOT change this back to getConversation().
        // -------------------------------------------------

        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Conversation not found for ID: "
                                                + conversationId
                                )
                        );

        System.out.println(
                "Conversation found: "
                        + conversation.getId()
        );

        System.out.println(
                "Conversation owner: "
                        + conversation.getOwnerEmail()
        );

        // -------------------------------------------------
        // Verify ownership
        // -------------------------------------------------

        if (!ownerEmail.equals(
                conversation.getOwnerEmail()
        )) {

            throw new RuntimeException(
                    "You are not allowed to rename this conversation"
            );
        }

        // -------------------------------------------------
        // Update title
        // -------------------------------------------------

        conversation.setTitle(
                title.trim()
        );

        conversation.setUpdatedAt(
                LocalDateTime.now()
        );

        // Manual rename = title is confirmed by user
        conversation.setTitleGenerated(
                true
        );

        Conversation savedConversation =
                conversationRepository.save(
                        conversation
                );

        System.out.println(
                "Conversation renamed successfully"
        );

        System.out.println(
                "=========================================="
        );

        return savedConversation;
    }

    // =====================================================
    // DELETE CONVERSATION
    // =====================================================

    @Transactional
    public void deleteConversation(
            UUID conversationId,
            String ownerEmail
    ) {

        Conversation conversation =
                getConversation(
                        conversationId,
                        ownerEmail
                );

        chatMessageRepository
                .deleteByConversationIdAndOwnerEmail(
                        conversationId,
                        ownerEmail
                );

        conversationRepository.delete(
                conversation
        );
    }
}