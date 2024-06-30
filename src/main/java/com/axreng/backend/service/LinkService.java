package com.axreng.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkService {

    private static final Logger logger = LoggerFactory.getLogger(LinkService.class);
    private final String HREF_PATTERN = "href\\s*=\\s*\"([^\"]*)\"";

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
