package net.pesofts.crush.permission;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tommykim on 15. 10. 13..
 */
public class PermissionRequestParams implements Parcelable {

    public static final String KEY = "key.permission.request.params";

    public String[] permissions;
    public String permissionNames;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(this.permissions);
        dest.writeString(this.permissionNames);
    }

    public PermissionRequestParams() {
    }

    protected PermissionRequestParams(Parcel in) {
        this.permissions = in.createStringArray();
        this.permissionNames = in.readString();
    }

    public static final Parcelable.Creator<PermissionRequestParams> CREATOR = new Parcelable.Creator<PermissionRequestParams>() {
        public PermissionRequestParams createFromParcel(Parcel source) {
            return new PermissionRequestParams(source);
        }

        public PermissionRequestParams[] newArray(int size) {
            return new PermissionRequestParams[size];
        }
    };
}
