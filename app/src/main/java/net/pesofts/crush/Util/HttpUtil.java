package net.pesofts.crush.Util;

import android.content.Context;

import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpRequestVO;

import org.apache.http.NameValuePair;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    public static final String COOKIE_KEY = "Cookie";
    public static final String CRUSH_ACCESS_TOKEN = "CrushAccessToken";

    public static HttpRequestVO getHttpRequestVO(String url, Class<?> responseVO, List<NameValuePair> paramInfo, Context context) {
        return getHttpRequestVO(url, responseVO, null, paramInfo, HttpMethod.GET, context);
    }

    public static HttpRequestVO getHttpRequestVO(String url, Class<?> responseVO, List<NameValuePair> paramInfo, HttpMethod httpMethod, Context context) {
        return getHttpRequestVO(url, responseVO, null, paramInfo, httpMethod, context);
    }

    public static HttpRequestVO getHttpRequestVO(String url,Type responseType, List<NameValuePair> paramInfo, HttpMethod httpMethod, Context context) {
        return getHttpRequestVO(url, null, responseType, paramInfo, httpMethod, context);
    }

    public static HttpRequestVO getHttpRequestVO(String url, Type responseType, List<NameValuePair> paramInfo, Context context) {
        return getHttpRequestVO(url, null, responseType, paramInfo, HttpMethod.GET, context);
    }

    public static HttpRequestVO getHttpRequestVO(String url, Class<?> responseVO, Type responseType, List<NameValuePair> paramInfo, HttpMethod httpMethod, Context context) {
        Map<String, String> headerInfo = new HashMap<>();
        addSessionCookie(headerInfo, context);
        HttpRequestVO requestVO = new HttpRequestVO();
        requestVO.setUrl(url);
        requestVO.setResponseVO(responseVO);
        requestVO.setResponseType(responseType);
        requestVO.setHeaderInfo(headerInfo);
        requestVO.setParamInfo(paramInfo);
        requestVO.setHttpMethod(httpMethod);
        return requestVO;
    }

    public static void checkSessionCookie(Map<String, String> headers, Context context) {
        if (headers.containsKey(SET_COOKIE_KEY)
                && headers.get(SET_COOKIE_KEY).startsWith(CRUSH_ACCESS_TOKEN)) {
            String cookie = headers.get(SET_COOKIE_KEY);
            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, cookie);
            }
        }
    }

    public static void addSessionCookie(Map<String, String> headers, Context context) {
        String accessToken = SharedPrefHelper.getInstance(context).getSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, "");
        if (accessToken.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(CRUSH_ACCESS_TOKEN);
            builder.append("=");
            builder.append(accessToken);
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, builder.toString());
        }
    }
}
