package com.axreng.backend.service;

import com.axreng.backend.model.CrawlState;
import com.axreng.backend.model.CrawlStatus;
import com.axreng.backend.util.HttpUtil;
import com.axreng.backend.util.IdGenerator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrawlService {

    private final ConcurrentMap<String, CrawlStatus> crawlStatuses;
    private final WebCrawlerService webCrawlerService;
    private final ExecutorService executorService;

    public CrawlService() {
        this.crawlStatuses = new ConcurrentHashMap<>();
        this.webCrawlerService = new WebCrawlerService(new HttpUtil());
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public String startCrawl(String keyword) {
        String id = IdGenerator.generateId();
        CrawlStatus status = new CrawlStatus(id, "active", keyword);
        crawlStatuses.put(id, status);
        var crawlState = new CrawlState(crawlStatuses);

        CompletableFuture.runAsync(() -> webCrawlerService.startCrawling(id, crawlState), executorService);

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
