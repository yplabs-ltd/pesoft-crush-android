package net.pesofts.crush.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import net.pesofts.crush.Constants;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.DateUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.activity.MainActivity;
import net.pesofts.crush.activity.StoryDetailActivity;
import net.pesofts.crush.model.StoryData;
import net.pesofts.crush.model.StoryVoice;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpNetworkError;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.network.RequestManager;
import net.pesofts.crush.widget.CircularNetworkImageView;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by erkas on 2017. 4. 30..
 */

public class StoryStoreFragment extends Fragment {

    private static final String TAG = "StoryStoreFragment";
    private StoryAdapter adapter;
    private String userId;
    private boolean isActive;
    private boolean needRefresh;
    private String gender;

    public static StoryStoreFragment newInstance(String userId, String gender) {
        StoryStoreFragment fragment = new StoryStoreFragment();

        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("gender", gender);
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.id_top_desc)
    TextView mTopDesc;
    @Bind(R.id.id_list_story)
    RecyclerView mListStory;
    @Bind(R.id.empty_list_layout)
    View mEmptyView;

    @OnClick(R.id.close_button)
    public void onClickClose() {
        EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.STATUS_CHANGE));
        getActivity().finish();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_store, container, false);

        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        userId = getArguments().getString("userId");
        gender = getArguments().getString("gender");

        int color = ResourcesCompat.getColor(getResources(), R.color.main_red_color, null);
        SpannableString spannable = new SpannableString(mTopDesc.getText());
        spannable.setSpan(new ForegroundColorSpan(color), 12, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannable.setSpan(new ForegroundColorSpan(color), 39, 41, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTopDesc.setText(spannable);

        mListStory.setLayoutManager(new LinearLayoutManager(getContext()));
        mListStory.setHasFixedSize(true);

        adapter = new StoryAdapter() {
            @Override
            public StoryHoler onCreateViewHolder(ViewGroup parent, int viewType) {
                final StoryHoler holder = super.onCreateViewHolder(parent, viewType);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        StoryData storyData = adapter.getData(position);

                        Intent intent = new Intent(getContext(), StoryDetailActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("userId", userId);
                        intent.putExtra("gender", gender);
                        intent.putExtra("storyData", storyData);

                        startActivity(intent);
                    }
                });

                return holder;
            }
        };
        mListStory.setAdapter(adapter);

        mListStory.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);

        getVoiceChatList();
    }

    private void getVoiceChatList() {
        Type type = new TypeToken<List<StoryData>>() {
        }.getType();
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.URL_STORY_STORE_LIST, type, null, getContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<List<StoryData>>() {
            @Override
            public void onResponse(List<StoryData> result) {

                if (result.size() == 0) {
                    return;
                }
                mEmptyView.setVisibility(View.GONE);
                mListStory.setVisibility(View.VISIBLE);

                adapter.setData(result);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(HttpNetworkError error) {
                Log.e(TAG, null, error);
            }
        }).execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(RefreshEvent refreshEvent) {
        if (refreshEvent.action == RefreshEvent.Action.STATUS_CHANGE) {
            needRefresh = true;
        }
    }

    public void refresh() {
        if (needRefresh) {
            if (getActivity() != null && isActive) {
                getVoiceChatList();
                needRefresh = false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        isActive = true;
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        isActive = false;
    }

    static class StoryAdapter extends RecyclerView.Adapter<StoryHoler> {

        private List<StoryData> data;

        @Override
        public StoryHoler onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story_store, parent, false);
            return new StoryHoler(view);
        }

        @Override
        public void onBindViewHolder(final StoryHoler holder, int position) {

            StoryData storyData = data.get(position);

            holder.mImageThumbnail.setBlurEffect(true);
            holder.mImageThumbnail.setImageUrl(storyData.getSmallImageUrl(), RequestManager.getImageLoader());

            if (storyData.isNew()) {
                holder.mTextNew.setVisibility(View.VISIBLE);
            } else {
                holder.mTextNew.setVisibility(View.GONE);
            }

            holder.mTextNickname.setText(storyData.getName());

            float remainTime = Float.parseFloat(storyData.getRemainTime());
            holder.mTextDday.setText("D-" + (int) (remainTime / 24));

            holder.mTextDate.setText(DateUtil.getRemainHour(Long.parseLong(storyData.getModifiedat())));

            holder.mTextDesc.setText(storyData.getCount()+"개의 대화");
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        public void setData(List<StoryData> data) {
            this.data = data;
        }

        public StoryData getData(int position) {
            return data.get(position);
        }
    }

    static class StoryHoler extends RecyclerView.ViewHolder {

        @Bind(R.id.img_thumbnail)
        CircularNetworkImageView mImageThumbnail;
        @Bind(R.id.id_text_new)
        View mTextNew;
        @Bind(R.id.id_text_nickname)
        TextView mTextNickname;
        @Bind(R.id.id_text_date)
        TextView mTextDate;
        @Bind(R.id.id_text_desc)
        TextView mTextDesc;
        @Bind(R.id.id_text_dday)
        TextView mTextDday;

        public StoryHoler(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
