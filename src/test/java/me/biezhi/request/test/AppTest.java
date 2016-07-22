package me.biezhi.request.test;

import java.io.File;
import java.io.IOException;

import me.biezhi.request.Header;

import me.biezhi.request.Request;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);

	@Test
	public void testGet(){
		try {
			String body = Request.get("https://github.com/opensearch.xml").body();
			LOGGER.info(body);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSaveAsFile(){
		try {
			Request.get("https://avatars3.githubusercontent.com/u/3849072?v=3&s=460")
			.saveAsDisk(new File("D:/aaa.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPost(){
		try {
			Request.post("https://github.com/biezhi/java8-request")
			.param("name", "jack")
			.body();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testHeader(){
		try {
			String out = Request.get("https://github.com/biezhi/java8-request")
			.header(Header.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0")
			.body();
			System.out.println(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
