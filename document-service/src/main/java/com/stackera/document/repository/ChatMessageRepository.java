package com.stackera.document.repository;

import com.stackera.document.conversation.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByConversationIdAndOwnerEmailOrderByCreatedAtAsc(
            UUID conversationId,
            String ownerEmail
    );

    void deleteByConversationIdAndOwnerEmail(
            UUID conversationId,
            String ownerEmail
    );
}