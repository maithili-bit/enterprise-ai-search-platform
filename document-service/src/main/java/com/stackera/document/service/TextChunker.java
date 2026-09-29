package com.stackera.document.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {

    // Number of characters in each chunk
    private static final int CHUNK_SIZE = 1000;

    // Overlap between chunks
    private static final int OVERLAP = 200;


    public List<String> splitText(String text) {

        List<String> chunks =
                new ArrayList<>();

        if (text == null ||
                text.isBlank()) {

            return chunks;
        }

        text = text.trim();

        int start = 0;

        int textLength = text.length();

        while (start < textLength) {

            int end =
                    Math.min(
                            start + CHUNK_SIZE,
                            textLength
                    );

            String chunk =
                    text.substring(
                            start,
                            end
                    ).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            // Finished
            if (end >= textLength) {
                break;
            }

            start =
                    end - OVERLAP;
        }

        return chunks;
    }
}