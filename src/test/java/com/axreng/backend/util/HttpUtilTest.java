package com.axreng.backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HttpUtilTest {

    private HttpUtil httpUtil;
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockHttpResponse;
    private static final Logger logger = LoggerFactory.getLogger(HttpUtilTest.class);

    @BeforeEach
    public void setUp() {
        mockHttpClient = mock(HttpClient.class);
        mockHttpResponse = mock(HttpResponse.class);
        httpUtil = new HttpUtil(mockHttpClient);
    }

    @Test
    void testFetchHttpContentSuccess() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn("<html>Test Content</html>");

        CompletableFuture<HttpResponse<String>> completableFuture = CompletableFuture.completedFuture(mockHttpResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(completableFuture);

        CompletableFuture<String> futureResult = httpUtil.fetchHttpContent("http://example.com");
        String result = futureResult.get();

        assertNotNull(result);
        assertEquals("<html>Test Content</html>", result);
    }

    @Test
    void testFetchHttpContentFail() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(404);

        CompletableFuture<HttpResponse<String>> completableFuture = CompletableFuture.completedFuture(mockHttpResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(completableFuture);

        CompletableFuture<String> futureResult = httpUtil.fetchHttpContent("http://example.com");
        String result = futureResult.get();

        assertNull(result);
    }
}
