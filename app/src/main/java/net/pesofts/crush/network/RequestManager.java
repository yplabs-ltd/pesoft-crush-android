package net.pesofts.crush.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import net.pesofts.crush.Constants;

public class RequestManager {

    private static RequestQueue requestQueue;
    private static ImageLoader imageLoader;

    private static HttpNetworkParts httpNetworkParts;

    private RequestManager() {
    }

    public static void initialize(Context context) {
        VolleyLog.DEBUG = Constants.ENABLE_LOG;
        requestQueue = Volley.newRequestQueue(context);
        imageLoader = new ImageLoader(requestQueue, new BitmapLruCache());
        httpNetworkParts = new VolleyNetworkParts(context, requestQueue);
    }

    public static RequestQueue getRequestQueue() {
        if (requestQueue != null) {
            return requestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public static void addRequest(Request<?> request, Object tag) {
        if (tag != null) {
            request.setTag(tag);
        }
        requestQueue.add(request);
    }

    public static HttpNetworkParts getNetworkParts() {
        return httpNetworkParts;
    }

    public static void setNetworkParts(HttpNetworkParts networkParts) {
        RequestManager.httpNetworkParts = networkParts;
    }

    public static void cancelAll(Object tag) {
        requestQueue.cancelAll(tag);
    }

    public static void stop() {
        requestQueue.stop();
    }

    public static ImageLoader getImageLoader() {
        if (imageLoader != null) {
            return imageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }
}
