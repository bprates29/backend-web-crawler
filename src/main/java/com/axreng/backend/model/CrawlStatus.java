package com.axreng.backend.model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CrawlStatus {
    private String id;
    private String status;
    private List<String> urls;
    private String keyword;

    public CrawlStatus(String id, String status, String keyword) {
        this.id = id;
        this.status = status;
        this.keyword = keyword;
        this.urls = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void addUrl(String url) {
        this.urls.add(url);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
