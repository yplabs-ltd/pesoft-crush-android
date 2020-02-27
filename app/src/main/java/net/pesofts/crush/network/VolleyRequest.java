package net.pesofts.crush.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class VolleyRequest<T> extends Request<T> {
    private Class<T> clazz;
    private Type responseType;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private VolleyResponseListener<T> listener;

    public VolleyRequest(int method, String url, Class<T> clazz, Type responseType, VolleyResponseListener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.responseType = responseType;
        this.listener = listener;
//        Log.e("request", url);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        if (this.headers != null) {
            headers.putAll(this.headers);
        }
        return headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters != null ? parameters : super.getParams();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            final Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
//			String charset = HttpHeaderParser.parseCharset(response.headers);
//            String str = new String(response.data, "UTF-8");
//            Log.e("reponse", str);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.data)));
//			T object = getResponseParser().onParse(bufferedReader, clazz, charset);
            T object = null;
            if (clazz == null && responseType != null) {
                object = gson.fromJson(bufferedReader, responseType);
            } else {
                object = gson.fromJson(bufferedReader, clazz);
            }
            listener.onParse(response);
            return Response.success(
                    object,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}
