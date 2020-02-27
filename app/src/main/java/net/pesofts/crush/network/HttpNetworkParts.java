package net.pesofts.crush.network;

import android.content.Context;

public abstract class HttpNetworkParts<TASK> {

	protected Context context;

	public HttpNetworkParts(Context context) {
		this.context = context;
	}

	public void perform(HttpRequest httpRequest) {
		httpRequest.getCallbackManager().invokeStart();
		onPerform(httpRequest);
	}

	@SuppressWarnings("unchecked")
	public void cancel(HttpRequest httpRequest) {
		httpRequest.getCallbackManager().invokeCancel();
		onCancel((TASK) httpRequest.getTask());
		httpRequest.setTask(null);
	}

	public abstract void onPerform(HttpRequest httpRequest);

	public abstract void onCancel(TASK task);
}
