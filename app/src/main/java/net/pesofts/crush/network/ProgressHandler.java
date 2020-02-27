package net.pesofts.crush.network;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import net.pesofts.crush.fragment.ProgressDialogFragment;


public class ProgressHandler<T> extends Interceptor<T> {

    private FragmentActivity activity;
    private boolean cancelable;
    private DialogFragment progress;
    private boolean showOnlyProgress;

    public ProgressHandler(FragmentActivity activity, boolean cancelable) {
        this(activity, cancelable, false);
    }

    public ProgressHandler(FragmentActivity activity, boolean cancelable, boolean showOnlyProgress) {
        this.activity = activity;
        this.cancelable = cancelable;
        this.showOnlyProgress = showOnlyProgress;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public Activity getActivity() {
        return activity;
    }

    @Override
    public void onStart() {
        if (activity != null && !activity.isFinishing()) {
            try {
                if (!showOnlyProgress) {
                    ProgressDialogFragment progressDialog = ProgressDialogFragment.newInstance();
//                        progressDialog.setIndeterminate(true);
//                        progressDialog.setInverseBackgroundForced(true);
                    progress = progressDialog;
                } else {
                    progress = ProgressDialogFragment.newInstance();
                }

                if (progress != null) {
                    progress.setCancelable(cancelable);
                    progress.show(activity.getSupportFragmentManager(), "ProgressDialogFragment");
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onCancel() {
        dismissDialog();
    }

    @Override
    public void onError(HttpNetworkError error) {
        dismissDialog();
    }

    @Override
    public void onResponse(T response) {
        dismissDialog();
    }

    private void dismissDialog() {
        if (progress != null && progress.isVisible()) {
            progress.dismissAllowingStateLoss();
            progress = null;
        }
    }
}
