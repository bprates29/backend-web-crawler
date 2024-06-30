package com.axreng.backend.service;

import com.axreng.backend.model.CrawlState;
import com.axreng.backend.model.Status;
import com.axreng.backend.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WebCrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);
    private final HttpUtil httpUtil;

    public WebCrawlerService(HttpUtil httpUtil) {
        this.httpUtil = httpUtil;
    }

    public void startCrawling(String id, CrawlState crawlState) {
        crawlState.addPendingUrl(crawlState.getTargetUrl());

        while (crawlState.hasPendingUrls()) {
            String url = crawlState.pollPendingUrl();
            if (shouldProcessUrl(url, crawlState)) {
                processUrl(id, crawlState, url);
            }
        }

        crawlState.setStatus(id, Status.DONE);
        logger.info("Crawling completed for ID: {}", id);
    }

    private boolean shouldProcessUrl(String url, CrawlState crawlState) {
        return url != null && !crawlState.isVisited(url);
    }

    private void processUrl(String id, CrawlState crawlState, String url) {
        crawlState.addVisitedUrl(url);
        logger.info("Processing URL: {}", url);
        var content = httpUtil.fetchHttpContent(url);
        if (content != null) {
            processContent(id, crawlState, url, content);
        }
    }

    private void processContent(String id, CrawlState crawlState, String url,
                                       String content) {
        var keyword = crawlState.getCrawlStatuses().get(id).getKeyword();
        if (content.toLowerCase().contains(keyword.toLowerCase())) {
            crawlState.addUrlToCrawlStatus(id, url);
        }
        List<String> links = httpUtil.findInternalLinks(content, crawlState.getTargetUrl());
        crawlState.addAllAndValidatePendingUrl(links);
    }
}
