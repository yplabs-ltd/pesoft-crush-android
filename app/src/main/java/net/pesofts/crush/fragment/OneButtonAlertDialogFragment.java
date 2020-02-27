package net.pesofts.crush.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.pesofts.crush.R;
import net.pesofts.crush.Util.StringUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OneButtonAlertDialogFragment extends BaseDialogFragment {

    private String title;
    private String description;
    private String alert;

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
    @Bind(R.id.divider_line)
    View dividerLine;

    public static OneButtonAlertDialogFragment newInstance() {
        Bundle args = new Bundle();

        OneButtonAlertDialogFragment fragment = new OneButtonAlertDialogFragment();
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
        }

        cancelButton.setVisibility(View.GONE);
        dividerLine.setVisibility(View.GONE);
        confirmButton.setText(R.string.confirm);

        title = getArguments().getString(AlertDialogFragment.DIALOG_TITLE_NAME);
        description = getArguments().getString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME);
        alert = getArguments().getString(AlertDialogFragment.DIALOG_ALERT_NAME);

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
        dismiss();
    }

}
