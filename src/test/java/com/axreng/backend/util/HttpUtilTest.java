package com.axreng.backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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
        httpUtil = new HttpUtil();
        mockHttpClient = mock(HttpClient.class);
        mockHttpResponse = mock(HttpResponse.class);
    }

    @Test
    void testFetchHttpContentSuccess() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn("<html>Test Content</html>");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        HttpUtil httpUtil = new HttpUtil(mockHttpClient);

        String result = httpUtil.fetchHttpContent("http://example.com");

        assertNotNull(result);
        assertEquals("<html>Test Content</html>", result);
    }

    @Test
    void testFetchHttpContentFail() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(404);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        HttpUtil httpUtil = new HttpUtil(mockHttpClient);

        String result = httpUtil.fetchHttpContent("http://example.com");

        assertNull(result);
    }

    @Test
    void testFindInternalLinks() {
        String content = "<html>" +
                "<a href=\"/link1.html\">Link1</a>" +
                "<a href=\"http://external.com/link2.html\">Link2</a>" +
                "<a href=\"mailto:someone@example.com\">Mail</a>" +
                "<a href=\"../parent/link3.html\">Parent Link</a>" +
                "<a href=\"<invalid>\">Invalid Link</a>" +
                "<a href=\"\">Empty Link</a>" +
                "</html>";
        String baseUrl = "http://example.com";

        List<String> links = httpUtil.findInternalLinks(content, baseUrl);

        assertNotNull(links);
        assertEquals(2, links.size());

        assertTrue(links.contains("http://example.com/link1.html"));
        assertTrue(links.contains("http://external.com/link2.html"));

        assertFalse(links.contains("mailto:someone@example.com"));
        assertFalse(links.contains("http://example.com/../parent/link3.html"));
        assertFalse(links.contains("<invalid>"));
        assertFalse(links.contains(""));
    }
}
