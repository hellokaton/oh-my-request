package io.github.biezhi.request.test;

import io.github.biezhi.request.Request;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RequestTest {

    private static final Logger log = LoggerFactory.getLogger(RequestTest.class);

    @Test
    public void testGet() {
        String body = Request.get("https://tale.biezhi.me/about").body();
        log.info(body);
    }

    @Test
    public void testSaveAsFile() {
        Request.get("https://avatars3.githubusercontent.com/u/3849072?v=3&s=460")
                .receive(new File("D:/aaa.png"));
    }

    @Test
    public void testPost() {
        Request.post("https://github.com/biezhi/java8-request").form("name", "jack").body();
    }

    @Test
    public void testHeader() {
        String out = Request.get("https://github.com/biezhi/java8-request")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0")
                .body();
        System.out.println(out);
    }

}