package com.stackera.document.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentEventProducer {

    private static final String TOPIC = "document-uploaded";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DocumentEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishDocumentUploaded(DocumentUploadedEvent event) {

        try {

            String message = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    TOPIC,
                    event.getDocumentId().toString(),
                    message
            );

            System.out.println("========== KAFKA PRODUCER ==========");
            System.out.println("Topic: " + TOPIC);
            System.out.println("Document ID: " + event.getDocumentId());
            System.out.println("File: " + event.getFileName());
            System.out.println("Kafka event published successfully");
            System.out.println("====================================");

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to create Kafka event",
                    e
            );
        }
    }
}