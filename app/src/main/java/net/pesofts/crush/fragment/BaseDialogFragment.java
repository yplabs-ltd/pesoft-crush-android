package net.pesofts.crush.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import net.pesofts.crush.R;

public class BaseDialogFragment extends DialogFragment {

    public interface ConfirmListener {
        void onDialogConfirmed();
    }

    public interface DismissListener {
        void onDialogDismissed(DialogFragment dialog);
    }

    public interface CancelListener {
        void onDialogCancelled(DialogFragment dialog);
    }

    public interface ButtonListener {
        void onDialogButtonClicked();
    }

    protected DismissListener dismissListener;

    protected CancelListener cancelListener;

    protected ConfirmListener confirmListener;

    public BaseDialogFragment() {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TransparentDialogFragment);
    }

    public void setDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public void setCancelListener(CancelListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    public void setConfirmListener(ConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        executeCancelListener();
    }

    protected void executeCancelListener() {
        if (cancelListener != null) {
            cancelListener.onDialogCancelled(this);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onDialogDismissed(this);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (isCancelable()) {
            dialog.setCanceledOnTouchOutside(true);
        }
        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (tag != null && manager.findFragmentByTag(tag) != null) {
            return;
        }
        try {
            // Fragment를 Activity가 Pause된 후에 이후에 보여주려고 하면 오류가 발생한다.
            // 다른 방법으로는 isActive를 확인해서 하는 방법도 있다.
            super.show(manager, tag);
        } catch (IllegalStateException ignore) {
        }
    }

    @Override
    public void dismiss() {
        if (isResumed()) {
            super.dismiss();
        }
    }
}
