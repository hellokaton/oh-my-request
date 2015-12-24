package me.biezhi.request.test;

import static me.biezhi.request.Request.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class AppTest {

	@Test
	public void testGet(){
		try {
			String body = get("https://github.com/opensearch.xml").body();
			System.out.println(body);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSaveAsFile(){
		try {
			get("https://avatars3.githubusercontent.com/u/3849072?v=3&s=460").saveAsDisk(new File("D:/aaa.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPost(){
		try {
			post("https://github.com/biezhi/java8-request").param("name", "jack").body();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
