package me.biezhi.request;

public class Response {

	private int statusCode;
	private int length;
	
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
	
}
