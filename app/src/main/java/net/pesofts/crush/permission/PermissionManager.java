package net.pesofts.crush.permission;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

/**
 * Created by tommykim on 15. 10. 2..
 */
public class PermissionManager {

    public static final String PERMISSION_RESULT_RECEIVER_KEY = "permission.result.receiver";

    private static class InstanceHolder {
        private static final PermissionManager INSTANCE;

        static {
            PermissionManager temp = new PermissionManager();
            INSTANCE = temp;
        }
    }

    public static PermissionManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private Handler handler;

    private PermissionManager() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void requestPermission(Context context,
                                  String[] permissions,
                                  String permissionNames,
                                  final PermissionListener permissionListener) {

        if (PermissionUtils.hasPermissions(permissions)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    permissionListener.onGranted();
                }
            });
            return;
        }

        Intent intent = new Intent(context, PermissionRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        PermissionRequestParams params = new PermissionRequestParams();
        params.permissions = permissions;
        params.permissionNames = permissionNames;
        intent.putExtra(PermissionRequestParams.KEY, params);
        intent.putExtra(PERMISSION_RESULT_RECEIVER_KEY, new ResultReceiver(handler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {

                final boolean hasPermission = resultCode == PackageManager.PERMISSION_GRANTED;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (hasPermission) {
                            permissionListener.onGranted();
                        } else {
                            permissionListener.onDenied();
                        }
                    }
                });
            }
        });
        context.startActivity(intent);

    }

}
