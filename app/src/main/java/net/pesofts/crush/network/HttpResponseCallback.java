package net.pesofts.crush.network;

import java.util.Map;

public abstract class HttpResponseCallback<T> implements ResponseCallback<T> {

	@Override
	public void onStart() {

	}

	@Override
	public void onFinish() {

	}

	@Override
	public void onError(HttpNetworkError error) {

	}

	@Override
	public void onCancel() {

	}

	@Override
	public void onParse(int code, Map<String, String> header, byte[] data, long networkTime) {

	}
}
