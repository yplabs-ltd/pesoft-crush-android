package net.pesofts.crush.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.DateUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.ImageUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.Util.SnackbarUtil;
import net.pesofts.crush.Util.StringUtil;
import net.pesofts.crush.fragment.AlertDialogFragment;
import net.pesofts.crush.fragment.BaseDialogFragment;
import net.pesofts.crush.fragment.ImageCropDialogFragment;
import net.pesofts.crush.fragment.MultiProfileCodeDialogFragment;
import net.pesofts.crush.fragment.OneButtonAlertDialogFragment;
import net.pesofts.crush.fragment.ProfileCodeDialogFragment;
import net.pesofts.crush.model.Image;
import net.pesofts.crush.model.User;
import net.pesofts.crush.model.UserInfoCode;
import net.pesofts.crush.model.UserInfoCodeGroup;
import net.pesofts.crush.model.UserInfoCodeMap;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.network.RequestManager;
import net.pesofts.crush.network.VolleyMultipartRequest;
import net.pesofts.crush.permission.PermissionListener;
import net.pesofts.crush.permission.PermissionManager;
import net.pesofts.crush.permission.PermissionUtils;
import net.pesofts.crush.widget.CircularNetworkImageView;

import org.apmem.tools.layouts.FlowLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class ProfileActivity extends FragmentActivity {

    private final static int REQUEST_CODE_PICK_FROM_GALLERY = 1;
    private final static int REQUEST_CODE_IMAGE_CROP = 2;
    public static final int ATTACHED_IMAGE_WIDTH = 1024;
    private User user;
    private UserInfoCodeMap userInfoCodeMap;
    private CircularNetworkImageView selectedProfileImageView;
    private List<UserInfoCode> birthYearList = new ArrayList<>();
    private List<UserInfoCode> heightList = new ArrayList<>();
    private List<UserInfoCode> photoMenuList = new ArrayList<>();
    private Map<View, String> titleMap = new HashMap<>();

    @Bind({R.id.profile_image_view1, R.id.profile_image_view2, R.id.profile_image_view3, R.id.profile_image_view4, R.id.profile_image_view5, R.id.profile_image_view6})
    List<CircularNetworkImageView> profileImageViewList;
    @Bind({R.id.ideal_text, R.id.name_text, R.id.age_text, R.id.blood_type_text, R.id.job_text, R.id.body_text, R.id.religion_text, R.id.region_text, R.id.height_text})
    List<TextView> needFillFieldList;
    @Bind(R.id.ideal_text)
    TextView idealText;
    @Bind(R.id.name_text)
    EditText nameText;
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
    @Bind(R.id.region_text)
    TextView regionText;
    @Bind(R.id.height_text)
    TextView heightText;
    @Bind(R.id.school_text)
    EditText schoolText;
    @Bind(R.id.status_text)
    TextView statusText;
    @Bind(R.id.confirm_button)
    TextView confirmButton;
    @Bind(R.id.add_interest_button)
    ImageButton addInterestButton;
    @Bind(R.id.add_attractive_button)
    ImageButton addAttractiveButton;
    @Bind(R.id.add_favor_button)
    ImageButton addFavorButton;
    @Bind(R.id.interest_layout)
    FlowLayout interestLayout;
    @Bind(R.id.attractive_layout)
    FlowLayout attractiveLayout;
    @Bind(R.id.favor_layout)
    FlowLayout favorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        CrushApplication.getInstance().loggingView(getString(R.string.ga_profile));

        for (int i = 0; i < profileImageViewList.size(); i++) {
            if (i < 2) {
                profileImageViewList.get(i).setDefaultImageResId(R.drawable.myprofile_btn_required_normal);
            } else {
                profileImageViewList.get(i).setDefaultImageResId(R.drawable.myprofile_btn_option_normal);
            }
            profileImageViewList.get(i).setErrorImageResId(R.drawable.myprofile_btn_fail_normal);
        }

        titleMap.put(idealText, getString(R.string.ideal));
        titleMap.put(ageText, getString(R.string.age));
        titleMap.put(regionText, getString(R.string.region));
        titleMap.put(jobText, getString(R.string.job));
        titleMap.put(bodyText, getString(R.string.body));
        titleMap.put(heightText, getString(R.string.height));
        titleMap.put(bloodTypeText, getString(R.string.blood_type));
        titleMap.put(religionText, getString(R.string.religion));
        titleMap.put(addInterestButton, getString(R.string.interest_about));
        titleMap.put(addAttractiveButton, getString(R.string.attractive_about));
        titleMap.put(addFavorButton, getString(R.string.favor_about));

        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            idealText.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.padding_4dp));
        }

        UserInfoCode userInfoCode = new UserInfoCode();
        userInfoCode.setValue(getString(R.string.photo_album));
        UserInfoCode userInfoCode2 = new UserInfoCode();
        userInfoCode2.setValue(getString(R.string.change_main_photo));
        UserInfoCode userInfoCode3 = new UserInfoCode();
        userInfoCode3.setValue(getString(R.string.delete));

        photoMenuList.add(userInfoCode);
        photoMenuList.add(userInfoCode2);
        photoMenuList.add(userInfoCode3);

        HttpRequestVO codeRequestVO = HttpUtil.getHttpRequestVO(Constants.PROFILE_CODE_URL, UserInfoCodeMap.class, null, getApplicationContext());
        new RequestFactory().create(codeRequestVO, new HttpResponseCallback<UserInfoCodeMap>() {
            @Override
            public void onResponse(UserInfoCodeMap userInfoCodeMap) {
                LogUtil.d("onResponse");

                ProfileActivity.this.userInfoCodeMap = userInfoCodeMap;

            }
        }).execute();

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.EDIT_PROFILE_URL, User.class, null, getApplicationContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setProgressHandler(new ProgressHandler(this, false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<User>() {
            @Override
            public void onResponse(User user) {
                ProfileActivity.this.user = user;
                setUserInfo();
            }
        }).execute();
    }

    private void setProfileImage() {
        for (int i = 0; i < profileImageViewList.size(); i++) {
            if (user.getImageInfoList() == null || i + 1 > user.getImageInfoList().size()) {
                profileImageViewList.get(i).setImageUrl("", RequestManager.getImageLoader());
            } else {
                Image image = user.getImageInfoList().get(i);
                image.setOrdering(i + 1);
                if (StringUtil.isNotEmpty(image.getImageUrl())) {
                    profileImageViewList.get(i).setImageUrl(image.getImageUrl(), RequestManager.getImageLoader());
                } else if (StringUtil.isNotEmpty(image.getUrlpath())) {
                    profileImageViewList.get(i).setImageUrl(image.getUrlpath(), RequestManager.getImageLoader());
                }
            }
        }
    }

    private void setUserInfo() {

        setProfileImage();

        nameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                confirmButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (StringUtil.isNotEmpty(s)) {
                    setEnableView(nameText);
                } else {
                    setDisableView(nameText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        schoolText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                confirmButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (StringUtil.isNotEmpty(s)) {
                    setEnableView(schoolText);
                } else {
                    setDisableView(schoolText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setUserOneInfoByCode(idealText, user.getIdealType(), R.string.fill_info);
        setUserOneInfoByString(nameText, user.getName(), R.string.fill_info);
        setUserOneInfoByString(ageText, DateUtil.getAgeString(user.getBirthDate()), R.string.fill_info);
        setUserOneInfoByCode(regionText, user.getHometown(), R.string.fill_info);
        setUserOneInfoByCode(jobText, user.getJob(), R.string.fill_info);
        setUserOneInfoByCode(bodyText, user.getBodyType(), R.string.fill_info);

        if (user.getHeight() > 0) {
            setUserOneInfoByString(heightText, Integer.toString(user.getHeight()) + "cm", R.string.fill_info);
        } else {
            setUserOneInfoByString(heightText, null, R.string.fill_info);
        }

        if (user.getBloodType() != null) {
            setUserOneInfoByString(bloodTypeText, user.getBloodType().getValue(), R.string.fill_info);
        } else {
            setUserOneInfoByString(bloodTypeText, null, R.string.fill_info);
        }

        setUserOneInfoByCode(religionText, user.getReligion(), R.string.fill_info);
        setUserOneInfoByString(schoolText, user.getSchool(), R.string.optional_info);

        if(user.getHobbyList() != null) {
            setKeywordTextViewList(interestLayout, user.getHobbyList());
        } else {
            user.setHobbyList(new ArrayList<UserInfoCode>());
        }

        if(user.getCharmTypeList() != null) {
            setKeywordTextViewList(attractiveLayout, user.getCharmTypeList());
        } else {
            user.setCharmTypeList(new ArrayList<UserInfoCode>());
        }

        if(user.getFavoriteTypeList() != null) {
            setKeywordTextViewList(favorLayout, user.getFavoriteTypeList());
        } else {
            user.setFavoriteTypeList(new ArrayList<UserInfoCode>());
        }

        setStatusTextAndConfirmButton();
    }

    private void setKeywordTextViewList(ViewGroup rootView, List<UserInfoCode> codeList) {
        if(codeList == null) {
            return;
        }

        for (UserInfoCode code : codeList) {
            TextView textView = (TextView) LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.incloud_profile_text_box, rootView, false);
            textView.setText(code.getValue());

            rootView.addView(textView, 0);
        }

    }

    private void setEnableView(TextView view) {
        view.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color));
        view.setBackgroundResource(R.drawable.main_text_underline);
    }

    private void setDisableView(TextView view) {
        view.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dimmed_text_color));
        view.setBackgroundResource(R.drawable.grey_underline);
    }

    private void setStatusTextAndConfirmButton() {
        if ("P".equals(user.getStatus())) {
            statusText.setText(R.string.fill_profile_desc);
            statusText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_red_color));
        } else if ("R".equals(user.getReviewStatus()) && StringUtil.isNotEmpty(user.getReviewMessage())) {
            statusText.setText(user.getReviewMessage());
            statusText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_red_color));
        } else if ("W".equals(user.getReviewStatus())) {
            statusText.setText(R.string.review_profile_desc);
            statusText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_red_color));
        } else {
            statusText.setText(R.string.change_photo_desc);
            statusText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color));
        }

        if ("P".equals(user.getStatus())) {
            confirmButton.setText(R.string.request_regist_profile);
            confirmButton.setVisibility(View.VISIBLE);
        } else {
            confirmButton.setText(R.string.request_revise_profile);
            confirmButton.setVisibility(View.GONE);
        }
    }

    private void setUserOneInfoByString(View view, String value, int emptyStringResId) {
        if (StringUtil.isNotEmpty(value)) {
            ((TextView) view).setText(value);
            view.setBackgroundResource(R.drawable.main_text_underline);
            ((TextView) view).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color));
        } else {
            view.setBackgroundResource(R.drawable.grey_underline);
            if (view instanceof EditText) {
                ((EditText) view).setHint(emptyStringResId);
                ((EditText) view).setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dimmed_text_color));
            } else {
                ((TextView) view).setText(emptyStringResId);
                ((TextView) view).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dimmed_text_color));
            }
        }
    }

    private void setUserOneInfoByCode(TextView textView, UserInfoCode userInfoCode, int emptyStringResId) {
        if (userInfoCode != null && StringUtil.isNotEmpty(userInfoCode.getValue())) {
            textView.setText(userInfoCode.getValue());
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color));
            textView.setBackgroundResource(R.drawable.main_text_underline);
        } else {
            textView.setText(emptyStringResId);
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dimmed_text_color));
            textView.setBackgroundResource(R.drawable.grey_underline);
        }
    }

    @OnClick({R.id.ideal_text, R.id.age_text, R.id.region_text, R.id.job_text, R.id.body_text, R.id.height_text, R.id.blood_type_text, R.id.religion_text})
    void clickProfileInfo(final View view) {

        List<UserInfoCode> userInfoCodes = null;
        List<UserInfoCodeGroup> userInfoCodeGroups = null;
        Integer selectPosition = null;

        if (view == idealText) {
            if (user.isMale()) {
                userInfoCodes = userInfoCodeMap.getIdealMTypeCode();
            } else {
                userInfoCodes = userInfoCodeMap.getIdealFTypeCode();
            }
        } else if (view == ageText) {
            userInfoCodes = getBirthYearList();
            selectPosition = 20;
        } else if (view == regionText) {
            userInfoCodes = userInfoCodeMap.getHometownCode();
        } else if (view == jobText) {
            userInfoCodeGroups = getUserInfoCodeGroups(userInfoCodeMap.getJobCode());
        } else if (view == bodyText) {
            if (user.isMale()) {
                userInfoCodes = userInfoCodeMap.getBodyMTypeCode();
            } else {
                userInfoCodes = userInfoCodeMap.getBodyFTypeCode();
            }
        } else if (view == heightText) {
            userInfoCodes = getHeightList();
            selectPosition = 30;
        } else if (view == bloodTypeText) {
            userInfoCodes = userInfoCodeMap.getBloodTypeCode();
        } else if (view == religionText) {
            userInfoCodes = userInfoCodeMap.getReligionCode();
        }

        if (userInfoCodes == null && userInfoCodeGroups == null) {
            return;
        }

        ProfileCodeDialogFragment profileDialogFragment = new ProfileCodeDialogFragment();
        profileDialogFragment.setTitle(titleMap.get(view));
        if (userInfoCodes != null) {
            profileDialogFragment.setCodeList(userInfoCodes);
        } else if (userInfoCodeGroups != null) {
            profileDialogFragment.setCodeGroup(userInfoCodeGroups);
        }
        if (selectPosition != null) {
            profileDialogFragment.setSelectPosition(selectPosition);
        }
        if (view == jobText) {
            if (user.getJob() != null) {
                profileDialogFragment.setSelectedValue(user.getJob().getExtra());
            }
        } else if (view == heightText) {
            profileDialogFragment.setSelectedValue(Integer.toString(user.getHeight()));
        } else if (view == ageText) {
            if (StringUtil.isNotEmpty(user.getBirthDate())) {
                profileDialogFragment.setSelectedValue(user.getBirthDate().substring(0, 4));
            }
        } else {
            CharSequence text = ((TextView) view).getText();
            if (text != null) {
                profileDialogFragment.setSelectedValue(text.toString());
            }
        }

        profileDialogFragment.setSelectCodeListener(
                new ProfileCodeDialogFragment.SelectCodeListener()

                {
                    @Override
                    public void onSelectCode(UserInfoCode userInfoCode) {
                        confirmButton.setVisibility(View.VISIBLE);

                        String value = "";
                        if (view == ageText) {
                            value = DateUtil.getAgeString(userInfoCode.getValue());
                        } else if (view == heightText) {
                            value = userInfoCode.getValue() + "cm";
                        } else if (view == jobText) {
                            value = userInfoCode.getExtra() + ", " + userInfoCode.getValue();
                        } else {
                            value = userInfoCode.getValue();
                        }

                        setUserOneInfoByString(view, value, R.string.fill_info);

                        if (view == idealText) {
                            user.setIdealType(userInfoCode);
                        } else if (view == ageText) {
                            user.setBirthDate(userInfoCode.getValue() + "-01-01");
                        } else if (view == regionText) {
                            user.setHometown(userInfoCode);
                        } else if (view == jobText) {
                            user.setJob(userInfoCode);
                        } else if (view == bodyText) {
                            user.setBodyType(userInfoCode);
                        } else if (view == heightText) {
                            user.setHeight(Integer.parseInt(userInfoCode.getValue()));
                        } else if (view == bloodTypeText) {
                            user.setBloodType(userInfoCode);
                        } else if (view == religionText) {
                            user.setReligion(userInfoCode);
                        }
                    }
                }

        );
        profileDialogFragment.show(getSupportFragmentManager(), "profileDialogFragment");
    }

    @OnClick({R.id.add_interest_button, R.id.add_attractive_button, R.id.add_favor_button})
    void clickAddProfileInfo(final View view) {

        List<UserInfoCode> userInfoCodes = null;
        List<UserInfoCode> selectedCodes = null;

        if (view == addInterestButton) {
            userInfoCodes = userInfoCodeMap.getHobbyCode();
            selectedCodes = user.getHobbyList();
        } else if (view == addAttractiveButton) {
            if (user.isMale()) {
                userInfoCodes = userInfoCodeMap.getCharmMTypeCode();
            } else {
                userInfoCodes = userInfoCodeMap.getCharmFTypeCode();
            }
            selectedCodes = user.getCharmTypeList();
        } else if (view == addFavorButton) {
            userInfoCodes = userInfoCodeMap.getFavoriteTypeCode();
            selectedCodes = user.getFavoriteTypeList();
        }

        if (userInfoCodes == null) {
            return;
        }

        MultiProfileCodeDialogFragment profileDialogFragment = new MultiProfileCodeDialogFragment();
        profileDialogFragment.setTitle(titleMap.get(view));
        if (userInfoCodes != null) {
            profileDialogFragment.setCodeList(userInfoCodes);
        }
        if (selectedCodes != null) {
            profileDialogFragment.setSelectedCodeList(selectedCodes);
        }

        profileDialogFragment.setSelectCodeListener(
                new MultiProfileCodeDialogFragment.SelectCodeListener()

                {
                    @Override
                    public void onSelectCode(List<UserInfoCode> userInfoCodeList) {
                        confirmButton.setVisibility(View.VISIBLE);

                        ViewGroup rootView = null;
                        if (view == addInterestButton) {
                            rootView = interestLayout;

                            user.setHobbyList(new ArrayList<UserInfoCode>());
                            user.getHobbyList().addAll(userInfoCodeList);
                        } else if (view == addAttractiveButton) {
                            rootView = attractiveLayout;

                            user.setCharmTypeList(new ArrayList<UserInfoCode>());
                            user.getCharmTypeList().addAll(userInfoCodeList);
                        } else if (view == addFavorButton) {
                            rootView = favorLayout;

                            user.setFavoriteTypeList(new ArrayList<UserInfoCode>());
                            user.getFavoriteTypeList().addAll(userInfoCodeList);
                        }

                        removeChildTextView(rootView);

                        setKeywordTextViewList(rootView, userInfoCodeList);
                    }
                }

        );
        profileDialogFragment.show(getSupportFragmentManager(), "profileDialogFragment");
    }

    private void removeChildTextView(ViewGroup rootView) {
        for (int i = rootView.getChildCount() - 1; i >= 0; i--) {
            if (rootView.getChildAt(i) instanceof TextView) {
                rootView.removeViewAt(i);
            }
        }
    }

    private List<UserInfoCodeGroup> getUserInfoCodeGroups(List<UserInfoCode> userInfoCodes) {
        List<UserInfoCodeGroup> userInfoCodeGroups = new ArrayList<>();
        List<String> extraTitleList = new ArrayList<>();
        for (UserInfoCode userInfoCode : userInfoCodes) {
            if (userInfoCode.getExtra() != null) {
                if (extraTitleList.indexOf(userInfoCode.getExtra()) < 0) {
                    extraTitleList.add(userInfoCode.getExtra());

                    UserInfoCodeGroup userInfoCodeGroup = new UserInfoCodeGroup();
                    userInfoCodeGroup.setTitle(userInfoCode.getExtra());
                    userInfoCodeGroup.setUserInfoCodeList(new ArrayList<UserInfoCode>());
                    userInfoCodeGroup.getUserInfoCodeList().add(userInfoCode);

                    userInfoCodeGroups.add(userInfoCodeGroup);
                } else {
                    UserInfoCodeGroup userInfoCodeGroup = userInfoCodeGroups.get(extraTitleList.indexOf(userInfoCode.getExtra()));
                    userInfoCodeGroup.getUserInfoCodeList().add(userInfoCode);
                }
            }
        }

        return userInfoCodeGroups;
    }

    private List<UserInfoCode> getBirthYearList() {
        if (birthYearList.size() == 0) {
            for (int i = 1975; i < 2001; i++) {
                UserInfoCode userInfoCode = new UserInfoCode();
                userInfoCode.setValue(Integer.toString(i));
                birthYearList.add(userInfoCode);
            }
        }
        return birthYearList;
    }

    private List<UserInfoCode> getHeightList() {
        if (heightList.size() == 0) {
            for (int i = 140; i < 210; i++) {
                UserInfoCode userInfoCode = new UserInfoCode();
                userInfoCode.setValue(Integer.toString(i));
                heightList.add(userInfoCode);
            }
        }
        return heightList;
    }

    @OnClick({R.id.profile_image_view1, R.id.profile_image_view2, R.id.profile_image_view3, R.id.profile_image_view4, R.id.profile_image_view5, R.id.profile_image_view6})
    void clickProfileImage(final View view) {
        final int order = profileImageViewList.indexOf(view);

        if (user.getImageInfoList() != null && user.getImageInfoList().size() > order) {
            ProfileCodeDialogFragment profileDialogFragment = new ProfileCodeDialogFragment();
            profileDialogFragment.setTitle(getString(R.string.photo));
            profileDialogFragment.setCodeList(photoMenuList);
            profileDialogFragment.setSelectCodeListener(new ProfileCodeDialogFragment.SelectCodeListener() {
                @Override
                public void onSelectCode(UserInfoCode userInfoCode) {
                    if (userInfoCode == null) {
                        return;
                    }
                    if (getString(R.string.photo_album).equals(userInfoCode.getValue())) {
                        callGalley(order);
                    } else if (getString(R.string.change_main_photo).equals(userInfoCode.getValue())) {
                        Image mainImage = user.getImageInfoList().get(0);
                        mainImage.setOrdering(order + 1);
                        Image selectImage = user.getImageInfoList().get(order);
                        selectImage.setOrdering(1);

                        user.getImageInfoList().remove(mainImage);
                        user.getImageInfoList().remove(selectImage);

                        user.getImageInfoList().add(0, selectImage);
                        user.getImageInfoList().add(order, mainImage);

                        setProfileImage();

                        confirmButton.setVisibility(View.VISIBLE);
                    } else if (getString(R.string.delete).equals(userInfoCode.getValue())) {
                        Image selectImage = user.getImageInfoList().get(order);
                        user.getImageInfoList().remove(selectImage);
                        setProfileImage();

                        confirmButton.setVisibility(View.VISIBLE);
                    }
                }
            });

            profileDialogFragment.show(getSupportFragmentManager(), "profileDialogFragment");
        } else {
            callGalley(order);
        }

    }

    private void callGalley(int profileViewOrder) {
        if (user.getImageInfoList() == null) {
            profileViewOrder = 0;
        } else if (user.getImageInfoList().size() < profileViewOrder) {
            profileViewOrder = user.getImageInfoList().size();
        }

        selectedProfileImageView = profileImageViewList.get(profileViewOrder);
        callGallery();
    }


    @OnClick(R.id.confirm_button)
    void clickConfirm(View view) {
        if (checkAllFillField()) {
            updateProfile(getUserForUpdate());
        }
    }

    private boolean checkAllFillField() {
        if (user.getImageInfoList() == null || user.getImageInfoList().size() < 2) {
            SnackbarUtil.getSnackbar(confirmButton, getString(R.string.fill_2images_desc)).show();
            return false;
        }

        for (int i = 0; i < 2; i++) {
            Image image = user.getImageInfoList().get(i);
            if (StringUtil.isEmpty(image.getUrlpath())) {
                SnackbarUtil.getSnackbar(confirmButton, getString(R.string.fill_2images_desc)).show();
                return false;
            }
        }

        for (TextView view : needFillFieldList) {
            if (StringUtil.isEmpty(view.getText()) || getString(R.string.fill_info).equals(view.getText())) {
                SnackbarUtil.getSnackbar(view, getString(R.string.fill_profile_desc)).show();
                return false;
            }
        }

        return true;
    }

    public User getUserForUpdate() {
        User userForUpdate = user;
        userForUpdate.setName(nameText.getText().toString());
        userForUpdate.setSchool(schoolText.getText().toString());

        return userForUpdate;
    }

    private void updateProfile(User user) {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.UPDATE_PROFILE_URL, User.class, null, HttpMethod.POST, getApplicationContext());
        httpRequestVO.setPayloadEntity(user);
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setProgressHandler(new ProgressHandler(this, false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<User>() {
            @Override
            public void onResponse(User user) {
                LogUtil.d("UserInfo", user);

                if (user != null) {
                    String status = SharedPrefHelper.getInstance(getApplicationContext()).getSharedPreferences(SharedPrefHelper.USER_STATUS, "");

                    if (!status.equals(user.getStatus())) {
                        SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.USER_STATUS, user.getStatus());
                        EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.STATUS_CHANGE));
                    }
                }

                ProfileActivity.this.user = user;
                setStatusTextAndConfirmButton();

                OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.response_revise_profile));
                oneButtonAlertDialogFragment.setArguments(bundle);
                oneButtonAlertDialogFragment.show(getSupportFragmentManager(), "oneButtonAlertDialogFragment");

            }
        }).execute();
    }

    private void callGallery() {
        PermissionManager.getInstance().requestPermission(
                this,
                PermissionUtils.STORAGE,
                PermissionUtils.STORAGE_NAME,
                new PermissionListener() {
                    @Override
                    public void onGranted() {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        if (intent.resolveActivity(getPackageManager()) == null) {
                            Toast.makeText(getBaseContext(), "갤러리 없음", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        startActivityForResult(intent, REQUEST_CODE_PICK_FROM_GALLERY);
                    }

                    @Override
                    public void onDenied() {

                    }
                }
        );
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_PICK_FROM_GALLERY:
                    Uri imageUri = data.getData();
                    showImageCropDialog(ImageUtil.getFilePathFromUri(imageUri, getApplicationContext()));
                    break;
                case REQUEST_CODE_IMAGE_CROP:
                    Bundle extras = data.getExtras();
                    if (extras != null) {
//                        Bitmap bitmap = (Bitmap) extras.get("data");
                        getUploadImageInfoAndUploadImage();
                    }

                    break;
                default:
                    break;
            }

        }
    }

