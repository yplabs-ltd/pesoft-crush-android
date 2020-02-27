package net.pesofts.crush.model;

public class PointLog {
    private String id;
    private String userid;
    private String code;
    private String description;
    private String pointdiff;
    private String adjustpoint;
    private long regDttm;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPointdiff() {
        return pointdiff;
    }

    public void setPointdiff(String pointdiff) {
        this.pointdiff = pointdiff;
    }

    public String getAdjustpoint() {
        return adjustpoint;
    }

    public void setAdjustpoint(String adjustpoint) {
        this.adjustpoint = adjustpoint;
    }

    public long getRegDttm() {
        return regDttm;
    }

    public void setRegDttm(long regDttm) {
        this.regDttm = regDttm;
    }
}
