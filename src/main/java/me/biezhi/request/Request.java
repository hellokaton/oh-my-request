package me.biezhi.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Request {

	private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);

	/**
	 * request timeout
	 */
	private int timeout = Const.DEFAULT_REQUEST_TIMEOUT;

	/**
	 * request url
	 */
	private String url;

	/**
	 * rquest httpmethod
	 */
	private HttpMethod httpMethod;

	/**
	 * request header info
	 */
	private Map<String, String> headers;

	/**
	 * request form data
	 */
	private Map<String, Object> formdatas;

	/**
	 * request body
	 */
	private Body body;

	/**
	 * response
	 */
	private Response response;

	private Request(){

	}

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
	 * Send put request
	 * @param url
	 * @return
	 */
	public static Request put(String url){
		return new Request(url, HttpMethod.PUT);
	}
	
	/**
	 * Send delete request
	 * @param url
	 * @return
	 */
	public static Request delete(String url){
		return new Request(url, HttpMethod.DELETE);
	}
	
	/**
	 * Send patch request
	 * @param url
	 * @return
	 */
	public static Request patch(String url){
		return new Request(url, HttpMethod.PATCH);
	}
	
	/**
	 * Send head request
	 * @param url
	 * @return
	 */
	public static Request head(String url){
		return new Request(url, HttpMethod.HEAD);
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
	 * Put form data
	 * @param params
	 * @return
	 */
	public Request params(Map<String, Object> params){
		this.formdatas.putAll(params);
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
	
	public Request auth(){
		return this;
	}
	
	private Request(String url, HttpMethod httpMethod) {
		this.url = url;
		this.httpMethod = httpMethod;
		this.headers = new HashMap<String, String>();
		this.formdatas = new HashMap<String, Object>();
	}
	
	/**
	 * Return request body as string
	 * @return
	 * @throws IOException
	 */
	public String body() throws IOException {

		this.execute();

		if(null != this.body){
			return this.body.getString();
		}
		return null;
	}
	
	/**
	 * @return	Return request body
	 */
	public Body requestBody(){
		return this.body;
	}
	
	/**
	 * Save request url to file
	 * @param file
	 * @throws IOException
	 */
	public void saveAsDisk(File file) throws IOException {

		this.execute();

		if(null == this.body){
			throw new IOException("request body is null");
		}

		if(null == this.body.getInputStream()){
			throw new IOException("inputstream is null");
		}

		ReadableByteChannel rbc = Channels.newChannel(this.body.getInputStream());
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
	
	private Body execute() {
		try {

			LOGGER.debug("Request URL\t\t\t=> {}", this.url);
			LOGGER.debug("Request Method\t\t=> {}", this.httpMethod);
			LOGGER.debug("Request Header\t\t=> {}", this.headers);
			LOGGER.debug("Request FormDatas\t=> {}", this.formdatas);

			this.response = new Response();
			
			URL _url = new URL(this.url);
			final HttpURLConnection urlConn = url.startsWith("https") ? 
					(HttpsURLConnection) _url.openConnection() : (HttpURLConnection) _url.openConnection();
			
			urlConn.setRequestMethod(this.httpMethod.toString());
			urlConn.setReadTimeout(timeout);
			
			// Setting header
			Iterator<Map.Entry<String, String>> its = headers.entrySet().iterator();
			while(its.hasNext()) {
				Map.Entry<String, String> entry = its.next();
				urlConn.setRequestProperty(entry.getKey(), entry.getValue());
			}

			// Send post data
			if(this.httpMethod == HttpMethod.POST){
				
				// sn=C02G8416DRJM&cn=&locale=&caller=&num=12345
				String urlParameters = this.postParams();
				if(null != urlParameters && !urlParameters.equals("")){
					urlConn.setDoOutput(true);
					DataOutputStream wr = new DataOutputStream(urlConn.getOutputStream());
					wr.writeBytes(urlParameters);
					wr.flush();
					wr.close();
				}
			}
			
			response.contentType(urlConn.getContentType());
			response.length(urlConn.getContentLength());
			response.date(urlConn.getDate());
			response.msg(urlConn.getResponseMessage());
			response.statusCode(urlConn.getResponseCode());
			
			this.body = new Body(urlConn.getInputStream());
			return this.body;
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
			response.statusCode(500);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			response.statusCode(500);
		}
		return null;
	}

	private String postParams() {
		if(formdatas.size() > 0){
			// url has been a parameter e.g:sn=C02G8416DRJM&cn=&locale=&caller=&num=12345
			StringBuffer sb = new StringBuffer("");
			Iterator<Map.Entry<String, Object>> its = formdatas.entrySet().iterator();
			while(its.hasNext()) {
				Map.Entry<String, Object> entry = its.next();
				sb.append('&').append(entry.getKey()).append('=').append(entry.getValue());
			}
			return sb.substring(1);
		}
		return null;
	}
	
}
