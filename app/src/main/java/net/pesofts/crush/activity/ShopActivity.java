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

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShopActivity extends FragmentActivity {

    IInAppBillingService mService;

    @Bind(R.id.own_buchi_text)
    TextView ownBuchiText;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shop);
        ButterKnife.bind(this);

        CrushApplication.getInstance().loggingView(getString(R.string.ga_shop));

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.MY_INFO_URL, User.class, null, getApplicationContext());
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<User>() {
            @Override
            public void onResponse(User user) {
                setPointText(user);
            }
        }).execute();
    }

    private void setPointText(User user) {
        if (user != null) {
            ownBuchiText.setText(getString(R.string.own_buchi, user.getPoint()));
        }
    }

    public void buyItem(String itemId) {
        try {
            AlreadyPurchaseItems();

            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), itemId, "inapp", "develop_payload");
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

            if (pendingIntent != null) {
                startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                        Integer.valueOf(0));
            } else {
                // 결제가 막혔다면
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
//            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String originalJson = data.getStringExtra("INAPP_PURCHASE_DATA");
            String signature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                if (originalJson != null) {

                    LogUtil.d(" ShopActivity getOriginalJson : " + originalJson);
                    LogUtil.d("ShopActivity getSignature : " + signature);

                    List<NameValuePair> paramInfo = new ArrayList<>();
                    paramInfo.add(new BasicNameValuePair("signature", signature));
                    paramInfo.add(new BasicNameValuePair("originalJson", originalJson));

                    HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.PAYMENT_URL, User.class, paramInfo, HttpMethod.PUT, getApplicationContext());
                    RequestFactory requestFactory = new RequestFactory();
                    requestFactory.setProgressHandler(new ProgressHandler(ShopActivity.this, false));
                    requestFactory.create(httpRequestVO, new HttpResponseCallback<User>() {
                        @Override
                        public void onResponse(User user) {
                            if (user != null) {
                                setPointText(user);
                                SharedPrefHelper.getInstance(ShopActivity.this).setSharedPreferences(SharedPrefHelper.POINT, user.getPoint());
                            }
                        }
                    }).execute();

                }
            } else {
                LogUtil.d(" ShopActivity error result code : " + resultCode);
            }
        }
    }

    // TODO : 쓰레드로?
    public void AlreadyPurchaseItems() {
        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {

                ArrayList purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                String[] tokens = new String[purchaseDataList.size()];
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = (String) purchaseDataList.get(i);
                    JSONObject jo = new JSONObject(purchaseData);
                    tokens[i] = jo.getString("purchaseToken");
                    // 여기서 tokens를 모두 컨슘 해주기
                    mService.consumePurchase(3, getPackageName(), tokens[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.use_history_button)
    void onCilckUseHistoryButton() {
        Intent intent = new Intent(this, PointLogActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.close_button)
    void onCilckCloseButton() {
        finish();
    }

    @OnClick(R.id.point_30_button)
    void onCilck30Button() {
        buyItem("bird_30");
    }

    @OnClick(R.id.point_80_button)
    void onCilck80Button() {
        buyItem("bird_80");
    }

    @OnClick(R.id.point_150_button)
    void onCilck150Button() {
        buyItem("bird_150");
    }

    @OnClick(R.id.point_300_button)
    void onCilck300Button() {
        buyItem("bird_300");
    }

    @OnClick(R.id.point_600_button)
    void onCilck600Button() {
        buyItem("bird_600");
    }

//    @OnClick(R.id.error_button)
//    void onCilckErrorButton() {
//        Intent intent = new Intent(this, WebViewActivity.class);
//        Bundle extras = new Bundle();
//        extras.putString(WebViewActivity.WEB_VIEW_TITLE_NAME, getString(R.string.notice));
//        extras.putString(WebViewActivity.WEB_VIEW_URL_NAME, "http://devpesoft.cafe24.com/notice/154");
//        intent.putExtras(extras);
//        startActivity(intent);
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mServiceConn != null) {
            unbindService(mServiceConn);
        }

        ButterKnife.unbind(this);
    }

}
