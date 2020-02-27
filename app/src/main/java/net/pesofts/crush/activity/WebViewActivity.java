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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WebViewActivity extends FragmentActivity {

    public static final String WEB_VIEW_TITLE_NAME = "title";
    public static final String WEB_VIEW_URL_NAME = "url";

    @Bind(R.id.web_view)
    WebView webView;
    @Bind(R.id.name_text)
    TextView nameText;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);


        String title = getIntent().getStringExtra(WEB_VIEW_TITLE_NAME);
        String url = getIntent().getStringExtra(WEB_VIEW_URL_NAME);

        if (Constants.NOTICE_URL.equals(url)) {
            CrushApplication.getInstance().loggingView(getString(R.string.ga_notice));
        }

        nameText.setText(title);
        webView.setVerticalScrollBarEnabled(false);
        webView.setWebViewClient(new InnerWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    @OnClick(R.id.close_button)
    void onCilckCloseButton() {
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }

    class InnerWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}