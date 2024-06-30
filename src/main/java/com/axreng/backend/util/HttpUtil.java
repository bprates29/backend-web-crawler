package com.axreng.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private final HttpClient client;

    public HttpUtil() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpUtil(HttpClient client) {
        this.client = client;
    }

    public CompletableFuture<String> fetchHttpContent(String urlString) {
        return fetchHttpContentWithRetries(urlString, 2, 1);
    }

    private CompletableFuture<String> fetchHttpContentWithRetries(String urlString, int maxRetries, int attempt) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(urlString))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            logger.info("Fetched content from URL: {}", urlString);
                            return response.body();
                        } else {
                            logger.error("Failed to retrieve content for URL: {}, Status Code: {}", urlString, response.statusCode());
                            return null;
                        }
                    })
                    .exceptionally(e -> {
                        logger.error("Failed to retrieve content for URL: {} on attempt {} due to error: {}", urlString, attempt, e.toString());
                        if (attempt < maxRetries && (e.getCause() instanceof HttpTimeoutException || e.getCause() instanceof IOException)) {
                            return fetchHttpContentWithRetries(urlString, maxRetries, attempt + 1).join();
                        }
                        return null;
                    });
        } catch (URISyntaxException e) {
            logger.error("Invalid URL: {}", urlString, e);
            return CompletableFuture.completedFuture(null);
        }
    }
}
