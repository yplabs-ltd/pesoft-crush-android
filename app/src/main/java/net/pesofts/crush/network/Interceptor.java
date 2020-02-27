package net.pesofts.crush.network;

import java.util.Map;

public class Interceptor<T> implements ResponseCallback<T> {

	/* Runs on the UI thread */
	@Override
	public void onStart() {

	}

	/* Runs on the UI thread */
	@Override
	public void onFinish() {

	}

	/* Runs on the UI thread */
	@Override
	public void onResponse(T response) {

	}

	/* Runs on the UI thread */
	@Override
	public void onError(HttpNetworkError error) {

	}

	/* Runs on the UI thread */
	@Override
	public void onCancel() {

	}

	/* Runs on a worker thread */
	@Override
	public void onParse(int code, Map<String, String> header, byte[] data, long networkTime) {

	}
}
