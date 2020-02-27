package net.pesofts.crush.permission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import net.pesofts.crush.CrushApplication;

import java.util.Arrays;

/**
 * Created by tommykim on 15. 10. 1..
 */
public class PermissionUtils {

//    public static final String[] CALENDAR = {
//            Manifest.permission.READ_CALENDAR,
//            Manifest.permission.WRITE_CALENDAR
//    };

    public static final String[] CAMERA = {
            Manifest.permission.CAMERA
    };

    public static final String CAMERA_NAME = "카메라";

//    public static final String[] CONTACTS = {
//            Manifest.permission.READ_CONTACTS,
//            Manifest.permission.WRITE_CONTACTS,
//            Manifest.permission.GET_ACCOUNTS
//    };

//    public static final String CONTACTS_NAME = "주소록";

    public static final String[] LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static final String LOCATION_NAME = "위치";

    public static final String[] MICROPHONE = {
            Manifest.permission.RECORD_AUDIO
    };

    public static final String MICROPHONE_NAME = "마이크";

    public static final String[] PHONE = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE
//            Manifest.permission.READ_CALL_LOG,
//            Manifest.permission.WRITE_CALL_LOG,
//            Manifest.permission.ADD_VOICEMAIL,
//            Manifest.permission.USE_SIP,
//            Manifest.permission.PROCESS_OUTGOING_CALLS
    };

    public static final String PHONE_NAME = "전화";

//    public static final String[] SENSORS = {
//            Manifest.permission.BODY_SENSORS
//    };


//    public static final String[] SMS = {
//            Manifest.permission.SEND_SMS,
//            Manifest.permission.RECEIVE_SMS,
//            Manifest.permission.READ_SMS,
//            Manifest.permission.RECEIVE_WAP_PUSH,
//            Manifest.permission.RECEIVE_MMS,
//    };

    public static final String[] STORAGE;

    static {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            STORAGE = new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        } else {
            STORAGE = new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }

    public static final String STORAGE_NAME = "저장";

    public static boolean hasPermissions(String[]... permissionsArray) {
        final Context context = CrushApplication.getInstance();

        if (permissionsArray == null || permissionsArray.length < 1) {
            return false;
        }

        for (String[] permissions : permissionsArray) {
            if (permissions == null || permissions.length < 1) {
                continue;
            }
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }

    public static String[] mergePermissionsForRequest(@NonNull String[]... permissionsArray) {
        int totalLength = 0;
        for (String[] permissions : permissionsArray) {
            if (!hasPermissions(permissions)) {
                totalLength += permissions.length;
            }
        }

        String[] mergedPermissions = null;
        int mergedLength = 0;
        for (String[] permissions: permissionsArray) {
            if (!hasPermissions(permissions)) {
                if (mergedPermissions == null) {
                    mergedPermissions = Arrays.copyOf(permissions, totalLength);
                } else {
                    System.arraycopy(permissions, 0, mergedPermissions, mergedLength, permissions.length);
                }
                mergedLength += permissions.length;
            }
        }
        return mergedPermissions;
    }

    public static String mergePermissionNamesForRequest(@NonNull String... permissionNameArray) {
        if (permissionNameArray.length < 2) {
            return permissionNameArray[0];
        }

        StringBuilder builder = new StringBuilder(permissionNameArray[0]);
        int size = permissionNameArray.length;
        for (int i=1; i<size; i++) {
            if (i == size - 1) {
                builder.append(" 및 ");
            } else {
                builder.append(", ");
            }
            builder.append(permissionNameArray[i]);
        }
        return builder.toString();
    }

}
