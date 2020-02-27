package net.pesofts.crush.model;

/**
 * Created by erkas on 2017. 5. 10..
 *
 * {
 "id" : 107662,
 "order" : 1,
 "createdat" : "2017-04-22T07:27:29.698Z",
 "last" : false,
 "voiceUrl" : "https:\/\/s3-ap-northeast-1.amazonaws.com\/pesofts-image\/voiceChat\/20170422\/1076621492839385652",
 "uripath" : "\/voiceChat\/20170422\/1076621492839385652",
 "remainTime" : "75.4",
 "name" : "  니꿍꺼또"
 },
 */

public class StoryDetailData {
    private int id;
    private int order;
    private String createdat;
    private boolean last;
    private String voiceUrl;
    private String uripath;
    private String remainTime;
    private String name;

    public int getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public String getCreatedat() {
        return createdat;
    }

    public boolean isLast() {
        return last;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public String getUripath() {
        return uripath;
    }

    public String getRemainTime() {
        return remainTime;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "StoryDetailData{" +
                "id=" + id +
                ", order=" + order +
                ", createdat='" + createdat + '\'' +
                ", last=" + last +
                ", voiceUrl='" + voiceUrl + '\'' +
                ", uripath='" + uripath + '\'' +
                ", remainTime='" + remainTime + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
