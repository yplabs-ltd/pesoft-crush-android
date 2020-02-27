package net.pesofts.crush.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by erkas on 2017. 5. 8..
 *
 * {
 "urlpath" : "\/profile\/20161220\/1076621482221674165",
 "modifiedat" : "2017-04-22T07:27:29.698Z",
 "id" : "24",
 "largeImageUrl" : "http:\/\/imgcdn.ablesquare.co.kr\/720x\/filters:format(jpeg)\/image.ablesquare.co.kr\/profile\/20161220\/1076621482221674165",
 "count" : "2",
 "remainTime" : "75.5",
 "name" : "  니꿍꺼또",
 "new" : false
 }
 */

public class StoryData implements Parcelable {

    private String urlpath;
    private String modifiedat;
    private String id;
    private String smallImageUrl;
    private String largeImageUrl;
    private String count;
    private String remainTime;
    private String name;
    @SerializedName("new")
    private boolean isNew;

    public String getUrlpath() {
        return urlpath;
    }

    public String getModifiedat() {
        return modifiedat;
    }

    public String getId() {
        return id;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

//    public String getLargeImageUrl() {
//        return largeImageUrl;
//    }

    public String getCount() {
        return count;
    }

    public String getRemainTime() {
        return remainTime;
    }

    public String getName() {
        return name;
    }

    public boolean isNew() {
        return isNew;
    }

    @Override
    public String toString() {
        return "StoryData{" +
                "urlpath='" + urlpath + '\'' +
                ", modifiedat='" + modifiedat + '\'' +
                ", id='" + id + '\'' +
                ", smallImageUrl='" + smallImageUrl + '\'' +
                ", largeImageUrl='" + largeImageUrl + '\'' +
                ", count='" + count + '\'' +
                ", remainTime='" + remainTime + '\'' +
                ", name='" + name + '\'' +
                ", isNew=" + isNew +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.urlpath);
        dest.writeString(this.modifiedat);
        dest.writeString(this.id);
        dest.writeString(this.smallImageUrl);
        dest.writeString(this.largeImageUrl);
        dest.writeString(this.count);
        dest.writeString(this.remainTime);
        dest.writeString(this.name);
        dest.writeByte(this.isNew ? (byte) 1 : (byte) 0);
    }

    public StoryData() {
    }

    protected StoryData(Parcel in) {
        this.urlpath = in.readString();
        this.modifiedat = in.readString();
        this.id = in.readString();
        this.smallImageUrl = in.readString();
        this.largeImageUrl = in.readString();
        this.count = in.readString();
        this.remainTime = in.readString();
        this.name = in.readString();
        this.isNew = in.readByte() != 0;
    }

    public static final Parcelable.Creator<StoryData> CREATOR = new Parcelable.Creator<StoryData>() {
        @Override
        public StoryData createFromParcel(Parcel source) {
            return new StoryData(source);
        }

        @Override
        public StoryData[] newArray(int size) {
            return new StoryData[size];
        }
    };
}
