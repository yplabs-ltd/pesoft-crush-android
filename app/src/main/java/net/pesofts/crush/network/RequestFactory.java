package net.pesofts.crush.network;


import net.pesofts.crush.CrushApplication;

import java.util.ArrayList;
import java.util.List;

public class RequestFactory {

    private HttpNetworkParts networkParts;
    private ProgressHandler progressHandler;
    private List<Interceptor> interceptors;

    public IRequest create(HttpRequestVO value, HttpResponseCallback callback) {
        HttpNetworkParts networkParts = this.networkParts;
        if (networkParts == null) {
            networkParts = RequestManager.getNetworkParts();
        }
        CallbackManager callbackManager = new CallbackManager();
        callbackManager.setCallback(callback);
        addDefaultInterceptors();
        callbackManager.setInterceptors(interceptors);
        callbackManager.setIsIntercept(networkParts instanceof VolleyNetworkParts);

        HttpRequest httpRequest = new HttpRequest(networkParts);
        httpRequest.setRequestValue(value);
        httpRequest.setProgressHandler(progressHandler);
        httpRequest.setCallbackManager(callbackManager);
        return httpRequest;
    }

    private void addDefaultInterceptors() {
        if (interceptors == null) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(new NetworkErrorLogger(CrushApplication.getInstance()));
        if (progressHandler != null) {
            interceptors.add(progressHandler);
        }
    }

    public void setNetworkParts(HttpNetworkParts networkParts) {
        this.networkParts = networkParts;
    }

    public void setProgressHandler(ProgressHandler progressHandler) {
        this.progressHandler = progressHandler;
    }

    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public static class Builder {

        private HttpNetworkParts networkParts;
        private ProgressHandler progressHandler;
        private List<Interceptor> interceptors = new ArrayList<>();

        public Builder setNetworkParts(HttpNetworkParts networkParts) {
            this.networkParts = networkParts;
            return this;
        }

        public Builder setProgressHandler(ProgressHandler progressHandler) {
            this.progressHandler = progressHandler;
            interceptors.add(progressHandler);
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            interceptors.add(interceptor);
            return this;
        }

        public RequestFactory build() {
            RequestFactory requestFactory = new RequestFactory();
            requestFactory.setNetworkParts(networkParts);
            requestFactory.setInterceptors(interceptors);
            requestFactory.setProgressHandler(progressHandler);
            return requestFactory;
        }
    }
}
