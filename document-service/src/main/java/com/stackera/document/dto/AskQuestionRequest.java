package com.stackera.document.dto;

public class AskQuestionRequest {


    private String question;

    private Integer limit = 3;


    public AskQuestionRequest() {
    }


    public String getQuestion() {
        return question;
    }


    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getLimit() {
        return limit;
    }


    public void setLimit(Integer limit) {
        this.limit = limit;
    }

}