//    private void cropImage(Uri imageUri) {
//        Intent cropIntent = new Intent("com.android.camera.action.CROP");
//        cropIntent.setDataAndType(imageUri, "image/*");
//        cropIntent.putExtra("crop", "true");
//        cropIntent.putExtra("aspectX", 1);
//        cropIntent.putExtra("aspectY", 1);
//        cropIntent.putExtra("outputX", ATTACHED_IMAGE_WIDTH);
//        cropIntent.putExtra("outputY", ATTACHED_IMAGE_WIDTH);
////        cropIntent.putExtra("scale", true);
//        cropIntent.putExtra("return-data", false);
//        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
//        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//
//        startActivityForResult(cropIntent, REQUEST_CODE_IMAGE_CROP);
//    }

    protected void showImageCropDialog(final String filePath) {
//        PermissionManager.getInstance().requestPermission(
//                this,
//                PermissionUtils.STORAGE,
//                PermissionUtils.STORAGE_NAME,
//                new PermissionListener() {
//                    @Override
//                    public void onGranted() {
                        ImageCropDialogFragment imageCropDialogFragment = ImageCropDialogFragment.newInstance();
                        imageCropDialogFragment.setFilePath(filePath);
                        imageCropDialogFragment.setConfirmListener(new ImageCropDialogFragment.ConfirmCropListener() {

                            @Override
                            public void onDialogConfirmed(String filePath) {
                                getUploadImageInfoAndUploadImage();
                            }
                        });
                        imageCropDialogFragment.show(getSupportFragmentManager(), "ImageCropDialogFragment");
//                    }
//
//                    @Override
//                    public void onDenied() {
//
//                    }
//                }
//        );

    }

    private Uri getTempUri() {
        Uri uri = null;
        try {
            uri = Uri.fromFile(ImageUtil.createTempImageFileForProfile());
        } catch (Exception e) {
            LogUtil.w("getTempUri fail : " + e.getMessage());
        }
        return uri;
    }

    private void getUploadImageInfoAndUploadImage() {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.IMAGE_UPLOAD_INFO_URL, Map.class, null, getApplicationContext());
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<Map>() {
            @Override
            public void onResponse(Map result) {
                LogUtil.d("result", result);

                uploadImage(result);
            }
        }).execute();
    }

    public void uploadImage(Map updateInfo) {
        final String uploadImagePath = (String) updateInfo.get("uploadImagePath");

        String filePath = ImageUtil.getFilePathFromUri(getTempUri(), getApplicationContext());
        if (StringUtil.isEmpty(filePath)) {
            return;
        }

        final ProgressHandler progressHandler = new ProgressHandler(this, false);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Constants.IMAGE_SERVER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        LogUtil.d("onResponse : " + response);

                        progressHandler.onCancel();

                        confirmButton.setVisibility(View.VISIBLE);

                        selectedProfileImageView.setImageUrl(uploadImagePath, RequestManager.getImageLoader());
                        if (user.getImageInfoList() == null) {
                            user.setImageInfoList(new ArrayList<Image>());
                        }

                        int selectedProfileOrder = profileImageViewList.indexOf(selectedProfileImageView);

                        Image image = new Image();
                        image.setUrlpath(uploadImagePath);
                        image.setOrdering(selectedProfileOrder + 1);

                        if (user.getImageInfoList().size() <= selectedProfileOrder) {
                            user.getImageInfoList().add(image);
                        } else {
                            user.getImageInfoList().remove(selectedProfileOrder);
                            user.getImageInfoList().add(selectedProfileOrder, image);
                        }

                        ImageUtil.deleteImageByUri(getTempUri(), getApplicationContext());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtil.d("onErrorResponse : " + error.getMessage());

                progressHandler.onCancel();

                ImageUtil.deleteImageByUri(getTempUri(), getApplicationContext());
                SnackbarUtil.getSnackbar(idealText, getString(R.string.error_upload_image)).show();
            }
        });

        updateInfo.remove("Host");
        updateInfo.remove("uploadImagePath");
        multipartRequest.addStringParams(updateInfo);
        multipartRequest.addAttachment(VolleyMultipartRequest.MEDIA_TYPE_JPEG, "file", new File(filePath));
        multipartRequest.buildRequest();
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                Constants.HTTP_CONNECTION_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

//        multipartRequest.setFixedStreamingMode(true);


        progressHandler.onStart();
        RequestManager.addRequest(multipartRequest, "ProfileMultipart");
    }

    @OnClick(R.id.close_button)
    void onCilckCloseButton() {
        if (confirmButton.getVisibility() == View.VISIBLE) {
            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.confirm_without_save));
            alertDialogFragment.setArguments(bundle);

            alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                @Override
                public void onDialogConfirmed() {
                    finish();
                }
            });
            alertDialogFragment.show(getSupportFragmentManager(), "alertDialogFragment");
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        onCilckCloseButton();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        ImageUtil.deleteImageByUri(getTempUri(), getApplicationContext());
    }
}
