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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.adapter.PointLogAdapter;
import net.pesofts.crush.model.PointLog;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.RequestFactory;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PointLogActivity extends FragmentActivity {

    @Bind(R.id.my_recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_point_log);
        ButterKnife.bind(this);

        CrushApplication.getInstance().loggingView(getString(R.string.ga_point_log));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Type type = new TypeToken<List<PointLog>>() {
        }.getType();
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.POINT_LOG_HISTORY_URL, type, null, this);
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<List<PointLog>>() {
            @Override
            public void onResponse(List<PointLog> pointLogList) {
                final PointLogAdapter adapter = new PointLogAdapter(PointLogActivity.this, pointLogList);
                recyclerView.setAdapter(adapter);
            }
        }).execute();
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

}
