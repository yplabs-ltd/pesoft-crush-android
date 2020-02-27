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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.DateUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.Util.SnackbarUtil;
import net.pesofts.crush.adapter.ContactAdapter;
import net.pesofts.crush.fragment.AlertDialogFragment;
import net.pesofts.crush.fragment.OneButtonAlertDialogFragment;
import net.pesofts.crush.model.CardList;
import net.pesofts.crush.model.Contact;
import net.pesofts.crush.model.ContactList;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpNetworkError;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactActivity extends FragmentActivity {

    @Bind(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.review_layout)
    LinearLayout reviewLayout;
    @Bind(R.id.invite_button)
    Button inviteButton;
    @Bind(R.id.info_text)
    TextView infoText;
    @Bind(R.id.review_desc)
    TextView reviewDesc;
    @Bind(R.id.review_detail_desc1)
    TextView reviewDetailDesc1;
    @Bind(R.id.review_detail_desc2)
    TextView reviewDetailDesc2;

    private static final String NEED_NUMBER = "10";
    private List<Contact> checkList;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact);
        ButterKnife.bind(this);

        CrushApplication.getInstance().loggingView(getString(R.string.ga_contact));

        setViews();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setViews() {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.SMS_INVITE_CHECK_URL, CardList.class, null, getApplicationContext());
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<CardList>() {
            @Override
            public void onResponse(CardList cardList) {
                if (cardList != null) {
                    if (DateUtil.isPastTime(cardList.getNextDttm())) {
                        setContactList();
                    } else {
                        setReviewLayout(cardList.getNextDttm());
                    }
                }
            }

            @Override
            public void onError(HttpNetworkError error) {
                setReviewLayout(0);
            }
        }).execute();
    }

    private void setContactList() {
        List<Contact> contactList = getContactList();

        if (contactList != null && contactList.size() > 0) {
            final ContactAdapter adapter = new ContactAdapter(this, contactList);
            adapter.setOnContactClickListener(new ContactAdapter.OnContactClickListener() {
                @Override
                public void onContactClick(List<Contact> checkContactList) {
                    checkList = checkContactList;
                    if (checkContactList == null || checkContactList.size() == 0) {
                        inviteButton.setText(getString(R.string.get_point_invite, NEED_NUMBER));
                    } else {
                        inviteButton.setText(getString(R.string.get_point_invite, "(" + checkContactList.size() + "/" + NEED_NUMBER + ")"));
                    }
                }
            });
            recyclerView.setAdapter(adapter);
            inviteButton.setText(getString(R.string.get_point_invite, NEED_NUMBER));
            infoText.setText(getString(R.string.free_buchi_desc));

            recyclerView.setVisibility(View.VISIBLE);
            reviewLayout.setVisibility(View.GONE);
        } else {
            setReviewLayout(0);
        }

    }

    private void setReviewLayout(long nextDttm) {
        recyclerView.setVisibility(View.GONE);
        reviewLayout.setVisibility(View.VISIBLE);

        reviewDesc.setText(Html.fromHtml(getString(R.string.review_desc)));
        reviewDetailDesc1.setText(Html.fromHtml(getString(R.string.review_detail_desc1)));
        reviewDetailDesc2.setText(Html.fromHtml(getString(R.string.review_detail_desc2)));
        inviteButton.setText(getString(R.string.go_to_review));
        if (nextDttm == 0) {
            infoText.setText(getString(R.string.review_info_desc, ""));
        } else {
            infoText.setText(getString(R.string.review_info_desc, "D-" + DateUtil.getRemainDay(nextDttm) + " " + DateUtil.getRemainHourAndMinuteAndSec(nextDttm)));
        }
    }

    private List<Contact> getContactList() {

        // 주소록 URI
        Uri people = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        // 검색할 컬럼 정하기
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = getContentResolver().query(people, projection, ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1", null, ContactsContract.Contacts.DISPLAY_NAME);    // 전화번호부 가져오기

        int end = cursor.getCount();    // 전화번호부의 갯수 세기

        int count = 0;

        List<Contact> contactList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            // 컬럼명으로 컬럼 인덱스 찾기
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            do {
                String phoneNum = cursor.getString(phoneIndex);

                if (phoneNum.startsWith("010")) {
                    Contact contact = new Contact();
                    contact.setPhoneNumber(phoneNum);
                    contact.setName(cursor.getString(nameIndex));

                    contactList.add(contact);
                }

            } while (cursor.moveToNext() || count > end);
        }

        return contactList;

    }

    @OnClick(R.id.close_button)
    void onCilckCloseButton() {
        finish();
    }

    @OnClick(R.id.invite_button)
    void onCilckInviteButton() {
        if (recyclerView.getVisibility() == View.GONE) {
            List<NameValuePair> paramInfo = new ArrayList<>();
            paramInfo.add(new BasicNameValuePair("platform", "android"));
            HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.STORE_REVIEW_URL, User.class, paramInfo, HttpMethod.POST, getApplicationContext());
            new RequestFactory().create(httpRequestVO, new HttpResponseCallback<User>() {
                @Override
                public void onResponse(User user) {
                    if (user != null) {
                        SharedPrefHelper.getInstance(ContactActivity.this).setSharedPreferences(SharedPrefHelper.POINT, user.getPoint());
                    }
                }
            }).execute();

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
            startActivity(intent);
        } else {
            if (checkList == null || checkList.size() < 10) {
                SnackbarUtil.getSnackbar(inviteButton, getString(R.string.min_number_alert)).show();
                return;
            }
            ContactList contactList = new ContactList();
            List<String> phoneList = new ArrayList<>();
            for (Contact contact : checkList) {
                if (contact.getPhoneNumber().startsWith("010-")) {
                    phoneList.add(contact.getPhoneNumber().replaceFirst("010-", "+82-10-"));
                } else if (contact.getPhoneNumber().startsWith("010")) {
                    String number = contact.getPhoneNumber().replaceFirst("010", "+82-10-");
                    String[] split = number.split("-");
                    if (split.length <= 3) {
                        String substringFirst = number.substring(0, number.length() - 4);
                        String substringLast = number.substring(number.length() - 4, number.length());
                        number = substringFirst + "-" + substringLast;
                    }
                    phoneList.add(number);
                }
            }
            contactList.setPhoneNumberList(phoneList);

            HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.SMS_INVITE_URL, User.class, null, HttpMethod.POST, getApplicationContext());
            httpRequestVO.setPayloadEntity(contactList);
            RequestFactory requestFactory = new RequestFactory();
            requestFactory.setProgressHandler(new ProgressHandler(this, false));
            requestFactory.create(httpRequestVO, new HttpResponseCallback<User>() {
                @Override
                public void onResponse(User user) {
                    if (user != null) {
                        SharedPrefHelper.getInstance(ContactActivity.this).setSharedPreferences(SharedPrefHelper.POINT, user.getPoint());
                    }
                    setViews();

                    OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.success_invite));
                    oneButtonAlertDialogFragment.setArguments(bundle);

                    oneButtonAlertDialogFragment.show(getSupportFragmentManager(), "oneButtonAlertDialogFragment");
                }
            }).execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }

}
