package com.axreng.backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {

    private com.axreng.backend.util.LinkService linkService;

    @BeforeEach
    public void setUp() {
        linkService = new com.axreng.backend.util.LinkService();
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

        List<String> links = linkService.findInternalLinks(content, baseUrl);

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
