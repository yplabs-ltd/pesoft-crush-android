package net.pesofts.crush.network;

public class HttpNetworkError extends Exception {

	private int statusCode;
	private String responseBody;

	public HttpNetworkError() {
	}

	public HttpNetworkError(String message) {
		super(message);
	}

	public HttpNetworkError(Throwable throwable) {
		super(throwable);
	}

	public HttpNetworkError(String message, Throwable throwable) {
		super(message, throwable);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
}
