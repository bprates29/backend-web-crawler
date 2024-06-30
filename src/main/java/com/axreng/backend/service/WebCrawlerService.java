package com.axreng.backend.service;

import com.axreng.backend.model.CrawlState;
import com.axreng.backend.util.HttpUtil;
import com.axreng.backend.util.LinkService;
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
    private final LinkService linkService;
    private final ExecutorService executorService;

    public WebCrawlerService(HttpUtil httpUtil) {
        this.httpUtil = httpUtil;
        this.linkService = new LinkService();
        this.executorService = Executors.newFixedThreadPool(10); // Limite de 10 threads
    }

    public void startCrawling(String id, CrawlState crawlState) {
        AtomicInteger activeTasks = new AtomicInteger(0);
        crawlState.addPendingUrl(crawlState.getTargetUrl());

        while (shouldContinueCrawling(crawlState, activeTasks)) {
            List<String> urlsToProcess = getUrlsToProcess(crawlState);

            if (shouldBreakCrawling(urlsToProcess, activeTasks)) {
                break;
            }

            processUrls(id, crawlState, activeTasks, urlsToProcess);

            sleep();
        }

        finalizeCrawling(id, crawlState);
    }

    private boolean shouldContinueCrawling(CrawlState crawlState, AtomicInteger activeTasks) {
        return crawlState.hasPendingUrls() || activeTasks.get() > 0;
    }

    private List<String> getUrlsToProcess(CrawlState crawlState) {
        return crawlState.getPendingUrls().stream()
                .filter(url -> shouldProcessUrl(url, crawlState))
                .collect(Collectors.toList());
    }

    private boolean shouldBreakCrawling(List<String> urlsToProcess, AtomicInteger activeTasks) {
        return urlsToProcess.isEmpty() && activeTasks.get() == 0;
    }

    private void processUrls(String id, CrawlState crawlState, AtomicInteger activeTasks, List<String> urlsToProcess) {
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
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void finalizeCrawling(String id, CrawlState crawlState) {
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
        List<String> links = linkService.findInternalLinks(content, crawlState.getTargetUrl());
        crawlState.addAllAndValidatePendingUrl(links);
    }
}
