package com.stackera.document.dto;

public class SemanticSearchRequest {

    private String query;

    private int limit = 5;

    public SemanticSearchRequest() {
    }

    public SemanticSearchRequest(
            String query,
            int limit) {

        this.query = query;
        this.limit = limit;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}