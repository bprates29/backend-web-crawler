package com.axreng.backend.model;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CrawlRequestTest {

    @Test
    void testIsValid() {
        CrawlRequest validRequest = new CrawlRequest("validterm");
        assertThat(validRequest.isValid(), is(true));

        CrawlRequest tooShortRequest = new CrawlRequest("abc");
        assertThat(tooShortRequest.isValid(), is(false));

        CrawlRequest tooLongRequest = new CrawlRequest("thiskeywordiswaytoolongandshouldnotbevalid");
        assertThat(tooLongRequest.isValid(), is(false));

        CrawlRequest nullRequest = new CrawlRequest(null);
        assertThat(nullRequest.isValid(), is(false));
    }

    @Test
    void testFromJson() {
        String json = "{\"keyword\": \"testkeyword\"}";
        CrawlRequest request = CrawlRequest.fromJson(json);
        assertThat(request, is(notNullValue()));
        assertThat(request.getKeyword(), is("testkeyword"));
    }

    @Test
    void testFromJsonInvalidJson() {
        String json = "{\"invalid\": \"json\"}";
        CrawlRequest request = CrawlRequest.fromJson(json);
        assertThat(request, is(notNullValue()));
        assertThat(request.getKeyword(), is(nullValue()));
    }
}
