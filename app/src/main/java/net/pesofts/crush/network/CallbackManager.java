package net.pesofts.crush.network;

import net.pesofts.crush.Util.CollectionUtil;

import java.util.List;
import java.util.Map;

public class CallbackManager {

    private HttpResponseCallback callback;
    private List<Interceptor> interceptors;
    private boolean isIntercept;

    public void setCallback(HttpResponseCallback callback) {
        this.callback = callback;
    }

    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public void setIsIntercept(boolean isIntercept) {
        this.isIntercept = isIntercept;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public void invokeStart() {
        if (isIntercept && CollectionUtil.isNotEmpty(interceptors)) {
            for (Interceptor interceptor : interceptors) {
                interceptor.onStart();
            }
        }

        if (callback != null) {
            callback.onStart();
        }
    }

    public void invokeFinish() {
        if (isIntercept && CollectionUtil.isNotEmpty(interceptors)) {
            for (Interceptor interceptor : interceptors) {
                interceptor.onFinish();
            }
        }

        if (callback != null) {
            callback.onFinish();
        }
    }

    @SuppressWarnings("unchecked")
    public void invokeResult(Object response) {
        if (isIntercept && CollectionUtil.isNotEmpty(interceptors)) {
            for (Interceptor interceptor : interceptors) {
                interceptor.onResponse(response);
            }
        }

        if (callback != null) {
            callback.onResponse(response);
        }
    }

    public void invokeError(HttpNetworkError error) {
        if (isIntercept && CollectionUtil.isNotEmpty(interceptors)) {
            for (Interceptor interceptor : interceptors) {
                interceptor.onError(error);
            }
        }

        if (callback != null) {
            callback.onError(error);
        }
    }

    public void invokeCancel() {
        if (isIntercept && CollectionUtil.isNotEmpty(interceptors)) {
            for (Interceptor interceptor : interceptors) {
                interceptor.onCancel();
            }
        }

        if (callback != null) {
            callback.onCancel();
        }
    }

    public void invokeParse(int code, Map<String, String> header, byte[] data, long networkTime) {
        if (isIntercept && CollectionUtil.isNotEmpty(interceptors)) {
            for (Interceptor interceptor : interceptors) {
                interceptor.onParse(code, header, data, networkTime);
            }
        }

        if (callback != null) {
            callback.onParse(code, header, data, networkTime);
        }
    }
}
