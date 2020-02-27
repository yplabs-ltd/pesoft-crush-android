package net.pesofts.crush.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import net.pesofts.crush.Constants;
import net.pesofts.crush.R;
import net.pesofts.crush.activity.ProfileActivity;
import net.pesofts.crush.activity.ShopActivity;
import net.pesofts.crush.fragment.AlertDialogFragment;
import net.pesofts.crush.fragment.BaseDialogFragment;
import net.pesofts.crush.fragment.OneButtonAlertDialogFragment;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.RequestFactory;

import de.greenrobot.event.EventBus;

public class CommonUtil {

    public static boolean isCompleteProfileUser(Context context) {
        String userStatus = SharedPrefHelper.getInstance(context).getSharedPreferences(SharedPrefHelper.USER_STATUS, "");
        if ("N".equals(userStatus)) {
            return true;
        }

        return false;
    }

    public static boolean isFirstReviewingUser(Context context) {
        String userStatus = SharedPrefHelper.getInstance(context).getSharedPreferences(SharedPrefHelper.USER_STATUS, "");
        if ("W".equals(userStatus)) {
            return true;
        }

        return false;
    }

    public static boolean haveEnoughPoint(final FragmentActivity activity, int needPoint) {
        try {
            int point = SharedPrefHelper.getInstance(activity).getSharedPreferences(SharedPrefHelper.POINT, 0);

            if (needPoint <= point) {
                return true;
            } else {
                AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, activity.getString(R.string.need_buchi, needPoint));
                bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, activity.getString(R.string.own_buchi, point));
                bundle.putString(AlertDialogFragment.DIALOG_CONFIRM_NAME, activity.getString(R.string.buchi_charge));
                alertDialogFragment.setArguments(bundle);
                alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                    @Override
                    public void onDialogConfirmed() {
                        Intent intent = new Intent(activity, ShopActivity.class);
                        activity.startActivity(intent);
                    }
                });

                alertDialogFragment.show(activity.getSupportFragmentManager(), "alertDialogFragment");

                return false;
            }
        } catch (Exception e) {
            return true;
        }
    }

    public static void syncPoint(final Context context) {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.MY_INFO_URL, User.class, null, context);
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<User>() {
            @Override
            public void onResponse(User user) {
                if (user != null) {
                    SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.POINT, user.getPoint());
                }
            }
        }).execute();
    }

    public static void syncPointAndStatus(final Context context) {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.MY_INFO_URL, User.class, null, context);
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<User>() {
            @Override
            public void onResponse(User user) {
                if (user != null) {
                    SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.POINT, user.getPoint());
                    if (!SharedPrefHelper.getInstance(context).getSharedPreferences(SharedPrefHelper.USER_STATUS, "").equals(user.getStatus())) {
                        SharedPrefHelper.getInstance(context).setSharedPreferences(SharedPrefHelper.USER_STATUS, user.getStatus());
                        EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.STATUS_CHANGE));
                    }
                }
            }
        }).execute();
    }

    public static boolean isCompleteProfileUserForToday(final Context context) {
        if (CommonUtil.isCompleteProfileUser(context)) {
            return true;
        } else if (CommonUtil.isFirstReviewingUser(context)) {
            OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, context.getString(R.string.review_profile_and_wait_desc));
            oneButtonAlertDialogFragment.setArguments(bundle);
            oneButtonAlertDialogFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "oneButtonAlertDialogFragment");

            return false;
        } else {
            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, context.getString(R.string.fill_profile_info));
            bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, context.getString(R.string.fill_profile_info_description));
            alertDialogFragment.setArguments(bundle);

            alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                @Override
                public void onDialogConfirmed() {
                    final Intent intent = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent);
                }
            });

            alertDialogFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "alertDialogFragment");

            return false;
        }
    }
}
