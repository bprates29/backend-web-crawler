package com.axreng.backend.model;

import com.google.gson.Gson;

public class CrawlRequest {
    private String keyword;

    public CrawlRequest(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isValid() {
        return keyword != null && keyword.length() >= 4 && keyword.length() <= 32;
    }

    public static CrawlRequest fromJson(String json) {
        return new Gson().fromJson(json, CrawlRequest.class);
    }
}
