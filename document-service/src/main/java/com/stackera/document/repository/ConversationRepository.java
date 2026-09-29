package com.stackera.document.repository;

import com.stackera.document.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository
        extends JpaRepository<Conversation, UUID> {

    // =====================================================
    // Get all conversations of the logged-in user
    // =====================================================

    List<Conversation> findByOwnerEmailOrderByUpdatedAtDesc(
            String ownerEmail
    );

    // =====================================================
    // Find one conversation belonging to the user
    // =====================================================

    @Query(
            value = """
                    SELECT *
                    FROM conversations
                    WHERE id = :id
                      AND owner_email = :ownerEmail
                    """,
            nativeQuery = true
    )
    Optional<Conversation> findByIdAndOwnerEmail(
            @Param("id") UUID id,
            @Param("ownerEmail") String ownerEmail
    );
}