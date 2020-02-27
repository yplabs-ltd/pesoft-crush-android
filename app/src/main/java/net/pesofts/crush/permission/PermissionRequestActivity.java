package net.pesofts.crush.permission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.pesofts.crush.R;

/**
 * Created by tommykim on 15. 10. 1..
 */
public class PermissionRequestActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 0x00;
    private static final int APP_DETAIL_SETTINGS_CODE = 0x1000;

    private PermissionRequestParams params;
    private ResultReceiver resultReceiver;
    private View view;
    private Snackbar guideSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (savedInstanceState != null || isLaunchedFromHistory(intent)) {
            finish();
            return;
        }

        params = intent.getParcelableExtra(PermissionRequestParams.KEY);
        resultReceiver = intent.getParcelableExtra(PermissionManager.PERMISSION_RESULT_RECEIVER_KEY);

        setContentView(R.layout.activity_permission_request);
        view = findViewById(R.id.permission_request);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (guideSnackbar != null && guideSnackbar.isShown()) {
                    guideSnackbar.dismiss();
                }
            }
        });

        ActivityCompat.requestPermissions(this, params.permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE || permissions == null || grantResults == null) {
            onPermissionResult(false);
            return;
        }

        boolean hasPermission = true;
        int size = permissions.length;
        for (String request : params.permissions) {
            for (int i=0; i<size; i++) {
                if (request.equals(permissions[i])) {
                    hasPermission = hasPermission && (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
            }
        }

        if (!hasPermission && !ActivityCompat.shouldShowRequestPermissionRationale(this, params.permissions[0])) {
            guideSnackbar = Snackbar.make(view, getString(R.string.allow_permission_via_manage_app_permissions, params.permissionNames), Snackbar.LENGTH_INDEFINITE);
            guideSnackbar.setAction(R.string.move_to_app_detail_settings, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent viewIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    viewIntent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                    startActivityForResult(viewIntent, APP_DETAIL_SETTINGS_CODE);
                }
            });
            guideSnackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    switch (event) {
                        case Snackbar.Callback.DISMISS_EVENT_MANUAL:
                        case Snackbar.Callback.DISMISS_EVENT_SWIPE:
                        case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                            onPermissionResult(false);
                            break;
                    }

                }

                @Override
                public void onShown(Snackbar snackbar) {
                    super.onShown(snackbar);
                }
            });
            guideSnackbar.show();
        } else {
            onPermissionResult(hasPermission);
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_DETAIL_SETTINGS_CODE) {
            onPermissionResult(PermissionUtils.hasPermissions(params.permissions));
        }
    }

    private boolean isLaunchedFromHistory(Intent intent) {
        return intent != null &&
                (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY;
    }

    private void onPermissionResult(boolean hasPermission) {
        finish();
        if (resultReceiver != null) {
            resultReceiver.send(hasPermission ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED, null);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
