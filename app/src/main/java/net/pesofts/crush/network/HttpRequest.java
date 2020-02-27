package net.pesofts.crush.network;

import android.app.Activity;

public class HttpRequest implements IRequest {

    private HttpNetworkParts httpNetworkParts;
    private HttpRequestVO requestValue;
    private Object task;
    private ProgressHandler progressHandler;
    private CallbackManager callbackManager;

    public HttpRequest(HttpNetworkParts httpNetworkParts) {
        this.httpNetworkParts = httpNetworkParts;
    }

    public HttpRequestVO getRequestValue() {
        return requestValue;
    }

    public void setRequestValue(HttpRequestVO requestValue) {
        this.requestValue = requestValue;
    }

    public Object getTask() {
        return task;
    }

    public void setTask(Object task) {
        this.task = task;
    }

    public void setProgressHandler(ProgressHandler progressHandler) {
        this.progressHandler = progressHandler;
    }

    public CallbackManager getCallbackManager() {
        return callbackManager;
    }

    public void setCallbackManager(CallbackManager callbackManager) {
        this.callbackManager = callbackManager;
    }

    public boolean isCancelable() {
        if (progressHandler == null) {
            return false;
        }
        return progressHandler.isCancelable();
    }

    public Activity getActivity() {
        if (progressHandler != null) {
            return progressHandler.getActivity();
        }
        return null;
    }

    @Override
    public boolean isFinished() {
        return task == null;
    }

    @Override
    public void execute() {
        httpNetworkParts.perform(this);
    }

    @Override
    public void cancel() {
        httpNetworkParts.cancel(this);
    }
}
