package net.pesofts.crush.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.sendbird.android.sample.SendBirdMessagingChannelListActivity;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.DateUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.Util.StringUtil;
import net.pesofts.crush.fragment.AlertDialogFragment;
import net.pesofts.crush.fragment.BaseDialogFragment;
import net.pesofts.crush.fragment.OneButtonAlertDialogFragment;
import net.pesofts.crush.model.Image;
import net.pesofts.crush.model.Result;
import net.pesofts.crush.model.User;
import net.pesofts.crush.model.UserInfoCode;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.network.RequestManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class UserDetailActivity extends FragmentActivity {

    private List<NetworkImageView> profileImageViewList = new ArrayList<>();

    @Bind(R.id.profile_image_view)
    NetworkImageView profileImageView;
    @Bind(R.id.profile_image_view1)
    NetworkImageView profileImageView1;
    @Bind(R.id.profile_image_view2)
    NetworkImageView profileImageView2;
    @Bind(R.id.profile_image_view3)
    NetworkImageView profileImageView3;
    @Bind(R.id.profile_image_view4)
    NetworkImageView profileImageView4;
    @Bind(R.id.profile_image_view5)
    NetworkImageView profileImageView5;
    @Bind(R.id.profile_image_view6)
    NetworkImageView profileImageView6;
    @Bind(R.id.name_text)
    TextView nameText;
    @Bind(R.id.age_text)
    TextView ageText;
    @Bind(R.id.blood_type_text)
    TextView bloodTypeText;
    @Bind(R.id.job_text)
    TextView jobText;
    @Bind(R.id.body_text)
    TextView bodyText;
    @Bind(R.id.religion_text)
    TextView religionText;
    @Bind(R.id.ideal_text)
    TextView idealText;
    @Bind(R.id.region_text)
    TextView regionText;
    @Bind(R.id.height_text)
    TextView heightText;
    @Bind(R.id.school_text)
    TextView schoolText;
    @Bind(R.id.like_button)
    Button likeButton;
    @Bind(R.id.dislike_button)
    Button dislikeButton;
    @Bind(R.id.button_layout)
    LinearLayout buttonLayout;
    @Bind(R.id.message_layout)
    RelativeLayout messageLayout;
    @Bind(R.id.message_text)
    TextView messageText;
    @Bind(R.id.message_open_button)
    ImageButton messageOpenButton;
    @Bind(R.id.interest_title)
    TextView interestTitle;
    @Bind(R.id.attractive_title)
    TextView attractiveTitle;
    @Bind(R.id.favor_title)
    TextView favorTitle;
    @Bind(R.id.interest_layout)
    FlowLayout interestLayout;
    @Bind(R.id.attractive_layout)
    FlowLayout attractiveLayout;
    @Bind(R.id.favor_layout)
    FlowLayout favorLayout;
    @Bind(R.id.extra_info_layout)
    LinearLayout extraInfoLayout;

    private User user;
    private String fromType;
    private boolean fromChat;
    private boolean fromHidden;
    private boolean fromILike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        ButterKnife.bind(this);

        CrushApplication.getInstance().loggingView(getString(R.string.ga_user_detail));

        profileImageView.setDefaultImageResId(R.drawable.home_thumbnail_default_detail);

        profileImageViewList.add(profileImageView1);
        profileImageViewList.add(profileImageView2);
        profileImageViewList.add(profileImageView3);
        profileImageViewList.add(profileImageView4);
        profileImageViewList.add(profileImageView5);
        profileImageViewList.add(profileImageView6);

        for (NetworkImageView networkImageView : profileImageViewList) {
            networkImageView.setDefaultImageResId(R.drawable.detail_btn_lock_normal);
        }

        interestTitle.setText('"' + getString(R.string.interest_about) + '"');
        attractiveTitle.setText('"' + getString(R.string.attractive_about) + '"');
        favorTitle.setText('"' + getString(R.string.favor_about) + '"');

        User user = (User) getIntent().getSerializableExtra("user");
        fromType = getIntent().getStringExtra("fromType");
        fromChat = getIntent().getBooleanExtra("fromChat", false);
        fromHidden = getIntent().getBooleanExtra("fromHidden", false);
        fromILike = getIntent().getBooleanExtra("fromILike", false);
        getUserInfo(user);

    }

    private void getUserInfo(User user) {
        List<NameValuePair> paramInfo = new ArrayList<>();
        paramInfo.add(new BasicNameValuePair("userid", user.getId()));
        if (fromILike) {
            paramInfo.add(new BasicNameValuePair("likecheck", "true"));
        }

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.USER_DETAIL_URL, User.class, paramInfo, getApplicationContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setProgressHandler(new ProgressHandler(this, false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<User>() {
            @Override
            public void onResponse(User user) {
                LogUtil.d("UserInfo", user);

                if (user != null) {
                    initView(user);
                }
            }
        }).execute();
    }

    private void changeMainImage(int num) {
        if (user.getImageInfoList() != null && user.getImageInfoList().size() > num) {
            Image image = user.getImageInfoList().get(num);
            if (StringUtil.isNotEmpty(image.getLargeImageUrl())) {
                profileImageView.setImageUrl(image.getLargeImageUrl(), RequestManager.getImageLoader());
            }
        }
    }

    private void initView(final User user) {
        this.user = user;

        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            idealText.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.new_more_list_item_padding));
            messageText.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.padding_15dp));
        }

        changeMainImage(0);

        int userImageCount = 0;

        if (user.getImageInfoList() != null) {
            userImageCount = user.getImageInfoList().size();
            if (userImageCount > profileImageViewList.size()) {
                userImageCount = profileImageViewList.size();
            }
            for (int i = 0; i < userImageCount; i++) {
                profileImageViewList.get(i).setVisibility(View.VISIBLE);
                final int num = i;
                Image image = user.getImageInfoList().get(i);
                if (StringUtil.isNotEmpty(image.getImageUrl())) {
                    profileImageViewList.get(i).setImageUrl(image.getImageUrl(), RequestManager.getImageLoader());
                    profileImageViewList.get(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changeMainImage(num);
                        }
                    });
                } else {
                    profileImageViewList.get(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CrushApplication.getInstance().loggingEvent(getString(R.string.ga_user_detail), getString(R.string.ga_photo_more), getString(R.string.ga_try));
                            if (CommonUtil.haveEnoughPoint(UserDetailActivity.this, Constants.BUCHI_COUNT_FOR_MORE_PICTURE)) {
                                AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                                Bundle bundle = new Bundle();
                                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.ask_see_more_picture));
                                bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, getString(R.string.ask_see_more_picture_desc));
                                bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, getString(R.string.needed_buchi_count, Constants.BUCHI_COUNT_FOR_MORE_PICTURE));
                                alertDialogFragment.setArguments(bundle);
                                alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                                    @Override
                                    public void onDialogConfirmed() {
                                        List<NameValuePair> paramInfo = new ArrayList<>();
                                        paramInfo.add(new BasicNameValuePair("userid", user.getId()));

                                        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.USER_OPEN_IMAGE_URL, Image.class, paramInfo, HttpMethod.POST, getApplicationContext());
                                        RequestFactory requestFactory = new RequestFactory();
                                        requestFactory.setProgressHandler(new ProgressHandler(UserDetailActivity.this, false));
                                        requestFactory.create(httpRequestVO, new HttpResponseCallback<Image>() {
                                            @Override
                                            public void onResponse(Image image) {
                                                CommonUtil.syncPoint(getApplicationContext());

                                                setNewImage(image);
                                            }
                                        }).execute();
                                        CrushApplication.getInstance().loggingEvent(getString(R.string.ga_user_detail), getString(R.string.ga_photo_more), getString(R.string.ga_confirm));
                                    }
                                });

                                alertDialogFragment.show(getSupportFragmentManager(), "alertDialogFragment");
                            }

                        }
                    });
                }
            }
        }

        for (int i = userImageCount; i < profileImageViewList.size(); i++) {
            profileImageViewList.get(i).setVisibility(View.GONE);
        }

        setUserOneInfoByString(nameText, user.getName());
        setUserOneInfoByCode(idealText, user.getIdealType());
        setUserOneInfoByString(ageText, DateUtil.getAgeString(user.getBirthDate()));
        setUserOneInfoByCode(regionText, user.getHometown());
        setUserOneInfoByCode(jobText, user.getJob());

        if (user.getHeight() > 0) {
            setUserOneInfoByString(heightText, Integer.toString(user.getHeight()) + "cm");
        } else {
            setUserOneInfoByString(heightText, null);
        }

        setUserOneInfoByCode(bodyText, user.getBodyType());
        setUserOneInfoByCode(religionText, user.getReligion());
        setUserOneInfoByCode(bloodTypeText, user.getBloodType());
        setUserOneInfoByString(schoolText, user.getSchool());

        setUserExtraInfo(user);

        setLikeButton();
        setLikeMessage();
    }

    private void setUserExtraInfo(User user) {
        if ((user.getHobbyList() == null || user.getHobbyList().size() == 0)
                && (user.getCharmTypeList() == null || user.getCharmTypeList().size() == 0)
                && (user.getFavoriteTypeList() == null || user.getFavoriteTypeList().size() == 0)) {

            extraInfoLayout.setVisibility(View.GONE);
        } else {
            extraInfoLayout.setVisibility(View.VISIBLE);

            if (user.getHobbyList() != null && user.getHobbyList().size() > 0) {
                setKeywordTextViewList(interestLayout, user.getHobbyList());

                interestLayout.setVisibility(View.VISIBLE);
                interestTitle.setVisibility(View.VISIBLE);
            } else {
                interestLayout.setVisibility(View.GONE);
                interestTitle.setVisibility(View.GONE);
            }

            if (user.getCharmTypeList() != null && user.getCharmTypeList().size() > 0) {
                setKeywordTextViewList(attractiveLayout, user.getCharmTypeList());

                attractiveLayout.setVisibility(View.VISIBLE);
                attractiveTitle.setVisibility(View.VISIBLE);
            } else {
                attractiveLayout.setVisibility(View.GONE);
                attractiveTitle.setVisibility(View.GONE);
            }

            if (user.getFavoriteTypeList() != null && user.getFavoriteTypeList().size() > 0) {
                setKeywordTextViewList(favorLayout, user.getFavoriteTypeList());

                favorLayout.setVisibility(View.VISIBLE);
                favorTitle.setVisibility(View.VISIBLE);
            } else {
                favorLayout.setVisibility(View.GONE);
                favorTitle.setVisibility(View.GONE);
            }
        }
    }

    private void setKeywordTextViewList(ViewGroup rootView, List<UserInfoCode> codeList) {
        for (UserInfoCode code : codeList) {
            TextView textView = (TextView) LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.incloud_profile_text_red_box, rootView, false);
            textView.setText(code.getValue());

            rootView.addView(textView, 0);
        }

    }

    private void setNewImage(Image image) {
        if (user.getImageInfoList() != null) {
            int userImageCount = user.getImageInfoList().size();
            for (int i = 0; i < userImageCount; i++) {
                final int num = i;
                Image oriImage = user.getImageInfoList().get(i);
                if (oriImage.getOrdering() == image.getOrdering()) {
                    profileImageViewList.get(i).setImageUrl(image.getImageUrl(), RequestManager.getImageLoader());
                    profileImageViewList.get(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changeMainImage(num);
                        }
                    });

                    oriImage.setImageUrl(image.getImageUrl());
                    oriImage.setLargeImageUrl(image.getLargeImageUrl());

                    changeMainImage(num);
                    break;
                }
            }
        }
    }

    private void setLikeMessage() {
        if (user.isLikeMe() && StringUtil.isNotEmpty(user.getLikeMeMessage()) && !"R".equals(user.getReply())) {
            messageLayout.setVisibility(View.VISIBLE);
            messageText.setText("\"" + user.getLikeMeMessage() + "\"");
        } else {
            messageLayout.setVisibility(View.GONE);
        }
    }

    private void setLikeButton() {
        if (fromChat) {
            buttonLayout.setVisibility(View.GONE);
        } else if (user.isFavorMatch() || "A".equals(user.getReply())) {
            likeButton.setVisibility(View.VISIBLE);
            dislikeButton.setVisibility(View.GONE);
            likeButton.setText(R.string.move_to_chat);
        } else if (fromHidden) {
            likeButton.setVisibility(View.VISIBLE);
            dislikeButton.setVisibility(View.GONE);
            Spannable buttonLabel = new SpannableString("   " + getString(R.string.I_favor_too));
            buttonLabel.setSpan(new ImageSpan(getApplicationContext(), R.drawable.detail_ico_like,
                    ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            likeButton.setText(buttonLabel);
        } else if (user.isILike() && "R".equals(user.getReply())) {
            likeButton.setVisibility(View.VISIBLE);
            dislikeButton.setVisibility(View.GONE);

            Spannable buttonLabel = new SpannableString("  " + getString(R.string.like_again));
            buttonLabel.setSpan(new ImageSpan(getApplicationContext(), R.drawable.detail_ico_bird,
                    ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            likeButton.setText(buttonLabel);
        } else if (user.isILike()) {
            likeButton.setVisibility(View.GONE);
            dislikeButton.setVisibility(View.VISIBLE);
            dislikeButton.setEnabled(false);
            if (user.isLikecheck()) {
                dislikeButton.setText(R.string.like_check);
            } else {
                dislikeButton.setText(R.string.wait_response);
            }
        } else if (user.isLikeMe() && !"R".equals(user.getReply())) {
            likeButton.setVisibility(View.VISIBLE);
            dislikeButton.setVisibility(View.VISIBLE);
            likeButton.setText(R.string.response_like);
            dislikeButton.setText(R.string.dislike);
        } else {
            likeButton.setVisibility(View.VISIBLE);
            dislikeButton.setVisibility(View.GONE);

            Spannable buttonLabel = new SpannableString("  " + getString(R.string.like));
            buttonLabel.setSpan(new ImageSpan(getApplicationContext(), R.drawable.detail_ico_bird,
                    ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            likeButton.setText(buttonLabel);
        }
    }

    private void setUserOneInfoByString(TextView textView, String value) {
        if (StringUtil.isNotEmpty(value)) {
            textView.setText(value);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private void setUserOneInfoByCode(TextView textView, UserInfoCode userInfoCode) {
        if (userInfoCode != null) {
            textView.setText(userInfoCode.getValue());
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private void favorUser() {

        List<NameValuePair> paramInfo = new ArrayList<>();
        paramInfo.add(new BasicNameValuePair("userid", user.getId()));

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.REPLY_FAVOR_CARD_URL, Result.class, paramInfo, HttpMethod.POST, getApplicationContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setProgressHandler(new ProgressHandler(UserDetailActivity.this, false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<Result>() {
            @Override
            public void onResponse(Result result) {
                user.setFavorMatch(true);
                setLikeButton();

                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.LIKE));
            }
        }).execute();

        CrushApplication.getInstance().loggingEvent(getString(R.string.ga_user_detail), getString(R.string.ga_favor_in_detail), getString(R.string.ga_favor_in_detail));

    }

    @OnClick(R.id.like_button)
    void clickLikeButton() {
        if (user.isFavorMatch() || "A".equals(user.getReply())) {
            Intent intent = new Intent(UserDetailActivity.this, SendBirdMessagingChannelListActivity.class);
            startActivity(intent);
        } else if (fromHidden) {
            favorUser();
        } else if (user.isLikeMe() && !"R".equals(user.getReply())) {
            replyLike("A");
            CrushApplication.getInstance().loggingEvent(getString(R.string.ga_user_detail), getString(R.string.ga_like), getString(R.string.ga_accept));
        } else {
            CrushApplication.getInstance().loggingEvent(getString(R.string.ga_user_detail), getString(R.string.ga_like), getString(R.string.ga_like));

            int needPoint = 0;
//            if (user.isILike() && "R".equals(user.getReply())) {
//                needPoint = Constants.BUCHI_COUNT_FOR_LIKE_AGAIN;
//            } else if ("H".equals(fromType)) {
//                needPoint = Constants.BUCHI_COUNT_FOR_LIKE_PAST_CARD;
//            } else if ("EV".equals(fromType)) {
//                needPoint = Constants.BUCHI_COUNT_FOR_LIKE_PAST_CARD;
//            } else {
//                needPoint = Constants.BUCHI_COUNT_FOR_LIKE;
//            }
            if ("N".equals(fromType)) {
                needPoint = Constants.BUCHI_COUNT_FOR_LIKE;
            } else {
                needPoint = Constants.BUCHI_COUNT_FOR_LIKE_PAST_CARD;
            }

            if (CommonUtil.haveEnoughPoint(this, needPoint)) {
                CrushApplication.getInstance().loggingEvent(getString(R.string.ga_user_detail), getString(R.string.ga_like), getString(R.string.ga_like_enough_money));

                AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.ask_send_like));
                if (user.isILike() && "R".equals(user.getReply())) {
                    bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.ask_send_like_again));
                }
                bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, getString(R.string.send_like_info));
                bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, getString(R.string.needed_buchi_count, needPoint));
                bundle.putBoolean(AlertDialogFragment.DIALOG_EDITABLE_NAME, true);
                alertDialogFragment.setArguments(bundle);

                alertDialogFragment.setEditableConfirmListener(new AlertDialogFragment.EditableConfirmListener() {
                    @Override
                    public void onDialogConfirmed(String editString) {
                        List<NameValuePair> paramInfo = new ArrayList<>();
                        paramInfo.add(new BasicNameValuePair("userid", user.getId()));
                        paramInfo.add(new BasicNameValuePair("comment", editString));
                        if (StringUtil.isNotEmpty(fromType)) {
                            paramInfo.add(new BasicNameValuePair("fromType", fromType));
                        }

                        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.LIKE_CARD_URL, User.class, paramInfo, HttpMethod.POST, getApplicationContext());
                        RequestFactory requestFactory = new RequestFactory();
                        requestFactory.setProgressHandler(new ProgressHandler(UserDetailActivity.this, false));
                        requestFactory.create(httpRequestVO, new HttpResponseCallback<User>() {
                            @Override
                            public void onResponse(User user) {
                                LogUtil.d("UserInfo", user);

                                CommonUtil.syncPoint(getApplicationContext());

                                UserDetailActivity.this.user = user;
                                setLikeButton();

                                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.LIKE));

                                OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
                                Bundle bundle = new Bundle();
                                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.send_like_title));
                                bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, getString(R.string.send_like_desc));
                                oneButtonAlertDialogFragment.setArguments(bundle);
                                oneButtonAlertDialogFragment.show(getSupportFragmentManager(), "oneButtonAlertDialogFragment");
                            }
                        }).execute();

                        CrushApplication.getInstance().loggingEvent(getString(R.string.ga_user_detail), getString(R.string.ga_like), getString(R.string.ga_like_confirm));
                    }
                });

                alertDialogFragment.show(getSupportFragmentManager(), "alertDialogFragment");
            }
        }
    }

    @OnClick(R.id.dislike_button)
    void clickDislikeButton() {
        replyLike("R");
        CrushApplication.getInstance().loggingEvent(getString(R.string.ga_user_detail), getString(R.string.ga_like), getString(R.string.ga_reject));
    }

    private void replyLike(final String reply) {
        List<NameValuePair> paramInfo = new ArrayList<>();
        paramInfo.add(new BasicNameValuePair("userid", user.getId()));
        paramInfo.add(new BasicNameValuePair("value", reply));

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.REPLY_LIKE_CARD_URL, User.class, paramInfo, HttpMethod.POST, getApplicationContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setProgressHandler(new ProgressHandler(UserDetailActivity.this, false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<User>() {
            @Override
            public void onResponse(final User user) {
                LogUtil.d("UserInfo", user);

                UserDetailActivity.this.user = user;
                setLikeButton();
                setLikeMessage();

                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.REPLY));

                if ("A".equals(reply)) {
                    OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.reply_like_title));
                    bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, getString(R.string.reply_like_desc));
                    oneButtonAlertDialogFragment.setArguments(bundle);
                    oneButtonAlertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                        @Override
                        public void onDialogConfirmed() {
                            Intent intent = new Intent(UserDetailActivity.this, SendBirdMessagingChannelListActivity.class);
                            startActivity(intent);
                        }
                    });
                    oneButtonAlertDialogFragment.show(getSupportFragmentManager(), "oneButtonAlertDialogFragment");
                }

            }
        }).execute();
    }

    @OnClick(R.id.close_button)
    void onCilckCloseButton() {
        finish();
    }

    @OnClick(R.id.report_button)
    void onCilckReportButton() {
        Uri uri = Uri.parse(Constants.PESOFT_EMAIL);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_content_report, SharedPrefHelper.getInstance(getApplicationContext()).getSharedPreferences(SharedPrefHelper.USER_ID, ""), user.getId()));
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
    }

    @OnClick(R.id.message_close_button)
    void onCilckMessageClose() {
        messageOpenButton.setVisibility(View.VISIBLE);
        messageLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.message_open_button)
    void onCilckMessageOpen() {
        messageOpenButton.setVisibility(View.GONE);
        messageLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }

}
