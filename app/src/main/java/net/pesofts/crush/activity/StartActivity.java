package net.pesofts.crush.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.OneSignalUtil;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.fragment.AlertDialogFragment;
import net.pesofts.crush.fragment.BaseDialogFragment;
import net.pesofts.crush.fragment.OneButtonAlertDialogFragment;
import net.pesofts.crush.model.SystemCheck;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpNetworkError;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.RequestFactory;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivity extends FragmentActivity {

    @Bind(R.id.sign_in_button)
    Button signInButton;
    @Bind(R.id.sign_up_button)
    Button signUpButton;
    @Bind(R.id.bird_image)
    ImageView birdImage;

    SystemCheck systemCheckResponse;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        ButterKnife.bind(this);

        ((Animatable) birdImage.getDrawable()).start();

        setBadge(getApplicationContext(), 0);   // badge count remove.

        final String accessToken = SharedPrefHelper.getInstance(getApplicationContext()).getSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, "");
//        final String accessToken = "";
        if (accessToken.length() <= 0) {
            signInButton.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.VISIBLE);

            CrushApplication.getInstance().loggingView(getString(R.string.ga_start));

            OneSignalUtil.deleteTags();
        }

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.SYSTEM_CHECK_URL, SystemCheck.class, null, getApplicationContext());
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<SystemCheck>() {
            @Override
            public void onResponse(SystemCheck response) {
                LogUtil.d("SYSTEM_CHECK_URL onResponse");
                if (response.isOK()) {
                    systemCheckResponse = response;

                    if (!response.isUsableVersion(CrushApplication.getInstance().getAppVersionName())) {
                        OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.update_alert));
                        bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, getString(R.string.update_alert_desc));
                        oneButtonAlertDialogFragment.setArguments(bundle);

                        oneButtonAlertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                            @Override
                            public void onDialogConfirmed() {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                                startActivity(intent);
                            }
                        });

                        oneButtonAlertDialogFragment.show(getSupportFragmentManager(), "oneButtonAlertDialogFragment");

                        return;
                    }
                    if (accessToken.length() > 0) {
                        initUserInfo();
                    }
                } else {
                    OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, response.getTitle());
                    bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, response.getDescription());
                    oneButtonAlertDialogFragment.setArguments(bundle);

                    oneButtonAlertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                        @Override
                        public void onDialogConfirmed() {
                            finish();
                        }
                    });

                    oneButtonAlertDialogFragment.show(getSupportFragmentManager(), "oneButtonAlertDialogFragment");

                    return;
                }
            }

            @Override
            public void onError(HttpNetworkError error) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_init), Toast.LENGTH_LONG).show();
                finish();
            }
        }).execute();
    }

    private void initUserInfo() {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.MY_INFO_URL, User.class, null, getApplicationContext());
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<User>() {
            @Override
            public void onResponse(User user) {
                if (user != null) {
                    SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.USER_STATUS, user.getStatus());
                    SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.USER_ID, user.getId());
                    SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.POINT, user.getPoint());
                    SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.GENDER, user.getGender());
                }
                moveToMainActivity(user);
            }

            @Override
            public void onError(HttpNetworkError error) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_init), Toast.LENGTH_LONG).show();
                finish();
            }
        }).execute();
    }

    private void moveToMainActivity(User user) {
        final Intent intent = new Intent(this, MainActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("user", user);
        extras.putSerializable("systemCheckResponse", systemCheckResponse);
        Intent pushIntent = getIntent();
        if (pushIntent != null) {
            String type = pushIntent.getStringExtra("type");
            intent.putExtra("type", type);
        }

        intent.putExtras(extras);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.sign_in_button)
    void onClickSignIn() {
        final Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.sign_up_button)
    void onClickSignUp() {
        final Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }

    public void setBadge(Context context, int count) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String launcherClassName = launchIntent.getComponent().getClassName();

        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", packageName);
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

}
