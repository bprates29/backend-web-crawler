package com.axreng.backend.service;

import com.axreng.backend.model.CrawlState;
import com.axreng.backend.model.CrawlStatus;
import com.axreng.backend.model.Status;
import com.axreng.backend.util.HttpUtil;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.ConcurrentMap;

import static org.mockito.Mockito.*;

class WebCrawlerServiceTest {

    private WebCrawlerService webCrawlerService;
    private ConcurrentMap<String, CrawlStatus> crawlStatuses;
    private CrawlState crawlState;
    private HttpUtil httpUtil;

    @BeforeEach
    public void setUp() {
        crawlStatuses = Mockito.mock(ConcurrentMap.class);
        crawlState = Mockito.mock(CrawlState.class);
        httpUtil = Mockito.mock(HttpUtil.class);
        webCrawlerService = new WebCrawlerService(httpUtil);
    }

    @Test
    void testShouldProcessUrl() {
        var id = RandomString.make();
        var url = "http://axur.com";
        CrawlStatus crawlStatus = new CrawlStatus(id, Status.ACTIVE, "keyword");
        when(crawlStatuses.get(id)).thenReturn(crawlStatus);
        when(crawlState.getTargetUrl()).thenReturn(url);
        when(crawlState.getCrawlStatuses()).thenReturn(crawlStatuses);
        when(crawlState.hasPendingUrls()).thenReturn(true).thenReturn(false);
        when(crawlState.pollPendingUrl()).thenReturn(url);
        when(crawlState.isVisited(url)).thenReturn(false);
        when(httpUtil.fetchHttpContent(url)).thenReturn("<html>keyword</html>");

        webCrawlerService.startCrawling(id, crawlState);

        verify(crawlState).addPendingUrl(url);
        verify(crawlState).addUrlToCrawlStatus(id, url);
        verify(crawlState).pollPendingUrl();
        verify(crawlState).setStatus(id, Status.DONE);
    }
}
