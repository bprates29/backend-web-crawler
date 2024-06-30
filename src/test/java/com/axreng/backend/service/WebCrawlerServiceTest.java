package com.axreng.backend.service;

import com.axreng.backend.model.CrawlState;
import com.axreng.backend.model.CrawlStatus;
import com.axreng.backend.util.HttpUtil;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashSet;

import static org.mockito.Mockito.*;

class WebCrawlerServiceTest {

    private WebCrawlerService webCrawlerService;
    private ConcurrentMap<String, CrawlStatus> crawlStatuses;
    private CrawlState crawlState;
    private HttpUtil httpUtil;

    @BeforeEach
    public void setUp() {
        crawlStatuses = new ConcurrentHashMap<>();
        httpUtil = Mockito.mock(HttpUtil.class);

        // Criando um mock para CrawlState
        crawlState = Mockito.mock(CrawlState.class);

        // Mock para a fila de URLs pendentes
        Queue<String> pendingUrls = new ConcurrentLinkedQueue<>();
        pendingUrls.add("http://axur.com");

        // Mock para o conjunto de URLs visitadas
        Set<String> visitedUrls = new HashSet<>();

        // Configurando o mock para retornar a fila de URLs pendentes e o conjunto de URLs visitadas
        when(crawlState.getPendingUrls()).thenReturn(pendingUrls);
        when(crawlState.getVisitedUrls()).thenReturn(visitedUrls);
        when(crawlState.getCrawlStatuses()).thenReturn(crawlStatuses);

        webCrawlerService = new WebCrawlerService(httpUtil);
    }

    @Test
    void testShouldProcessUrl() {
        var id = RandomString.make();
        var url = "http://axur.com";
        CrawlStatus crawlStatus = new CrawlStatus(id, "active", "keyword");
        crawlStatuses.put(id, crawlStatus);

        // Configurando mocks adicionais
        when(crawlState.getTargetUrl()).thenReturn(url);
        when(crawlState.hasPendingUrls()).thenReturn(true).thenReturn(false);
        when(crawlState.pollPendingUrl()).thenReturn(url);
        when(crawlState.isVisited(url)).thenReturn(false);
        when(httpUtil.fetchHttpContent(url)).thenReturn(CompletableFuture.completedFuture("<html>keyword</html>"));

        webCrawlerService.startCrawling(id, crawlState);

        verify(httpUtil).fetchHttpContent(url);
        verify(crawlState).addVisitedUrl(url);
        verify(crawlState).addUrlToCrawlStatus(id, url);
        verify(crawlState).setStatus(id, "done");
    }
}
