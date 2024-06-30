package com.axreng.backend.service;

import com.axreng.backend.model.CrawlState;
import com.axreng.backend.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WebCrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);
    private final HttpUtil httpUtil;
    private final ExecutorService executorService;

    public WebCrawlerService(HttpUtil httpUtil) {
        this.httpUtil = httpUtil;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public void startCrawling(String id, CrawlState crawlState) {
        AtomicInteger activeTasks = new AtomicInteger(0);
        crawlState.addPendingUrl(crawlState.getTargetUrl());

        while (crawlState.hasPendingUrls() || activeTasks.get() > 0) {
            List<String> urlsToProcess = crawlState.getPendingUrls().stream()
                    .filter(url -> shouldProcessUrl(url, crawlState))
                    .collect(Collectors.toList());

            if (urlsToProcess.isEmpty() && activeTasks.get() == 0) {
                break;
            }

            for (String url : urlsToProcess) {
                if (activeTasks.incrementAndGet() > 10) {
                    activeTasks.decrementAndGet();
                    break;
                }
                processUrl(id, crawlState, url)
                        .whenComplete((v, t) -> {
                            activeTasks.decrementAndGet();
                            if (t != null) {
                                logger.error("Error processing URL: {}", url, t);
                            }
                        });
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        List<String> urls = new ArrayList<>(crawlState.getCrawlStatuses().get(id).getUrls());
        Collections.sort(urls);
        crawlState.getCrawlStatuses().get(id).setUrls(urls);

        crawlState.setStatus(id, "done");
        logger.info("Crawling completed for ID: {}", id);
    }

    private boolean shouldProcessUrl(String url, CrawlState crawlState) {
        return url != null && !crawlState.isVisited(url);
    }

    private CompletableFuture<Void> processUrl(String id, CrawlState crawlState, String url) {
        crawlState.addVisitedUrl(url);
        logger.info("Processing URL: {}", url);

        return httpUtil.fetchHttpContent(url)
                .thenAcceptAsync(content -> {
                    if (content != null) {
                        processContent(id, crawlState, url, content);
                    }
                }, executorService)
                .thenRun(() -> crawlState.getPendingUrls().remove(url));
    }

    private void processContent(String id, CrawlState crawlState, String url, String content) {
        var keyword = crawlState.getCrawlStatuses().get(id).getKeyword();
        if (content.toLowerCase().contains(keyword.toLowerCase())) {
            crawlState.addUrlToCrawlStatus(id, url);
        }
        List<String> links = httpUtil.findInternalLinks(content, crawlState.getTargetUrl());
        crawlState.addAllAndValidatePendingUrl(links);
    }
}
