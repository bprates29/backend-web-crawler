package com.axreng.backend.model;

import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CrawlStateTest {

    private CrawlState crawlState;
    private ConcurrentMap<String, CrawlStatus> crawlStatuses;
    private static final String BASE_URL_KEY = "BASE_URL";

    @BeforeEach
    public void setUp() {
        System.setProperty(BASE_URL_KEY, "http://axur.com");
        crawlStatuses = new ConcurrentHashMap<>();
        crawlState = new CrawlState(crawlStatuses);
    }

    @Test
    void testAddPendingUrl() {
        var randomUrl = RandomString.make();
        crawlState.addPendingUrl(randomUrl);
        assertThat(crawlState.getPendingUrls(), contains(randomUrl));
    }

    @Test
    void testPollPendingUrl() {
        var randomUrl = RandomString.make();
        crawlState.addPendingUrl(randomUrl);
        String url = crawlState.pollPendingUrl();
        assertThat(url, is(randomUrl));
        assertThat(crawlState.getPendingUrls(), is(empty()));
    }

    @Test
    void testAddVisitedUrl() {
        var randomUrl = RandomString.make();
        crawlState.addVisitedUrl(randomUrl);
        assertThat(crawlState.getVisitedUrls(), contains(randomUrl));
    }

    @Test
    void testIsVisited() {
        var randomUrl = RandomString.make();
        crawlState.addVisitedUrl(randomUrl);
        assertThat(crawlState.isVisited(randomUrl), is(true));
        assertThat(crawlState.isVisited(RandomString.make()), is(false));
    }

    @Test
    void testHasPendingUrls() {
        assertThat(crawlState.hasPendingUrls(), is(false));
        crawlState.addPendingUrl(RandomString.make());
        assertThat(crawlState.hasPendingUrls(), is(true));
    }

    @Test
    void testSetStatus() {
        var id = RandomString.make();
        var status = Status.ACTIVE;
        var keyword = RandomString.make();
        var doneStatus = Status.DONE;
        CrawlStatus crawlStatus = new CrawlStatus(id, status, keyword);
        crawlStatuses.put(id, crawlStatus);
        crawlState.setStatus(id, doneStatus);
        assertThat(crawlStatuses.get(id).getStatus(), is(doneStatus));
    }

    @Test
    void testAddUrlToCrawlStatus() {
        var id = RandomString.make();
        var status = Status.ACTIVE;
        var keyword = RandomString.make();
        var url = RandomString.make();
        CrawlStatus crawlStatus = new CrawlStatus(id, status, keyword);
        crawlStatuses.put(id, crawlStatus);
        crawlState.addUrlToCrawlStatus(id, url);
        assertThat(crawlStatuses.get(id).getUrls(), contains(url));
    }

/* Para fazer esse teste teria que colocar a variável de ambiente de alguma forma no teste,
    como são testes unitários fica mais complexo, teria que fazer por Reflection
 */
//    @Test
//    void testAddAllAndValidatePendingUrl() {
//        var url1 = crawlState.getTargetUrl() + RandomString.make();
//        var url2 = crawlState.getTargetUrl() + RandomString.make();
//        List<String> links = List.of(url1, url2);
//        crawlState.addAllAndValidatePendingUrl(links);
//        assertThat(crawlState.getPendingUrls(), containsInAnyOrder(url1, url2));
//    }
}
