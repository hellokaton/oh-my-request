package me.biezhi.request;

public class Response {

	private int statusCode;
	private int length;
	private long date;
	private String msg;
	private String contentType;
	
	public Response() {
	}
	
	public void statusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public int statusCode() {
		return statusCode;
	}

	public int length() {
		return length;
	}

	public void length(int length) {
		this.length = length;
	}

	public String contentType() {
		return contentType;
	}

	public void contentType(String contentType) {
		this.contentType = contentType;
	}

	public long date() {
		return date;
	}

	public void date(long date) {
		this.date = date;
	}

	public String msg() {
		return msg;
	}

	public void msg(String msg) {
		this.msg = msg;
	}
	
	
}
