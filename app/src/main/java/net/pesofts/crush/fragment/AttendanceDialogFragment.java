package net.pesofts.crush.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AttendanceDialogFragment extends BaseDialogFragment {

    @Bind(R.id.id_ico_cash_container)
    ViewGroup icoCashContainer;

    @Bind(R.id.title_text)
    TextView titleText;
    @Bind(R.id.desc_text)
    TextView descText;

    public static AttendanceDialogFragment newInstance(int day) {
        Bundle args = new Bundle();
        args.putInt("key.day", day);

        AttendanceDialogFragment fragment = new AttendanceDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_attendance, container, false);
        ButterKnife.bind(this, contentView);

        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            titleText.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.padding_6dp));
        }

        int day = getArguments().getInt("key.day");
        if (day < 5) {
            descText.setText(getString(R.string.buchi_free_desc, 2));
        } else {
            descText.setText(getString(R.string.buchi_free_desc, 10));
        }

        int count = icoCashContainer.getChildCount();
        for (int i=0; i<count; i++) {
            TextView child = (TextView) icoCashContainer.getChildAt(i);
            if (i < day) {
                child.setText("");
                child.setEnabled(false);
            } else if (i == day) {
                child.setSelected(true);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick(R.id.confirm_button)
    void clickConfirm(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getContext().getPackageName()));
        startActivity(intent);
        dismiss();
    }

    @OnClick(R.id.cancel_button)
    void clickCancel(View view) {
        dismiss();
    }

}
