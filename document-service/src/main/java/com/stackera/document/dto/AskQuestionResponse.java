package com.stackera.document.dto;

import java.util.List;

public class AskQuestionResponse {

    private String answer;

    private List<Source> sources;


    public AskQuestionResponse(
            String answer,
            List<Source> sources) {

        this.answer = answer;
        this.sources = sources;
    }


    public String getAnswer() {
        return answer;
    }


    public List<Source> getSources() {
        return sources;
    }


    public static class Source {

        private String documentId;

        private String fileName;

        private Integer chunkIndex;

        private Double similarity;


        public Source(
                String documentId,
                String fileName,
                Integer chunkIndex,
                Double similarity) {

            this.documentId = documentId;
            this.fileName = fileName;
            this.chunkIndex = chunkIndex;
            this.similarity = similarity;
        }


        public String getDocumentId() {
            return documentId;
        }


        public String getFileName() {
            return fileName;
        }


        public Integer getChunkIndex() {
            return chunkIndex;
        }


        public Double getSimilarity() {
            return similarity;
        }
    }
}