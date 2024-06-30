package com.axreng.backend.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class CrawlState {

    private final Queue<String> pendingUrls;
    private final Set<String> visitedUrls;
    private final ConcurrentMap<String, CrawlStatus> crawlStatuses;
    private final String targetUrl = System.getenv("BASE_URL");

    public CrawlState(ConcurrentMap<String, CrawlStatus> crawlStatuses) {
        this.pendingUrls = new LinkedList<>();
        this.visitedUrls = new HashSet<>();
        this.crawlStatuses = crawlStatuses;
    }

    public Queue<String> getPendingUrls() {
        return pendingUrls;
    }

    public Set<String> getVisitedUrls() {
        return visitedUrls;
    }

    public ConcurrentMap<String, CrawlStatus> getCrawlStatuses() {
        return crawlStatuses;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void addPendingUrl(String url) {
        pendingUrls.add(url);
    }

    public String pollPendingUrl() {
        return pendingUrls.poll();
    }

    public void addVisitedUrl(String url) {
        visitedUrls.add(url);
    }

    public boolean isVisited(String url) {
        return visitedUrls.contains(url);
    }

    public boolean hasPendingUrls() {
        return !pendingUrls.isEmpty();
    }

    public void setStatus(String id, Status status) {
        crawlStatuses.get(id).setStatus(status);
    }

    public void addUrlToCrawlStatus(String id, String url) {
        crawlStatuses.get(id).addUrl(url);
    }

    public void addAllAndValidatePendingUrl(List<String> links) {
        for (String link : links) {
            if (!isVisited(link) && !getPendingUrls().contains(link) && link.startsWith(targetUrl)) {
                addPendingUrl(link);
            }
        }
    }
}
