package com.axreng.backend.controller;

import com.axreng.backend.model.CrawlRequest;
import com.axreng.backend.model.CrawlStatus;
import com.axreng.backend.service.CrawlService;

import static spark.Spark.*;

public class CrawlController {
    public static final String CONTENT_TYPE = "application/json";
    private final CrawlService crawlService;

    public CrawlController() {
        this.crawlService = new CrawlService();
    }

    public void registerRoutes() {
        get("/crawl", (req, res) -> {
            res.type(CONTENT_TYPE);
            return crawlService.getAllCrawlStatus();
        });

        get("/crawl/:id", (req, res) -> {
            String id = req.params("id");
            CrawlStatus status = crawlService.getCrawlStatus(id);
            if (status == null) {
                res.status(404);
                return "ID not found";
            }
            res.type(CONTENT_TYPE);
            return status.toJson();
        });

        post("/crawl", (req, res) -> {
            CrawlRequest crawlRequest = CrawlRequest.fromJson(req.body());
            if (!crawlRequest.isValid()) {
                res.status(400);
                return "The search term must be between 4 and 32 characters.";
            }
            String id = crawlService.startCrawl(crawlRequest.getKeyword());
            res.type(CONTENT_TYPE);
            return "{\"id\": \"" + id + "\"}";
        });
    }
}
