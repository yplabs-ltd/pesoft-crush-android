package net.pesofts.crush;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.newrelic.agent.android.NewRelic;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.Util.StringUtil;
import net.pesofts.crush.activity.MainActivity;
import net.pesofts.crush.activity.StartActivity;
import net.pesofts.crush.activity.WebViewActivity;
import net.pesofts.crush.network.RequestManager;

import org.json.JSONObject;

import de.greenrobot.event.EventBus;

public class CrushApplication extends Application {
    private static CrushApplication mInstance;
    private Tracker tracker = null;
    private String gender = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        FacebookSdk.sdkInitialize(getApplicationContext());

        NewRelic.withApplicationToken(
                getString(R.string.new_relic_app_key)
        ).start(this);

//        OneSignal.startInit(this)
//                .setNotificationOpenedHandler(new CrushNotificationOpenedHandler())
//                .init();
//        OneSignal.enableNotificationsWhenActive(true);
//        OneSignal.enableInAppAlertNotification(false);
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationOpenedHandler(new CrushNotificationOpenedHandler())
                .init();

        RequestManager.initialize(this);

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        tracker = analytics.newTracker(R.xml.global_tracker);

    }

    private class CrushNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {

        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            try {
                if (result.notification.payload.additionalData != null) {
                    JSONObject data = result.notification.payload.additionalData;
                    final String type = data.getString("type");

                    if (MainActivity.isActive()) {
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("type", type);

                        startActivity(intent);

                        EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.PUSH, type));
                    } else {
                        Intent intent = new Intent(getBaseContext(), StartActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("type", type);

                        startActivity(intent);
                    }

//                    Log.e("Push", "notificationOpened : " + result.notification.payload.additionalData.toString());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void loggingView(String screenName) {
        tracker.setScreenName(screenName);
        HitBuilders.ScreenViewBuilder screenViewBuilder = new HitBuilders.ScreenViewBuilder();
        if (StringUtil.isNotEmpty(getGender())) {
            screenViewBuilder.setCustomDimension(1, getGender());
        }
        tracker.send(screenViewBuilder.build());
        LogUtil.d("loggingView - " + screenName + ", " + getGender());
    }

    public void loggingEvent(String category, String action, String label) {
        try {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            if (StringUtil.isNotEmpty(getGender())) {
                builder.setCustomDimension(1, getGender());
            }
            tracker.send(builder.setCategory(category).setAction(action).setLabel(label).build());
            LogUtil.d("loggingEvent - category:" + category + " action:" + action + " label:" + label);
        } catch (Exception e) {
        }
    }

    private String getGender() {
        if (StringUtil.isEmpty(gender)) {
            gender = SharedPrefHelper.getInstance(getApplicationContext()).getSharedPreferences(SharedPrefHelper.GENDER, "");
        }
        return gender;
    }

    public static synchronized CrushApplication getInstance() {
        return mInstance;
    }

    public String getAppVersionName() {
        String versionName = "0.0.1";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
        }
        return versionName;
    }

//    private void getKeyHash(){
//            // Add code to print out the key hash
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "net.pesofts.crush",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                LogUtil.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }
//    }


}