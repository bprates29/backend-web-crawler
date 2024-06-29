package com.axreng.backend.service;

import com.axreng.backend.model.CrawlStatus;
import com.axreng.backend.util.IdGenerator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CrawlService {

    private final ConcurrentMap<String, CrawlStatus> crawlStatuses;

    public CrawlService() {
        this.crawlStatuses = new ConcurrentHashMap<>();
    }

    public String startCrawl(String keyword) {
        String id = IdGenerator.generateId();
        CrawlStatus status = new CrawlStatus(id, "active", keyword);
        crawlStatuses.put(id, status);

        new Thread(() -> WebCrawlerService.startCrawling(id, keyword, crawlStatuses)).start();

        return id;
    }

    public CrawlStatus getCrawlStatus(String id) {
        return crawlStatuses.get(id);
    }

    public String getAllCrawlStatus() {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(crawlStatuses.values());
        return gson.toJson(jsonElement);
    }
}
