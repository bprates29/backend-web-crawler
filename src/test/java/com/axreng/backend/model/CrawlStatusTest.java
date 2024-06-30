package com.axreng.backend.model;

import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CrawlStatusTest {

    private CrawlStatus crawlStatus;
    private final String id = RandomString.make();
    private final Status status = Status.ACTIVE;
    private final String keyword = RandomString.make();

    @BeforeEach
    public void setUp() {
        crawlStatus = new CrawlStatus(id, status, keyword);
    }

    @Test
    void testAddUrl() {
        var url = RandomString.make();
        crawlStatus.addUrl(url);
        List<String> urls = crawlStatus.getUrls();
        assertThat(urls, hasSize(1));
        assertThat(urls, contains(url));
    }

    @Test
    void testToJson() {
        var url = RandomString.make();
        crawlStatus.addUrl(url);
        String json = crawlStatus.toJson();
        assertThat(json, containsString(id));
        assertThat(json, containsString(status.name()));
        assertThat(json, containsString(keyword));
        assertThat(json, containsString(url));
    }
}
