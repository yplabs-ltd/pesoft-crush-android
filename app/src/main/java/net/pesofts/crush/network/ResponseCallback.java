package net.pesofts.crush.network;

import java.util.Map;

public interface ResponseCallback<T> {

	/* Runs on the UI thread */
	void onStart();

	/* Runs on the UI thread */
	void onFinish();

	/* Runs on the UI thread */
	void onResponse(T response);

	/* Runs on the UI thread */
	void onError(HttpNetworkError error);

	/* Runs on the UI thread */
	void onCancel();

	/* Runs on a worker thread */
	void onParse(int code, Map<String, String> header, byte[] data, long networkTime);
}
