package com.stackera.document.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class OllamaTestService {

    private final ChatModel chatModel;

    public OllamaTestService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String ask(String question) {
        return chatModel.call(question);
    }
}