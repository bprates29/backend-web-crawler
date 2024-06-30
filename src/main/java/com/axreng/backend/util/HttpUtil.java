package com.axreng.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private final HttpClient client;
    private final String HREF_PATTERN = "href\\s*=\\s*\"([^\"]*)\"";

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

    public List<String> findInternalLinks(String content, String baseUrl) {
        var links = new ArrayList<String>();
        Matcher matcher = Pattern.compile(HREF_PATTERN, Pattern.CASE_INSENSITIVE).matcher(content);
        while (matcher.find()) {
            String link = matcher.group(1);
            if (isValidLink(link)) {
                link = resolveAndValidateURL(link, baseUrl);
                if (link != null) {
                    links.add(link);
                    logger.info("Found internal link: {}", link);
                }
            }
        }
        return links;
    }

    private boolean isValidLink(String link) {
        return !link.isEmpty() && !link.startsWith("<") && !link.contains("mailto:") && !link.contains("../");
    }

    private String resolveAndValidateURL(String link, String baseUrl) {
        if (!link.startsWith("http")) {
            try {
                URI baseUri = new URI(baseUrl);
                URI resolvedUri = baseUri.resolve(link).normalize();
                link = resolvedUri.toURL().toString();
            } catch (URISyntaxException | MalformedURLException e) {
                logger.error("Error resolving link: {}", link, e);
                return null;
            }
        }
        return isValidURL(link) ? link : null;
    }

    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            logger.error("Invalid URL: {}", url, e);
            return false;
        }
    }
}
