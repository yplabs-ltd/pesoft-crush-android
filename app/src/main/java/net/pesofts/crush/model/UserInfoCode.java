package net.pesofts.crush.model;

import java.io.Serializable;

public class UserInfoCode implements Serializable {
    private static final long serialVersionUID = -2287926303491274959L;

    private String code;
    private String value;
    private String extra;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
