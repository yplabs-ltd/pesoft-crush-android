package net.pesofts.crush.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.DateUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.VoicePlayerManager;
import net.pesofts.crush.model.Result;
import net.pesofts.crush.model.StoryData;
import net.pesofts.crush.model.StoryDetailData;
import net.pesofts.crush.model.StoryVoice;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpNetworkError;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.network.RequestManager;
import net.pesofts.crush.widget.CircularNetworkImageView;
import net.pesofts.crush.widget.VoicePlaySmallView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by erkas on 2017. 5. 10..
 */

public class StoryDetailFragment extends Fragment {
    public static final String TAG = "StoryDetailFragment";
    private StoryData storyData;
    private String userId;
    private StoryDetailAdapter adapter;
    private String gender;

    public static StoryDetailFragment newInstance(String userId, String gender, StoryData storyData) {
        StoryDetailFragment fragment = new StoryDetailFragment();

        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("gender", gender);
        args.putParcelable("storyData", storyData);

        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.id_text_title)
    TextView mTextTitle;
    @Bind(R.id.id_list_detail)
    RecyclerView mListDetail;
    @Bind(R.id.id_btn_reply)
    View mBtnReply;

    @OnClick(R.id.id_btn_reply)
    public void onClickBtnReply() {
        if (adapter.getMySendCount() == 3 && "M".equals(gender)) {

            int needPoint = 30;
            if (CommonUtil.haveEnoughPoint(getActivity(), needPoint)) {
                CrushApplication.getInstance().loggingEvent(getString(R.string.ga_story_detail), getString(R.string.ga_story_reply), getString(R.string.ga_like_enough_money));

                AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.ask_story_reply));
//                bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, getString(R.string.story_reply_info));
                bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, getString(R.string.story_reply_info, needPoint));

