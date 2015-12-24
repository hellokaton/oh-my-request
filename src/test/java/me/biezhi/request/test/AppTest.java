package me.biezhi.request.test;

import static me.biezhi.request.Request.get;

import java.io.File;
import java.io.IOException;

public class AppTest {

	public static void main(String[] args) throws IOException {
		
//		String body = get("https://github.com/opensearch.xml").body();
//		System.out.println(body);
		
		get("https://avatars3.githubusercontent.com/u/3849072?v=3&s=460").saveAsDisk(new File("D:/aaa.png"));
		
	}
}
