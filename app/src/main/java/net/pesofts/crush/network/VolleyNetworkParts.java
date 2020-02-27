package net.pesofts.crush.network;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

import net.pesofts.crush.Constants;
import net.pesofts.crush.Util.CollectionUtil;

import org.apache.http.NameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class VolleyNetworkParts extends HttpNetworkParts<Request> {

    private RequestQueue requestQueue;

    public VolleyNetworkParts(Context context, RequestQueue requestQueue) {
        super(context);
        this.requestQueue = requestQueue;
    }

    @Override
    public void onPerform(HttpRequest httpRequest) {
        HttpRequestVO requestValue = httpRequest.getRequestValue();
        ResponseBridge responseBridge = new ResponseBridge<>(
                httpRequest,
                httpRequest.getCallbackManager());

        VolleyRequest request = createRequest(requestValue, responseBridge);
        request.setRetryPolicy(createRetryPolicy());
        request.setHeaders(requestValue.getHeaderInfo());
        request.setParameters(requestValue.getParameter());

        if (requestQueue != null) {
            requestQueue.add(request);
        }
        httpRequest.setTask(request);
    }

    @Override
    public void onCancel(Request task) {
        if (task != null) {
            task.cancel();
        }
    }

    @SuppressWarnings("unchecked")
    private static VolleyRequest createRequest(HttpRequestVO requestValue, ResponseBridge responseBridge) {
        if (requestValue.getPayloadEntity() != null) {
            VolleyJsonRequest request = new VolleyJsonRequest<>(
                    convertMethod(requestValue.getHttpMethod()),
                    extractUrl(requestValue),
                    requestValue.getResponseVO(),
                    requestValue.getResponseType(),
                    responseBridge,
                    responseBridge);
            request.setRequestObject(requestValue.getPayloadEntity());
            return request;
        } else {
            VolleyRequest request = new VolleyRequest<>(
                    convertMethod(requestValue.getHttpMethod()),
                    extractUrl(requestValue),
                    requestValue.getResponseVO(),
                    requestValue.getResponseType(),
                    responseBridge,
                    responseBridge);
            return request;
        }
    }

    private static int convertMethod(HttpMethod httpMethod) {
        int method = Request.Method.GET;
        if (httpMethod == null) {
            return method;
        }

        switch (httpMethod) {
            case GET:
                method = Request.Method.GET;
                break;
            case POST:
                method = Request.Method.POST;
                break;
            case PUT:
                method = Request.Method.PUT;
                break;
            case DELETE:
                method = Request.Method.DELETE;
                break;
            default:
                break;
        }
        return method;
    }

    private static HttpNetworkError convertError(VolleyError error) {
        HttpNetworkError networkError = new HttpNetworkError();
        if (error.networkResponse != null) {
            networkError.setStatusCode(error.networkResponse.statusCode);
            try {
                networkError.setResponseBody(new String(error.networkResponse.data, "utf-8"));
            } catch (UnsupportedEncodingException e) {
            }
        }
        return networkError;
    }

    private static String extractUrl(HttpRequestVO value) {
        String url = value.getUrl();
        if (url == null) {
            return null;
        }

        List<NameValuePair> parameters = value.getParamInfo();
        if (!hasEntity(value.getHttpMethod()) && CollectionUtil.isNotEmpty(parameters)) {
            Uri.Builder uriBuilder = Uri.parse(value.getUrl()).buildUpon();
            for (NameValuePair pair : parameters) {
                uriBuilder.appendQueryParameter(
                        pair.getName(),
                        TextUtils.isEmpty(pair.getValue()) ? "" : pair.getValue());
            }
            url = uriBuilder.toString();
        }
        return url;
    }

    private static boolean hasEntity(HttpMethod method) {
        return HttpMethod.POST.equals(method)
                || HttpMethod.PUT.equals(method)
                || HttpMethod.PATCH.equals(method);
    }

    private static RetryPolicy createRetryPolicy() {
        return new DefaultRetryPolicy(
                Constants.HTTP_CONNECTION_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public class ResponseBridge<T> implements VolleyResponseListener<T> {

        private HttpRequest request;
        private CallbackManager callbackManager;

        public ResponseBridge(HttpRequest request, CallbackManager callbackManager) {
            this.request = request;
            this.callbackManager = callbackManager;
        }

        @Override
        public void onParse(NetworkResponse response) {
            callbackManager.invokeParse(
                    response.statusCode,
                    response.headers,
                    response.data,
                    response.networkTimeMs);

        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            request.setTask(null);
            callbackManager.invokeError(convertError(volleyError));
            callbackManager.invokeFinish();
        }

        @Override
        public void onResponse(T response) {
            request.setTask(null);
            callbackManager.invokeResult(response);
            callbackManager.invokeFinish();
        }
    }
}
