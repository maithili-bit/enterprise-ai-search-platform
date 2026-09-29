package com.stackera.document.conversation;

import com.stackera.document.dto.AskQuestionRequest;
import com.stackera.document.dto.AskQuestionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(
            ConversationService conversationService
    ) {
        this.conversationService = conversationService;
    }

    // =====================================================
    // Create a new conversation
    // =====================================================

    @PostMapping
    public ResponseEntity<Conversation> createConversation(
            @RequestBody ConversationRequest request,
            Authentication authentication
    ) {

        String ownerEmail =
                authentication.getName();

        Conversation conversation =
                conversationService.createConversation(
                        ownerEmail,
                        request.getTitle()
                );

        return ResponseEntity.ok(
                conversation
        );
    }

    // =====================================================
    // Get all conversations for current user
    // =====================================================

    @GetMapping
    public ResponseEntity<List<Conversation>> getMyConversations(
            Authentication authentication
    ) {

        String ownerEmail =
                authentication.getName();

        return ResponseEntity.ok(
                conversationService.getMyConversations(
                        ownerEmail
                )
        );
    }

    // =====================================================
    // Get one conversation
    // =====================================================

    @GetMapping("/{conversationId}")
    public ResponseEntity<Conversation> getConversation(
            @PathVariable UUID conversationId,
            Authentication authentication
    ) {

        String ownerEmail =
                authentication.getName();

        return ResponseEntity.ok(
                conversationService.getConversation(
                        conversationId,
                        ownerEmail
                )
        );
    }

    // =====================================================
    // Get messages from a conversation
    // =====================================================

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable UUID conversationId,
            Authentication authentication
    ) {

        String ownerEmail =
                authentication.getName();

        return ResponseEntity.ok(
                conversationService.getMessages(
                        conversationId,
                        ownerEmail
                )
        );
    }

    // =====================================================
    // Ask a question inside a conversation
    // =====================================================

    @PostMapping("/{conversationId}/ask")
    public ResponseEntity<AskQuestionResponse> askQuestion(
            @PathVariable UUID conversationId,
            @RequestBody AskQuestionRequest request,
            Authentication authentication
    ) {

        String ownerEmail =
                authentication.getName();

        int limit =
                request.getLimit() == null
                        ? 3
                        : request.getLimit();

        AskQuestionResponse response =
                conversationService.askQuestion(
                        conversationId,
                        request.getQuestion(),
                        ownerEmail,
                        limit
                );

        return ResponseEntity.ok(
                response
        );
    }

    // =====================================================
    // Rename a conversation
    // =====================================================

    @PatchMapping("/{conversationId}")
    public ResponseEntity<Conversation> renameConversation(
            @PathVariable UUID conversationId,
            @RequestBody RenameConversationRequest request,
            Authentication authentication
    ) {

        String ownerEmail =
                authentication.getName();

        Conversation conversation =
                conversationService.renameConversation(
                        conversationId,
                        ownerEmail,
                        request.getTitle()

                );

        return ResponseEntity.ok(
                conversation
        );
    }

    // =====================================================
    // Delete a conversation
    // =====================================================

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable UUID conversationId,
            Authentication authentication
    ) {

        String ownerEmail =
                authentication.getName();

        conversationService.deleteConversation(
                conversationId,
                ownerEmail
        );

        return ResponseEntity.noContent()
                .build();
    }

    // =====================================================
    // Create conversation request
    // =====================================================

    public static class ConversationRequest {

        private String title;

        public ConversationRequest() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    // =====================================================
    // Rename conversation request
    // =====================================================

    public static class RenameConversationRequest {

        private String title;

        public RenameConversationRequest() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}