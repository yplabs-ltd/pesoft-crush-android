package net.pesofts.crush.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.sendbird.android.shadow.com.squareup.okhttp.Headers;
import com.sendbird.android.shadow.com.squareup.okhttp.MediaType;
import com.sendbird.android.shadow.com.squareup.okhttp.MultipartBuilder;
import com.sendbird.android.shadow.com.squareup.okhttp.RequestBody;
import com.sendbird.android.shadow.okio.Buffer;

import net.pesofts.crush.Util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class VolleyMultipartRequest extends Request<String> {


    /* MediaTypes */
    public static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    public static final MediaType MEDIA_TYPE_TEXT_PLAIN = MediaType.parse("text/plain");

    private MultipartBuilder mBuilder = new MultipartBuilder();
    private final Response.Listener<String> mListener;
    private RequestBody mRequestBody;

    public VolleyMultipartRequest(String url,
                                  Response.Listener<String> listener,
                                  Response.ErrorListener errorListener
    ) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mBuilder.type(MultipartBuilder.FORM);
    }

    /**
     * Adds a collection of string values to the request.
     *
     * @param mParams {@link HashMap} collection of values to be added to the request.
     */
    public void addStringParams(Map<String, String> mParams) {
        for (Map.Entry<String, String> entry : mParams.entrySet()) {
            mBuilder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                    RequestBody.create(MEDIA_TYPE_TEXT_PLAIN, entry.getValue()));
        }
    }

    /**
     * Adds a single value to the request.
     *
     * @param key   String - the field name.
     * @param value String - the field's value.
     */
    public void addStringParam(String key, String value) {
        mBuilder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
                RequestBody.create(MEDIA_TYPE_TEXT_PLAIN, value));
    }

    /**
     * Adds a binary attachment to the request.
     *
     * @param content_type {@link MediaType} - the type of the attachment.
     * @param key          String - the attachment field name.
     * @param value        {@link File} - the file to be attached.
     */
    public void addAttachment(MediaType content_type, String key, File value) {
        mBuilder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
                RequestBody.create(content_type, value));
    }

    /**
     * Builds the request.
     * Must be called before adding the request to the Volley request queue.
     */
    public void buildRequest() {
        mRequestBody = mBuilder.build();
    }

    @Override
    public String getBodyContentType() {
        return mRequestBody.contentType().toString();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            Buffer buffer = new Buffer();
            mRequestBody.writeTo(buffer);
            buffer.copyTo(bos);
        } catch (IOException e) {
            LogUtil.e(e.toString());
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        return super.setRetryPolicy(retryPolicy);
    }

    @Override
    protected void deliverResponse(String response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }
}
