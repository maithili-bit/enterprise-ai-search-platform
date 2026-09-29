package com.stackera.document.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DocumentEventConsumer {

    private final ObjectMapper objectMapper;

    public DocumentEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "document-uploaded",
            groupId = "document-processing-group"
    )
    public void consumeDocumentUploaded(String message) {

        try {

            DocumentUploadedEvent event =
                    objectMapper.readValue(
                            message,
                            DocumentUploadedEvent.class
                    );

            System.out.println("========== KAFKA CONSUMER ==========");
            System.out.println("Received document event");
            System.out.println("Document ID: " + event.getDocumentId());
            System.out.println("File: " + event.getFileName());
            System.out.println("Owner: " + event.getOwnerEmail());
            System.out.println("Kafka event consumed successfully");
            System.out.println("====================================");

        } catch (Exception e) {

            System.err.println(
                    "Kafka consumer error: " + e.getMessage()
            );
        }
    }
}