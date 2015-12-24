package me.biezhi.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class Request {
	
	// Default timeout is 60s
	private int timeout = 60 * 1000;
	
	// Request url
	private String url;
	
	// Request method
	private HttpMethod httpMethod;
	
	// Input data
	private InputStream inputStream;
	
	// Header info
	private Map<String, String> headers = new HashMap<String, String>();
	
	// Form data
	private Map<String, Object> formdatas = new HashMap<String, Object>();
	
	// Response
	private Response response;
	
	/**
	 * Send get request
	 * @param url
	 * @return
	 */
	public static Request get(String url){
		return new Request(url, HttpMethod.GET);
	}
	
	/**
	 * Send post request
	 * @param url
	 * @return
	 */
	public static Request post(String url){
		return new Request(url, HttpMethod.POST);
	}
	
	/**
	 * Add form data
	 * @param name
	 * @param value
	 * @return
	 */
	public Request param(String name, Object value){
		this.formdatas.put(name, value);
		return this;
	}
	
	/**
	 * Setting header info
	 * @param name
	 * @param value
	 * @return
	 */
	public Request header(String name, String value){
		this.headers.put(name, value);
		return this;
	}
	
	/**
	 * Setting request timeout
	 * @param timeout
	 * @return
	 */
	public Request timeout(int timeout){
		this.timeout = timeout;
		return this;
	}
	
	private Request(String url, HttpMethod httpMethod) {
		this.url = url;
		this.httpMethod = httpMethod;
	}
	
	/**
	 * Return request body as string
	 * @return
	 * @throws IOException
	 */
	public String body() throws IOException {
		send();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(this.inputStream));
		String line;
		StringBuffer body = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			body.append(line);
			body.append('\r');
		}
		rd.close();
		return body.toString();
	}
	
	/**
	 * Save request url to file
	 * @param file
	 * @throws IOException
	 */
	public void saveAsDisk(File file) throws IOException {
		send();
		
		ReadableByteChannel rbc = Channels.newChannel(this.inputStream);
		FileOutputStream fos = new FileOutputStream(file);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.flush();
		fos.close();
	}
	
	/**
	 * @return	Return response
	 */
	public Response response(){
		return response;
	}
	
	private void send() {
		try {
			
			response = new Response();
			
			// execute url
			String realurl = executeURL(this.url);
			
			URL yahoo = new URL(realurl);
			if(url.startsWith("https")){
				HttpsURLConnection urlConn = (HttpsURLConnection) yahoo.openConnection();
				urlConn.setRequestMethod(this.httpMethod.toString());
				Set<String> keys = headers.keySet();
				for(String key : keys){
					urlConn.setRequestProperty(key, headers.get(key));
				}
				urlConn.setReadTimeout(timeout);
				response.length(urlConn.getContentLength());
				this.inputStream = urlConn.getInputStream();
			} else {
				HttpURLConnection urlConn = (HttpURLConnection) yahoo.openConnection();
				urlConn.setRequestMethod(this.httpMethod.toString());
				Set<String> keys = headers.keySet();
				for(String key : keys){
					urlConn.setRequestProperty(key, headers.get(key));
				}
				urlConn.setReadTimeout(timeout);
				response.length(urlConn.getContentLength());
				this.inputStream = urlConn.getInputStream();
			}
			response.statusCode(200);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			response.statusCode(500);
		} catch (IOException e) {
			e.printStackTrace();
			response.statusCode(500);
		}
	}

	private String executeURL(String url) {
		// url has been a parameter http://xxx.com/aa?name=value
		
		StringBuffer sb = new StringBuffer(url);
		if(formdatas.size() > 0){
			if(url.indexOf("?") != -1 && url.indexOf("=") != -1){
				sb.append("&");
			} else {
				sb.append("?");
			}
			Set<String> keys = formdatas.keySet();
			for(String key : keys){
				sb.append(key + "=" + formdatas.get(key) + "&");
			}
		}
		return sb.substring(0, sb.length() - 1);
	}
	
}
