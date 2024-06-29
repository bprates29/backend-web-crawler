package com.axreng.backend.service;

import com.axreng.backend.model.CrawlState;
import com.axreng.backend.model.CrawlStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static com.axreng.backend.util.HttpUtil.fetchHttpContent;
import static com.axreng.backend.util.HttpUtil.findInternalLinks;

public class WebCrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);


    public static void startCrawling(String id, String keyword, ConcurrentMap<String, CrawlStatus> crawlStatuses) {
        CrawlState crawlState = new CrawlState(crawlStatuses);
        crawlState.addPendingUrl(crawlState.getTargetUrl());

        while (crawlState.hasPendingUrls()) {
            String url = crawlState.pollPendingUrl();
            if (shouldProcessUrl(url, crawlState)) {
                processUrl(id, keyword, crawlState, url);
            }
        }

        crawlState.setStatus(id, "done");
        logger.info("Crawling completed for ID: {}", id);
    }

    private static boolean shouldProcessUrl(String url, CrawlState crawlState) {
        return url != null && !crawlState.isVisited(url);
    }

    private static void processUrl(String id, String keyword, CrawlState crawlState, String url) {
        crawlState.addVisitedUrl(url);
        logger.info("Processing URL: {}", url);
        String content = fetchHttpContent(url);
        if (content != null) {
            processContent(id, keyword, crawlState, url, content);
        }
    }

    private static void processContent(String id, String keyword, CrawlState crawlState, String url,
                                       String content) {
        if (content.toLowerCase().contains(keyword)) {
            crawlState.addUrlToCrawlStatus(id, url);
        }
        List<String> links = findInternalLinks(content, crawlState.getTargetUrl());
        crawlState.addAllAndValidatePendingUrl(links);
    }
}
