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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final HttpClient client = HttpClient.newHttpClient();
    public static final String HREF_PATTERN = "href\\s*=\\s*\"([^\"]*)\"";

    private HttpUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String fetchHttpContent(String urlString) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(urlString))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                logger.error("Failed to retrieve content. HTTP response code: {} for URL: {}", response.statusCode(), urlString);
                return null;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error retrieving content from URL: {}", urlString, e);
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            logger.error("Invalid URL: {}", urlString, e);
            return null;
        }
    }

    public static List<String> findInternalLinks(String content, String baseUrl) {
        var links = new ArrayList<String>();
        if (content == null) {
            return links;
        }
        Matcher matcher = Pattern.compile(HREF_PATTERN, Pattern.CASE_INSENSITIVE).matcher(content);
        while (matcher.find()) {
            String link = matcher.group(1);
            link = resolveAndValidateLink(link, baseUrl);
            if (isValidLink(link)) {
                links.add(link);
            }
        }
        return links;
    }

    private static String resolveAndValidateLink(String link, String baseUrl) {
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

    private static boolean isValidLink(String link) {
        return link != null && !link.isEmpty() && !link.startsWith("<") && !link.contains("mailto:") && !link.contains("../");
    }

    private static boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}