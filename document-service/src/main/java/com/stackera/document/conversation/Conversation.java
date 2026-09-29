package com.stackera.document.conversation;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String ownerEmail;

    @Column(nullable = false)
    private String title;

    /*
     * false = title can be automatically generated
     * true  = title has already been generated or manually chosen
     *
     * Boolean is intentionally used instead of primitive boolean
     * so existing database rows created before this field existed
     * can safely contain NULL.
     */
    @Column
    private Boolean titleGenerated;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Conversation() {
    }

    public Conversation(
            String ownerEmail,
            String title
    ) {

        this.ownerEmail = ownerEmail;
        this.title = title;
        this.titleGenerated = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Getters
    // =====================================================

    public UUID getId() {
        return id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getTitleGenerated() {
        return titleGenerated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // =====================================================
    // Setters
    // =====================================================

    public void setOwnerEmail(
            String ownerEmail
    ) {

        this.ownerEmail = ownerEmail;
    }

    public void setTitle(
            String title
    ) {

        this.title = title;
    }

    public void setTitleGenerated(
            Boolean titleGenerated
    ) {

        this.titleGenerated = titleGenerated;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {

        this.updatedAt = updatedAt;
    }
}