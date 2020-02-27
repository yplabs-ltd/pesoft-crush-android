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
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.OneSignalUtil;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.Util.SnackbarUtil;
import net.pesofts.crush.Util.StringUtil;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.RequestFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignInActivity extends FragmentActivity {

    @Bind(R.id.email_edit_text)
    EditText emailEditText;
    @Bind(R.id.password_edit_text)
    EditText passwordEditText;
    @Bind(R.id.sign_in_button)
    TextView signInButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        CrushApplication.getInstance().loggingView(getString(R.string.ga_sign_in));

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (StringUtil.isNotEmpty(s)) {
                    setEnableView(emailEditText);
                } else {
                    setDisableView(emailEditText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (StringUtil.isNotEmpty(s)) {
                    setEnableView(passwordEditText);
                } else {
                    setDisableView(passwordEditText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void setEnableView(TextView view) {
        view.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color));
        view.setBackgroundResource(R.drawable.main_text_underline);
    }

    private void setDisableView(TextView view) {
        view.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dimmed_text_color));
        view.setBackgroundResource(R.drawable.grey_underline);
    }

    private void moveToMainActivity(User user) {
        final Intent intent = new Intent(this, MainActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("user", user);
        intent.putExtras(extras);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.sign_in_button)
    void onClickSignIn() {

        String errorString = "";
        String emailText = emailEditText.getText().toString().trim();
        String passwordText = passwordEditText.getText().toString().trim();

        if (emailEditText.getText().toString().indexOf(" ") >= 0 || passwordEditText.getText().toString().indexOf(" ") >= 0) {
            errorString = getString(R.string.invalid_space);
        } else if (StringUtil.isEmpty(emailText)) {
            errorString = getString(R.string.empty_email);
        } else if (emailText.indexOf("@") < 0) {
            errorString = getString(R.string.invalid_email);
        } else if (StringUtil.isEmpty(passwordText)) {
            errorString = getString(R.string.empty_password);
        } else if (passwordText.length() < 4) {
            errorString = getString(R.string.short_password);
        }

        if (StringUtil.isNotEmpty(errorString)) {
            SnackbarUtil.getSnackbar(signInButton, errorString).show();
            return;
        }

        List<NameValuePair> paramInfo = new ArrayList<>();
        paramInfo.add(new BasicNameValuePair("email", emailText));
        paramInfo.add(new BasicNameValuePair("passwd", passwordText));

        LogUtil.d("email : " + emailEditText.getText().toString().trim());
        LogUtil.d("passwd : " + passwordEditText.getText().toString().trim());

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.SIGN_IN_URL, User.class, paramInfo, HttpMethod.POST, getApplicationContext());
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<User>() {
                    @Override
                    public void onResponse(User user) {
                        if (user != null) {
                            SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.USER_STATUS, user.getStatus());
                            SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.USER_ID, user.getId());
                            SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.POINT, user.getPoint());
                            SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.GENDER, user.getGender());
                        }
                        OneSignalUtil.resetTags(user.getId());
                        moveToMainActivity(user);
                    }

                    @Override
                    public void onParse(int code, Map<String, String> headers, byte[] data, long networkTime) {
                        HttpUtil.checkSessionCookie(headers, getApplicationContext());
                    }
                }
        ).execute();
    }

    @OnClick(R.id.lose_password_text)
    void onClickLosePassword() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }

}
