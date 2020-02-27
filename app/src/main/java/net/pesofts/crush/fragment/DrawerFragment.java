package net.pesofts.crush.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.activity.ContactActivity;
import net.pesofts.crush.activity.MainActivity;
import net.pesofts.crush.activity.ProfileActivity;
import net.pesofts.crush.activity.SettingActivity;
import net.pesofts.crush.activity.ShopActivity;
import net.pesofts.crush.activity.WebViewActivity;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.network.RequestManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class DrawerFragment extends Fragment {

    @Bind(R.id.profile_image_view)
    NetworkImageView profileImageView;
    @Bind(R.id.name_text)
    TextView nameText;
    @Bind(R.id.buchi_count_text)
    TextView buchiCountText;

    private User user;
    private boolean needRefresh = false;

    public static DrawerFragment newInstance(User user) {
        DrawerFragment fragment = new DrawerFragment();

        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainLayout = inflater.inflate(
                R.layout.fragment_drawer, container, false);

        ButterKnife.bind(this, mainLayout);

        this.user = (User) getArguments().getSerializable("user");

        return mainLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EventBus.getDefault().register(this);
        initView();
    }

    private void getMyInfo() {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.MY_INFO_URL, User.class, null, getActivity());
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<User>() {
            @Override
            public void onResponse(User user) {
                if (user != null) {
                    DrawerFragment.this.user = user;
                    initView();
                }
            }
        }).execute();
    }

    private void initView() {
        profileImageView.setImageUrl(user.getMainImageUrl(), RequestManager.getImageLoader());

        if (CommonUtil.isCompleteProfileUser(getActivity())) {
            nameText.setText(user.getName());
        } else if (CommonUtil.isFirstReviewingUser(getActivity())) {
            nameText.setText(R.string.wait_accept);
        } else {
            nameText.setText(R.string.fill_nickname);
        }
    }

    public void onOpenDrawer() {
        CrushApplication.getInstance().loggingView(getString(R.string.ga_drawer));
        int point = SharedPrefHelper.getInstance(getActivity()).getSharedPreferences(SharedPrefHelper.POINT, 0);
        if (buchiCountText != null) {
            buchiCountText.setText(point + "개");
        }

        if (needRefresh) {
            getMyInfo();
        }
    }

    @OnClick(R.id.profile_image_view)
    protected void onClickProfile() {
        final Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);

        closeDrawer();
    }

    @OnClick(R.id.notice_button)
    protected void onClickNotice() {
        closeDrawer();

        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        Bundle extras = new Bundle();
        extras.putString(WebViewActivity.WEB_VIEW_TITLE_NAME, getString(R.string.notice));
        extras.putString(WebViewActivity.WEB_VIEW_URL_NAME, Constants.NOTICE_URL);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @OnClick(R.id.qna_button)
    protected void onClickQna() {
        Uri uri = Uri.parse(Constants.PESOFT_EMAIL);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.do_qna));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_content_qna, user.getId()));
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)));

        closeDrawer();
    }

    @OnClick(R.id.setting_button)
    protected void onClickSetting() {
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        startActivity(intent);

        closeDrawer();
    }

    @OnClick(R.id.charge_button)
    protected void onClickCharge() {
        Intent intent = new Intent(getActivity(), ShopActivity.class);
        startActivity(intent);

        closeDrawer();
    }

    @OnClick(R.id.free_button)
    protected void onClickFree() {
//        Intent intent = new Intent(getActivity(), ContactActivity.class);
//        startActivity(intent);

        AlertDialogFragment oneButtonAlertDialogFragment = AlertDialogFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "이미 받으셨습니다");
        bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, "힘이 되는 리뷰와 별점\n남겨주실 수 있을까요?");
        bundle.putString(AlertDialogFragment.DIALOG_CONFIRM_NAME, "확인");
        bundle.putString(AlertDialogFragment.DIALOG_CANCEL_NAME, "리뷰쓰러가기");
        bundle.putInt(AlertDialogFragment.DIALOG_CANCEL_COLOR, 0xFFF2503B);
        oneButtonAlertDialogFragment.setArguments(bundle);
        oneButtonAlertDialogFragment.setCancelListener(new BaseDialogFragment.CancelListener() {
            @Override
            public void onDialogCancelled(DialogFragment dialog) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getContext().getPackageName()));
                startActivity(intent);
            }
        });

        oneButtonAlertDialogFragment.show(getFragmentManager(), "AlertDialogFragment");

        closeDrawer();
    }

    private void closeDrawer() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).closeDrawer();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void onEvent(RefreshEvent refreshEvent) {
        if (refreshEvent.action == RefreshEvent.Action.STATUS_CHANGE) {
            needRefresh = true;
        }
        LogUtil.d("EvaluateFragment RefreshEvent : " + refreshEvent.action);
    }

}
