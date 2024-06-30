package com.axreng.backend.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CrawlStatus {
    @Expose
    private String id;
    @Expose
    private String status;
    @Expose
    private List<String> urls;
    private String keyword;

    public CrawlStatus(String id, String status, String keyword) {
        this.id = id;
        this.status = status;
        this.keyword = keyword;
        this.urls = new CopyOnWriteArrayList<>();
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

    public String getKeyword() {
        return keyword;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls.clear();
        this.urls.addAll(urls);
    }

    public void addUrl(String url) {
        this.urls.add(url);
    }

    public String toJson() {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return gson.toJson(this);
    }
}
