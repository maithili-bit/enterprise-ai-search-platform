package com.stackera.document.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic documentUploadedTopic() {
        return new NewTopic(
                "document-uploaded",
                1,
                (short) 1
        );
    }
}