package net.pesofts.crush.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.pesofts.crush.R;
import net.pesofts.crush.Util.StringUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlertDialogFragment extends BaseDialogFragment {

    public static final String DIALOG_TITLE_NAME = "title";
    public static final String DIALOG_DESCRIPTION_NAME = "description";
    public static final String DIALOG_ALERT_NAME = "alert";
    public static final String DIALOG_EDITABLE_NAME = "editable";
    public static final String DIALOG_SIGNOUT_EDITABLE_NAME = "signout_editable";
    public static final String DIALOG_SIGNOUT_EDITABLE_HINT = "signout_editable_hint";
    public static final String DIALOG_CONFIRM_NAME = "confirm";
    public static final String DIALOG_CANCEL_NAME = "cancel";
    public static final String DIALOG_CANCEL_COLOR = "cancel.color";

    @Bind(R.id.title_text)
    TextView titleText;
    @Bind(R.id.description_text)
    TextView descriptionText;
    @Bind(R.id.alert_text)
    TextView alertText;
    @Bind(R.id.confirm_button)
    Button confirmButton;
    @Bind(R.id.cancel_button)
    Button cancelButton;
    @Bind(R.id.like_edit_text)
    EditText likeEditText;
    @Bind(R.id.dialog_layout)
    RelativeLayout dialogLayout;

    protected EditableConfirmListener editableConfirmListener;
    private boolean isReport;

    public void setReport(boolean report) {
        isReport = report;
    }

    public interface EditableConfirmListener {
        void onDialogConfirmed(String editString);
    }

    public void setEditableConfirmListener(EditableConfirmListener editableConfirmListener) {
        this.editableConfirmListener = editableConfirmListener;
    }

    public static AlertDialogFragment newInstance() {
        Bundle args = new Bundle();

        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_alert, container, false);
        ButterKnife.bind(this, contentView);

        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            titleText.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.padding_6dp));
            descriptionText.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.padding_6dp));
            alertText.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.padding_6dp));
        }

        String title = getArguments().getString(DIALOG_TITLE_NAME);
        String description = getArguments().getString(DIALOG_DESCRIPTION_NAME);
        String alert = getArguments().getString(DIALOG_ALERT_NAME);
        String confirm = getArguments().getString(DIALOG_CONFIRM_NAME);
        String cancel = getArguments().getString(DIALOG_CANCEL_NAME);
        int cancelColor = getArguments().getInt(DIALOG_CANCEL_COLOR);
        boolean isEditable = getArguments().getBoolean(DIALOG_EDITABLE_NAME);
        boolean isSignOutEditable = getArguments().getBoolean(DIALOG_SIGNOUT_EDITABLE_NAME);
        String isSignOutEditableHint = getArguments().getString(DIALOG_SIGNOUT_EDITABLE_HINT);

        if (isReport) {
            titleText.setTextColor(ResourcesCompat.getColor(getResources(), R.color.main_red_color, null));
            descriptionText.setTextColor(ResourcesCompat.getColor(getResources(), R.color.main_text_color, null));
            alertText.setTextColor(ResourcesCompat.getColor(getResources(), R.color.main_text_color, null));
        }

        if (StringUtil.isNotEmpty(title)) {
            titleText.setText(title);
        } else {
            titleText.setVisibility(View.GONE);
        }

        if (StringUtil.isNotEmpty(description)) {
            descriptionText.setText(description);
        } else {
            descriptionText.setVisibility(View.GONE);
        }

        if (StringUtil.isNotEmpty(alert)) {
            alertText.setText(alert);
        } else {
            alertText.setVisibility(View.GONE);
        }

        if (StringUtil.isNotEmpty(confirm)) {
            confirmButton.setText(confirm);
        }

        if (StringUtil.isNotEmpty(cancel)) {
            cancelButton.setText(cancel);
            if (cancelColor != 0) {
                cancelButton.setTextColor(ResourcesCompat.getColor(getResources(), R.color.main_red_color, null));
            }

        }

        if (isEditable || isSignOutEditable) {
            likeEditText.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams layoutParams = dialogLayout.getLayoutParams();
            if (isEditable) {
//                layoutParams.height = getResources().getDimensionPixelSize(R.dimen.like_dialog_height_large);
            } else if (isSignOutEditable) {
//                layoutParams.height = getResources().getDimensionPixelSize(R.dimen.like_dialog_height_signout);
                likeEditText.setHint(isSignOutEditableHint);
            }

            likeEditText.addTextChangedListener(new TextWatcher() {
                String previousString = "";

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    previousString = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (likeEditText.getLineCount() >= 6) {
                        likeEditText.setText(previousString);
                        likeEditText.setSelection(likeEditText.length());
                    }
                }
            });
        }

        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick(R.id.confirm_button)
    void clickConfirm(View view) {
        if (confirmListener != null) {
            confirmListener.onDialogConfirmed();
        }
        if (editableConfirmListener != null) {
            editableConfirmListener.onDialogConfirmed(likeEditText.getText().toString());
        }
        dismiss();
    }

    @OnClick(R.id.cancel_button)
    void clickCancel(View view) {
        if (cancelListener != null) {
            cancelListener.onDialogCancelled(this);
        }
        dismiss();
    }

}