                alertDialogFragment.setArguments(bundle);
                alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                    @Override
                    public void onDialogConfirmed() {
                        sendReply();
                    }
                });

                alertDialogFragment.show(getFragmentManager(), "alertDialogFragment");
            }
            return;
        }

        sendReply();
    }

    @OnClick(R.id.close_button)
    public void onClickClose() {
        EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.STATUS_CHANGE));
        getActivity().finish();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_detail, container, false);

        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        userId = getArguments().getString("userId");
        gender = getArguments().getString("gender");
        storyData = args.getParcelable("storyData");

        mTextTitle.setText(storyData.getName());

        mListDetail.setLayoutManager(new LinearLayoutManager(getContext()));
        mListDetail.setHasFixedSize(true);

        adapter = new StoryDetailAdapter(userId, storyData) {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);

                switch (viewType) {
                    case 0:
                        StoryDetailHeaderHolder headerHolder = (StoryDetailHeaderHolder) holder;
                        headerHolder.mBtnReport.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                                Bundle bundle = new Bundle();
                                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "신고하기");
                                bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, "불쾌한 음성인가요?");
                                bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, "지금 바로 신고해주세요.\n(신고하면 방은 삭제됩니다)");
                                bundle.putString(AlertDialogFragment.DIALOG_CONFIRM_NAME, "신고하기");
                                bundle.putString(AlertDialogFragment.DIALOG_CANCEL_NAME, "취소");

                                alertDialogFragment.setArguments(bundle);
                                alertDialogFragment.setReport(true);
                                alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                                    @Override
                                    public void onDialogConfirmed() {
                                        Map<String, String> headerInfo = new HashMap<>();
                                        HttpUtil.addSessionCookie(headerInfo, getContext());

                                        Ion.with(getContext())
                                                .load("POST", Constants.URL_STORY_PASS_ROOM)
                                                .addHeader(HttpUtil.COOKIE_KEY, headerInfo.get(HttpUtil.COOKIE_KEY))
                                                .setBodyParameter("voiceChatRoomId", storyData.getId())
                                                .setBodyParameter("passType", "REPORT")
                                                .asString()
                                                .setCallback(new FutureCallback<String>() {
                                                    @Override
                                                    public void onCompleted(Exception e, String result) {
                                                        if (e != null) {
                                                            Log.e(TAG, null, e);
                                                            return;
                                                        }

                                                        Log.d(TAG, result);
                                                        onClickClose();
                                                    }
                                                });
                                    }
                                });

                                alertDialogFragment.show(getFragmentManager(), "alertDialogFragment");

                            }
                        });
                        break;
                    case 1:
                        final StoryDetailHolder detailHolder = (StoryDetailHolder) holder;
                        detailHolder.mBtnLeftVoicePlay.setVoicePlayListener(new VoicePlaySmallView.VoicePlayListener() {
                            @Override
                            public void onPlay() {
                                StoryDetailData data = adapter.getData(detailHolder.getAdapterPosition());
                                int duration = VoicePlayerManager.getInstance().voicePlay(data.getVoiceUrl());
                                if (duration < 0) {
                                    detailHolder.mBtnLeftVoicePlay.stopVoicePlayProgress();
                                } else {
                                    detailHolder.mBtnLeftVoicePlay.startVoicePlayProgress(duration);
                                }
                            }

                            @Override
                            public void onStop() {
                                VoicePlayerManager.getInstance().voicePlayStop();
                            }
                        });

                        detailHolder.mBtnRightVoicePlay.setVoicePlayListener(new VoicePlaySmallView.VoicePlayListener() {
                            @Override
                            public void onPlay() {
                                StoryDetailData data = adapter.getData(detailHolder.getAdapterPosition());
                                int duration = VoicePlayerManager.getInstance().voicePlay(data.getVoiceUrl());
                                if (duration < 0) {
                                    detailHolder.mBtnRightVoicePlay.stopVoicePlayProgress();
                                } else {
                                    detailHolder.mBtnRightVoicePlay.startVoicePlayProgress(duration);
                                }
                            }

                            @Override
                            public void onStop() {
                                VoicePlayerManager.getInstance().voicePlayStop();
                            }
                        });
                        break;
                }

                return holder;
            }
        };

        mListDetail.setAdapter(adapter);

        Map<String, String> headerInfo = new HashMap<>();
        HttpUtil.addSessionCookie(headerInfo, getContext());

        Ion.with(getContext())
                .load("POST", Constants.URL_STORY_CHAT_CHECK)
                .addHeader(HttpUtil.COOKIE_KEY, headerInfo.get(HttpUtil.COOKIE_KEY))
                .setBodyParameter("voiceChatRoomId", storyData.getId())
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            Log.e(TAG, null, e);
                            return;
                        }

                        Log.d(TAG, result);
                    }
                });

        getChatData();
    }

    private void sendReply() {
        StoryDialogFragment dialogFragment = StoryDialogFragment.newInstance(true, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {

                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "음성이 발송되었습니다", Toast.LENGTH_SHORT).show();
                }

                final String fileUrl = resultData.getString(Constants.EXTRA_RESULT_DATA);
                String url = Constants.URL_STORY_REPLY;
                if (adapter.getMySendCount() == 3) {
                    url = Constants.URL_STORY_REPLY_PAY;
                }

                List<NameValuePair> paramInfo = new ArrayList<>();
                paramInfo.add(new BasicNameValuePair("userId", userId));
                paramInfo.add(new BasicNameValuePair("urlPath", fileUrl));
                paramInfo.add(new BasicNameValuePair("voiceChatRoomId", storyData.getId()));

                HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(url, Result.class, paramInfo, HttpMethod.POST, getContext());
                RequestFactory requestFactory = new RequestFactory();
                requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
                requestFactory.create(httpRequestVO, new HttpResponseCallback<Result>() {
                    @Override
                    public void onResponse(Result result) {
                        // reponse: {"code":"OK"}
//                        Log.e(TAG, result.toString());
                        if (!"OK".equals(result.getCode())) {
                            OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
                            Bundle bundle = new Bundle();
                            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "삭제된 이야기 입니다.");
                            oneButtonAlertDialogFragment.setArguments(bundle);

                            oneButtonAlertDialogFragment.show(getFragmentManager(), "oneButtonAlertDialogFragment");

                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.onBackPressed();
                            }
                            return;
                        }

                        // go next
                        getChatData();
                    }

                    @Override
                    public void onError(HttpNetworkError error) {
                        Log.e(TAG, "sendReply", error);
                        OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "삭제된 이야기 입니다.");
                        oneButtonAlertDialogFragment.setArguments(bundle);

                        oneButtonAlertDialogFragment.show(getFragmentManager(), "oneButtonAlertDialogFragment");

                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.onBackPressed();
                        }
                    }
                }).execute();
            }
        });

        dialogFragment.show(getFragmentManager(), StoryDialogFragment.TAG);
    }

    private void getChatData() {
        List<NameValuePair> paramInfo = new ArrayList<>();
        paramInfo.add(new BasicNameValuePair("voiceChatRoomId", storyData.getId()));

        TypeToken<List<StoryDetailData>> typeToken = new TypeToken<List<StoryDetailData>>() {};
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.URL_STORY_VOICE_CHAT, typeToken.getType(), paramInfo, HttpMethod.POST, getContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<List<StoryDetailData>>() {
            @Override
            public void onResponse(List<StoryDetailData> result) {

                if (adapter.getItemCount() > 1) {
                    int change = adapter.setData(result);
                    adapter.notifyItemRangeInserted(adapter.getItemCount() - change, change);
                } else {
                    int change = adapter.setData(result);
                    adapter.notifyItemRangeInserted(1, change);
                }

                mListDetail.scrollToPosition(adapter.getItemCount() - 1);

                if (adapter.isSendEnable()) {
                    mBtnReply.setEnabled(true);
                } else {
                    mBtnReply.setEnabled(false);
                }
            }

            @Override
            public void onError(HttpNetworkError error) {
                Log.e(TAG, "getChatData", error);
                OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "삭제된 이야기 입니다.");
                oneButtonAlertDialogFragment.setArguments(bundle);

                oneButtonAlertDialogFragment.show(getFragmentManager(), "oneButtonAlertDialogFragment");

                Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
            }
        }).execute();
    }

    static class StoryDetailAdapter extends RecyclerView.Adapter {

        private final String userId;
        private String otherUserId;
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        private final StoryData headerData;
        private List<StoryDetailData> data;
        private int mySendCount;

        public StoryDetailAdapter(String userId, StoryData storyData) {
            this.userId = userId;
            this.headerData = storyData;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case 0:
                    View view = inflater.inflate(R.layout.item_story_detail_top, parent, false);
                    return new StoryDetailHeaderHolder(view);
                case 1:
                    view = inflater.inflate(R.layout.item_story_detail, parent, false);
                    return new StoryDetailHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);
            switch (viewType) {
                case 0:
                    StoryDetailHeaderHolder headerHolder = (StoryDetailHeaderHolder) holder;

                    headerHolder.mImageThumbnail.setBlurEffect(true);
                    headerHolder.mImageThumbnail.setImageUrl(headerData.getSmallImageUrl(), RequestManager.getImageLoader());
                    break;
                case 1:
                    StoryDetailHolder detailHolder = (StoryDetailHolder) holder;

                    StoryDetailData detailData = data.get(position - 1);

                    if (userId.equals(String.valueOf(detailData.getId()))) {
                        detailHolder.mLeftLine.setVisibility(View.GONE);
                        detailHolder.mLeftTextContainer.setVisibility(View.GONE);
                        detailHolder.mBtnLeftVoicePlay.setVisibility(View.GONE);

                        detailHolder.mRightLine.setVisibility(View.VISIBLE);
                        detailHolder.mRightTextContainer.setVisibility(View.VISIBLE);
                        detailHolder.mBtnRightVoicePlay.setVisibility(View.VISIBLE);

                        detailHolder.mTextRightTitle.setText(detailData.getName());

                        detailHolder.mTextRightDate.setText(DateUtil.getRemainHour(Long.parseLong(detailData.getCreatedat())));

                    } else {
                        otherUserId = String.valueOf(detailData.getId());
                        detailHolder.mLeftLine.setVisibility(View.VISIBLE);
                        detailHolder.mLeftTextContainer.setVisibility(View.VISIBLE);
                        detailHolder.mBtnLeftVoicePlay.setVisibility(View.VISIBLE);

                        detailHolder.mRightLine.setVisibility(View.GONE);
                        detailHolder.mRightTextContainer.setVisibility(View.GONE);
                        detailHolder.mBtnRightVoicePlay.setVisibility(View.GONE);

                        detailHolder.mTextLeftTitle.setText(detailData.getName());

                        detailHolder.mTextLeftDate.setText(DateUtil.getRemainHour(Long.parseLong(detailData.getCreatedat())));
                    }
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public int getItemCount() {
            return data == null ? 1 : data.size() + 1;
        }

        public int setData(List<StoryDetailData> data) {
            int change = data.size();
            if (this.data != null) {
                change = Math.abs(change - this.data.size());
            }

            mySendCount = 0;
            for (StoryDetailData detailData : data) {
                if (userId.equals(String.valueOf(detailData.getId()))) {
                    mySendCount++;
                }
            }

            this.data = data;
            return change;
        }

        public StoryDetailData getData(int position) {
            return this.data.get(position - 1);
        }

        public String getOtherUserId() {
            return otherUserId;
        }

        public int getMySendCount() {
            return mySendCount;
        }

        public boolean isSendEnable() {
            if (getItemCount() <= 1) {
                return false;
            }
            StoryDetailData detailData = data.get(getItemCount() - 2);
            if (userId.equals(String.valueOf(detailData.getId()))) {
                return false;
            }
            return true;
        }
    }

    static class StoryDetailHeaderHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.id_img_thumbnail)
        CircularNetworkImageView mImageThumbnail;
        @Bind(R.id.id_btn_voice_play)
        View mBtnVoice;
        @Bind(R.id.id_btn_report)
        View mBtnReport;

        public StoryDetailHeaderHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    static class StoryDetailHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.id_left_line)
        View mLeftLine;
        @Bind(R.id.id_btn_left_voice_play)
        VoicePlaySmallView mBtnLeftVoicePlay;
        @Bind(R.id.id_text_left_title)
        TextView mTextLeftTitle;
        @Bind(R.id.id_text_left_date)
        TextView mTextLeftDate;
        @Bind(R.id.id_left_text_container)
        View mLeftTextContainer;

        @Bind(R.id.id_right_line)
        View mRightLine;
        @Bind(R.id.id_btn_right_voice_play)
        VoicePlaySmallView mBtnRightVoicePlay;
        @Bind(R.id.id_text_right_title)
        TextView mTextRightTitle;
        @Bind(R.id.id_text_right_date)
        TextView mTextRightDate;
        @Bind(R.id.id_right_text_container)
        View mRightTextContainer;

        public StoryDetailHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
