/**
 * Copyright 2014 Daum Kakao Corp.
 * <p/>
 * Redistribution and modification in source or binary forms are not permitted without specific prior written permission.혻
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.pesofts.crush.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.onesignal.OneSignal;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.OneSignalUtil;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.fragment.AlertDialogFragment;
import net.pesofts.crush.model.CustomProfile;
import net.pesofts.crush.model.Result;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.widget.CrystalRangeSeekbar;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends FragmentActivity {

    @Bind(R.id.card_setting_button)
    ImageButton cardSettingButton;
    @Bind(R.id.like_setting_button)
    ImageButton likeSettingButton;
    @Bind(R.id.chat_setting_button)
    ImageButton chatSettingButton;
    @Bind(R.id.range_seekbar)
    CrystalRangeSeekbar rangeSeekbar;
    @Bind(R.id.age_range_text)
    TextView ageRangeText;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        CrushApplication.getInstance().loggingView(getString(R.string.ga_setting));

        OneSignal.getTags(new OneSignal.GetTagsHandler() {
            @Override
            public void tagsAvailable(final JSONObject tags) {
                if (!isFinishing()) { //콜백 받았을 때 액티비티가 종료되었으면 UI를 그리지 않는다.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (tags.getBoolean(OneSignalUtil.ENABLE_CARD)) {
                                    cardSettingButton.setImageResource(R.drawable.settings_btn_on);
                                    cardSettingButton.setTag("true");
                                } else {
                                    cardSettingButton.setImageResource(R.drawable.settings_btn_off);
                                    cardSettingButton.setTag("false");
                                }

                                if (tags.getBoolean(OneSignalUtil.ENABLE_LIKE)) {
                                    likeSettingButton.setImageResource(R.drawable.settings_btn_on);
                                    likeSettingButton.setTag("true");
                                } else {
                                    likeSettingButton.setImageResource(R.drawable.settings_btn_off);
                                    likeSettingButton.setTag("false");
                                }

                                if (tags.getBoolean(OneSignalUtil.ENABLE_CHAT)) {
                                    chatSettingButton.setImageResource(R.drawable.settings_btn_on);
                                    chatSettingButton.setTag("true");
                                } else {
                                    chatSettingButton.setImageResource(R.drawable.settings_btn_off);
                                    chatSettingButton.setTag("false");
                                }
                            } catch (JSONException e) {
                            }
                        }
                    });
                }

            }
        });

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.TODAY_CARD_AGE, CustomProfile.class, null, HttpMethod.GET, SettingActivity.this);
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<CustomProfile>() {
            @Override
            public void onResponse(CustomProfile customProfile) {
                rangeSeekbar.setMinStartValue(customProfile.getMinAge());
                rangeSeekbar.setMaxStartValue(customProfile.getMaxAge());
                rangeSeekbar.setMinValue(19);
                rangeSeekbar.setMaxValue(40);
                rangeSeekbar.setGap(6);
                rangeSeekbar.apply();

                rangeSeekbar.setVisibility(View.VISIBLE);
                ageRangeText.setVisibility(View.VISIBLE);
                ageRangeText.setText(getString(R.string.age_range, customProfile.getMinAge().toString(), customProfile.getMaxAge().toString()));
            }
        }).execute();

        rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                ageRangeText.setText(getString(R.string.age_range, String.valueOf(minValue), String.valueOf(maxValue)));
            }
        });

        rangeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                LogUtil.d("CRS=>", String.valueOf(minValue) + " : " + String.valueOf(maxValue));

                List<NameValuePair> paramInfo = new ArrayList<>();
                paramInfo.add(new BasicNameValuePair("minAge", String.valueOf(minValue)));
                paramInfo.add(new BasicNameValuePair("maxAge", String.valueOf(maxValue)));
                HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.TODAY_CARD_AGE, Result.class, paramInfo, HttpMethod.POST, SettingActivity.this);
                new RequestFactory().create(httpRequestVO, new HttpResponseCallback<Result>() {
                    @Override
                    public void onResponse(Result result) {
                        LogUtil.d("CRS=>" + result.getCode());
                    }
                }).execute();
            }
        });
    }

    @OnClick(R.id.close_button)
    void onCilckCloseButton() {
        finish();
    }

    private void setSettingButton(ImageButton view, String key) {
        if ("true".equals(view.getTag())) {
            OneSignal.sendTag(key, "false");
            view.setTag("false");
            view.setImageResource(R.drawable.settings_btn_off);
        } else {
            OneSignal.sendTag(key, "true");
            view.setTag("true");
            view.setImageResource(R.drawable.settings_btn_on);
        }
    }

    @OnClick(R.id.card_setting_button)
    void cardSettingButton(ImageButton view) {
        setSettingButton(view, OneSignalUtil.ENABLE_CARD);
    }

    @OnClick(R.id.like_setting_button)
    void likeSettingButton(ImageButton view) {
        setSettingButton(view, OneSignalUtil.ENABLE_LIKE);
    }

    @OnClick(R.id.chat_setting_button)
    void chatSettingButton(ImageButton view) {
        setSettingButton(view, OneSignalUtil.ENABLE_CHAT);
    }

    @OnClick(R.id.logout_layout)
    void logoutButton() {
        resetUser();
    }

    private void resetUser() {
        OneSignalUtil.deleteTags();
        SharedPrefHelper.getInstance(this).removeAllSharedPreferences();

        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.signout_layout)
    void signoutButton() {
        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.ask_account_pause));
        bundle.putBoolean(AlertDialogFragment.DIALOG_SIGNOUT_EDITABLE_NAME, true);
        bundle.putString(AlertDialogFragment.DIALOG_SIGNOUT_EDITABLE_HINT, getString(R.string.signout_hint));
        alertDialogFragment.setArguments(bundle);

        alertDialogFragment.setEditableConfirmListener(new AlertDialogFragment.EditableConfirmListener() {
            @Override
            public void onDialogConfirmed(String editString) {
                List<NameValuePair> paramInfo = new ArrayList<>();
                paramInfo.add(new BasicNameValuePair("reason", editString));

                HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.SIGN_OUT_URL, Result.class, paramInfo, HttpMethod.POST, SettingActivity.this);
                new RequestFactory().create(httpRequestVO, new HttpResponseCallback<Result>() {
                    @Override
                    public void onResponse(Result result) {
                        resetUser();
                    }
                }).execute();
            }
        });

        alertDialogFragment.show(getSupportFragmentManager(), "oneButtonAlertDialogFragment");

    }

    @OnClick(R.id.quit_layout)
    void quitButton() {
        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.ask_account_quit));
        bundle.putBoolean(AlertDialogFragment.DIALOG_SIGNOUT_EDITABLE_NAME, true);
        bundle.putString(AlertDialogFragment.DIALOG_SIGNOUT_EDITABLE_HINT, getString(R.string.quit_hint));
        alertDialogFragment.setArguments(bundle);

        alertDialogFragment.setEditableConfirmListener(new AlertDialogFragment.EditableConfirmListener() {
            @Override
            public void onDialogConfirmed(String editString) {
                List<NameValuePair> paramInfo = new ArrayList<>();
                paramInfo.add(new BasicNameValuePair("reason", editString));
                paramInfo.add(new BasicNameValuePair("status", "D"));

                HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.SIGN_OUT_URL, Result.class, paramInfo, HttpMethod.POST, SettingActivity.this);
                new RequestFactory().create(httpRequestVO, new HttpResponseCallback<Result>() {
                    @Override
                    public void onResponse(Result result) {
                        resetUser();
                    }
                }).execute();
            }
        });

        alertDialogFragment.show(getSupportFragmentManager(), "oneButtonAlertDialogFragment");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }

}
