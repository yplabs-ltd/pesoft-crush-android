package net.pesofts.crush.network;


import net.pesofts.crush.Util.CollectionUtil;

import org.apache.http.NameValuePair;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestVO {

    private String url;
    private Map<String, String> headerInfo;
    private List<NameValuePair> paramInfo;
    private Class<?> responseVO;
    private Type responseType;
    private HttpMethod httpMethod;
    private Object payloadEntity; // not collection

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        this.setHttpMethod(HttpMethod.GET);
    }

    public Map<String, String> getHeaderInfo() {
        return headerInfo;
    }

    public void setHeaderInfo(Map<String, String> headerInfo) {
        this.headerInfo = headerInfo;
    }

    public List<NameValuePair> getParamInfo() {
        return paramInfo;
    }

    public Map<String, String> getParameter() {
        if (CollectionUtil.isEmpty(paramInfo)) {
            return new HashMap<>();
        }
        Map<String, String> parameter = new HashMap<>();
        for (NameValuePair nvp : paramInfo) {
            parameter.put(nvp.getName(), nvp.getValue());
        }
        return parameter;
    }

    public void setParamInfo(List<NameValuePair> paramInfo) {
        this.paramInfo = paramInfo;
    }

    public Class<?> getResponseVO() {
        return responseVO;
    }

    public void setResponseVO(Class<?> responseVO) {
        this.responseVO = responseVO;
    }

    public Type getResponseType() {
        return responseType;
    }

    public void setResponseType(Type responseType) {
        this.responseType = responseType;
    }

    @Override
    public String toString() {
        StringBuilder printMessage = new StringBuilder();
        printMessage.append("HttpRequestVO [url=");
        printMessage.append(url);
        printMessage.append(", headerInfo=");
        printMessage.append(headerInfo);
        printMessage.append(", paramInfo=");
        printMessage.append(paramInfo);
        printMessage.append(", responseVO=");
        printMessage.append(responseVO);
        printMessage.append(", responseType=");
        printMessage.append(responseType);
        printMessage.append("]");
        return printMessage.toString();
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Object getPayloadEntity() {
        return payloadEntity;
    }

    public void setPayloadEntity(Object payloadEntity) {
        this.payloadEntity = payloadEntity;
    }

}
