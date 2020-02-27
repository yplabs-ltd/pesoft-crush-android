package net.pesofts.crush.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.VoicePlayerManager;
import net.pesofts.crush.activity.ProfileActivity;
import net.pesofts.crush.activity.StoryStoreActivity;
import net.pesofts.crush.model.CardListSection;
import net.pesofts.crush.model.Result;
import net.pesofts.crush.model.StoryData;
import net.pesofts.crush.model.StoryUpdate;
import net.pesofts.crush.model.StoryVoice;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpNetworkError;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.network.RequestManager;
import net.pesofts.crush.widget.BounceBgView;
import net.pesofts.crush.widget.CircularNetworkImageView;
import net.pesofts.crush.widget.VoicePlayView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by erkas on 2017. 4. 24..
 */

public class StoryFragment extends Fragment {
    private static final String TAG = "StoryFragment";

    private static long STORY_UPDATT_DELAY = 5 * 60 * 1000;

    private StoryVoice mStoryVoice;
    private User user;
    private boolean isActive;
    private boolean needRefresh;
    private long updateTime;

    public static StoryFragment newInstance(User user) {
        StoryFragment fragment = new StoryFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Bind(R.id.id_text_title)
    TextView mTextTitle;
    @Bind(R.id.id_empty)
    View mEmpty;
    @Bind(R.id.id_content)
    View mContent;
    @Bind(R.id.id_bg_bounce)
    BounceBgView mBgBounce;
    @Bind(R.id.id_text_new)
    View mTextNew;

    @Bind(R.id.id_btn_voice_play)
    VoicePlayView mBtnVoicePlay;
    @Bind(R.id.id_img_thumbnail)
    CircularNetworkImageView mImageThumbnail;

    @OnClick(R.id.id_btn_report)
    public void onClickBtnReport() {
        if (!checkProfile()) {
            return;
        }

        mBtnVoicePlay.resetVoicePlay();

        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "신고하기");
        bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, "불쾌한 음성인가요?");
        bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, "지금 바로 신고해주세요.");
        bundle.putString(AlertDialogFragment.DIALOG_CONFIRM_NAME, "신고하기");
        bundle.putString(AlertDialogFragment.DIALOG_CANCEL_NAME, "취소");

        alertDialogFragment.setArguments(bundle);
        alertDialogFragment.setReport(true);
        alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
            @Override
            public void onDialogConfirmed() {
                List<NameValuePair> paramInfo = new ArrayList<>();
                paramInfo.add(new BasicNameValuePair("userId", user.getId()));
                paramInfo.add(new BasicNameValuePair("voiceCloudId", mStoryVoice.getVoicecloudid()));
                paramInfo.add(new BasicNameValuePair("passType", "REPORT"));

                HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.URL_STORY_PASS, Result.class, paramInfo, HttpMethod.POST, getContext());
                RequestFactory requestFactory = new RequestFactory();
                requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
                requestFactory.create(httpRequestVO, new HttpResponseCallback<Result>() {
                    @Override
                    public void onResponse(Result result) {
                        Log.d(TAG, result.toString());
                        getRandomVoice();
                    }

                    @Override
                    public void onError(HttpNetworkError error) {
                        Log.e(TAG, null, error);
                        mEmpty.setVisibility(View.VISIBLE);
                        mContent.setVisibility(View.GONE);
                    }
                }).execute();
            }
        });

        alertDialogFragment.show(getFragmentManager(), "alertDialogFragment");

    }

    @OnClick(R.id.id_btn_pass)
    public void onClickBtnPass() {
        if (!checkProfile()) {
            return;
        }

        mBtnVoicePlay.resetVoicePlay();

        List<NameValuePair> paramInfo = new ArrayList<>();
        paramInfo.add(new BasicNameValuePair("userId", user.getId()));
        paramInfo.add(new BasicNameValuePair("voiceCloudId", mStoryVoice.getVoicecloudid()));
        paramInfo.add(new BasicNameValuePair("passType", "PASS"));

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.URL_STORY_PASS, Result.class, paramInfo, HttpMethod.POST, getContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<Result>() {
            @Override
            public void onResponse(Result result) {
                Log.d(TAG, result.toString());
                getRandomVoiceAutoPlay();
            }

            @Override
            public void onError(HttpNetworkError error) {
                Log.e(TAG, null, error);
                mEmpty.setVisibility(View.VISIBLE);
                mContent.setVisibility(View.GONE);
            }
        }).execute();
    }

    @OnClick(R.id.id_btn_reply)
    public void onClickBtnReply() {
        if (!checkProfile()) {
            return;
        }

        mBtnVoicePlay.resetVoicePlay();
        if ("M".equals(user.getGender())) {
            int needPoint = 15;
            if (CommonUtil.haveEnoughPoint(getActivity(), needPoint)) {
                CrushApplication.getInstance().loggingEvent(getString(R.string.ga_story), getString(R.string.ga_story_reply), getString(R.string.ga_like_enough_money));

                AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "답장을 보내시겠습니까?");
                bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, "첫 답장에만 15버찌가 사용됩니다.\n(상대방은 무료로 답장할 수 있습니다.)");
//                bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, getString(R.string.story_reply_info, needPoint));

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

    @OnClick(R.id.id_btn_story_store)
    public void onClickBtnStoryStore() {
        if (!checkProfile()) {
            return;
        }

        Intent intent = new Intent(getContext(), StoryStoreActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("userId", user.getId());
        intent.putExtra("gender", user.getGender());

        startActivity(intent);
    }

    @OnClick({R.id.id_btn_new_story, R.id.id_btn_new_story_empty})
    public void onClickBtnNewStory() {
        if (!checkProfile()) {
            return;
        }

        if (STORY_UPDATT_DELAY > System.currentTimeMillis() - updateTime) {
            OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "새로운 이야기는 5분에 하나씩만 보낼\n있어요. 잠시후 다시 시도해주세요.");
            oneButtonAlertDialogFragment.setArguments(bundle);

            oneButtonAlertDialogFragment.show(getFragmentManager(), "oneButtonAlertDialogFragment");
            return;
        }

        if ("M".equals(user.getGender())) {
            int needPoint = 10;
            if (CommonUtil.haveEnoughPoint(getActivity(), needPoint)) {
                CrushApplication.getInstance().loggingEvent(getString(R.string.ga_story), getString(R.string.ga_story_reply), getString(R.string.ga_like_enough_money));

                AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "새로운 이야기를 올리시겠습니까?");
                bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, "10버찌가 사용됩니다.\n(상대방은 무료로 답장할 수 있습니다.)");
//                bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, getString(R.string.story_reply_info, needPoint));

                alertDialogFragment.setArguments(bundle);
                alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                    @Override
                    public void onDialogConfirmed() {
                        newStory();
                    }
                });

                alertDialogFragment.show(getFragmentManager(), "alertDialogFragment");
            }
            return;
        }

        newStory();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = (User) getArguments().getSerializable("user");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainLayout = inflater.inflate(R.layout.fragment_story, container, false);

        ButterKnife.bind(this, mainLayout);
        EventBus.getDefault().register(this);

        return mainLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mEmpty.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.GONE);
        mTextNew.setVisibility(View.GONE);

        mBtnVoicePlay.setVoicePlayListener(new VoicePlayView.VoicePlayListener() {
            @Override
            public void onPlay() {
                if (!checkProfile()) {
                    mBtnVoicePlay.resetVoicePlay();
                    return;
                }

                int duration = VoicePlayerManager.getInstance().voicePlay(mStoryVoice.getVoiceuripath());
                if (duration > 0) {
                    mBtnVoicePlay.startVoicePlayProgress(duration);
                }
            }

            @Override
            public void onStop() {
                VoicePlayerManager.getInstance().voicePlayStop();
            }
        });

        mBgBounce.startBounceAnimation();

        updateTime = SharedPrefHelper.getInstance(getContext()).getSharedPreferences(SharedPrefHelper.STORY_UPDATE_TIME, 0L);

        getRandomVoice();
        checkVoiceChatNew();
    }

    public void onEvent(RefreshEvent refreshEvent) {
        if (refreshEvent.action == RefreshEvent.Action.STATUS_CHANGE) {
            needRefresh = true;
        }
    }

    public void refresh() {
        if (needRefresh) {
            if (getActivity() != null && isActive) {
                checkVoiceChatNew();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBgBounce.stopBounceAnimation();

        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
    }

    private void getRandomVoiceAutoPlay() {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.URL_STORY_RANDOM_VOICE, StoryVoice.class, null, getContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.create(httpRequestVO, new HttpResponseCallback<StoryVoice>() {
            @Override
            public void onResponse(StoryVoice result) {

                if (result.getUserid() == 0) {
                    mEmpty.setVisibility(View.VISIBLE);
                    mContent.setVisibility(View.GONE);
                    return;
                }

                mEmpty.setVisibility(View.GONE);
                mContent.setVisibility(View.VISIBLE);

                mStoryVoice = result;
                mImageThumbnail.setBlurEffect(true);
                mImageThumbnail.setImageUrl(result.getImageurlpath(), RequestManager.getImageLoader());
                mTextTitle.setText(result.getUsername());

                mBtnVoicePlay.callOnClick();
            }

            @Override
            public void onError(HttpNetworkError error) {
                Log.e(TAG, null, error);
                mEmpty.setVisibility(View.VISIBLE);
                mContent.setVisibility(View.GONE);
            }
        }).execute();
    }

    private void getRandomVoice() {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.URL_STORY_RANDOM_VOICE, StoryVoice.class, null, getContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.create(httpRequestVO, new HttpResponseCallback<StoryVoice>() {
            @Override
            public void onResponse(StoryVoice result) {

                if (result.getUserid() == 0) {
                    mEmpty.setVisibility(View.VISIBLE);
                    mContent.setVisibility(View.GONE);
                    return;
                }

                mEmpty.setVisibility(View.GONE);
                mContent.setVisibility(View.VISIBLE);

                mStoryVoice = result;
                mImageThumbnail.setBlurEffect(true);
                mImageThumbnail.setImageUrl(result.getImageurlpath(), RequestManager.getImageLoader());
                mTextTitle.setText(result.getUsername());
            }

            @Override
            public void onError(HttpNetworkError error) {
                Log.e(TAG, null, error);
                mEmpty.setVisibility(View.VISIBLE);
                mContent.setVisibility(View.GONE);
            }
        }).execute();
    }

    private void checkVoiceChatNew() {
        Type type = new TypeToken<List<StoryData>>() {
        }.getType();
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.URL_STORY_STORE_LIST, type, null, getContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.create(httpRequestVO, new HttpResponseCallback<List<StoryData>>() {
            @Override
            public void onResponse(List<StoryData> result) {
                if (result.size() == 0) {
                    return;
                }

                for (StoryData storyData : result) {
                    if (storyData.isNew()) {
                        mTextNew.setVisibility(View.VISIBLE);
                        return;
                    }
                }

                mTextNew.setVisibility(View.GONE);
            }

            @Override
            public void onError(HttpNetworkError error) {
                Log.e(TAG, null, error);
            }
        }).execute();
    }

    private boolean checkProfile() {
        if (CommonUtil.isCompleteProfileUser(getActivity())) {
            return true;
        } else if (CommonUtil.isFirstReviewingUser(getActivity())) {
            OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.review_profile_and_wait_desc));
            oneButtonAlertDialogFragment.setArguments(bundle);

            oneButtonAlertDialogFragment.show(getFragmentManager(), "oneButtonAlertDialogFragment");

            return false;
        } else {
            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "프로필 입력화면으로 이동합니다.");
            bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, getString(R.string.no_profile_desc, getString(R.string.story)));
            bundle.putString(AlertDialogFragment.DIALOG_CONFIRM_NAME, "이동");
            bundle.putString(AlertDialogFragment.DIALOG_CANCEL_NAME, "취소");

            alertDialogFragment.setArguments(bundle);
            alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                @Override
                public void onDialogConfirmed() {
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    startActivity(intent);
                }
            });

            alertDialogFragment.show(getFragmentManager(), "alertDialogFragment");
            return false;
        }
    }

    private void sendReply() {
        StoryDialogFragment dialogFragment = StoryDialogFragment.newInstance(true, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "음성이 발송되었습니다", Toast.LENGTH_SHORT).show();
                }

                String fileUrl = resultData.getString(Constants.EXTRA_RESULT_DATA);
                String url = Constants.URL_STORY_REPLY_START;

                List<NameValuePair> paramInfo = new ArrayList<>();
                paramInfo.add(new BasicNameValuePair("userId", user.getId()));
                paramInfo.add(new BasicNameValuePair("urlPath", fileUrl));
                paramInfo.add(new BasicNameValuePair("otherUserId", String.valueOf(mStoryVoice.getUserid())));
                paramInfo.add(new BasicNameValuePair("otherVoiceCloudId", mStoryVoice.getVoicecloudid()));

                HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(url, StoryVoice.class, paramInfo, HttpMethod.POST, getContext());
                RequestFactory requestFactory = new RequestFactory();
                requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
                requestFactory.create(httpRequestVO, new HttpResponseCallback<StoryVoice>() {
                    @Override
                    public void onResponse(StoryVoice result) {
                        // reponse: {"code":"OK"}
                        Log.e(TAG, result.toString());

                        // go next
                        getRandomVoice();
                    }

                    @Override
                    public void onError(HttpNetworkError error) {
                        Log.e(TAG, "onClickBtnReply", error);
                    }
                }).execute();
            }
        });

        dialogFragment.show(getFragmentManager(), StoryDialogFragment.TAG);
    }

    private void newStory() {
        StoryDialogFragment dialogFragment = StoryDialogFragment.newInstance(false, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "음성이 발송되었습니다", Toast.LENGTH_SHORT).show();
                }

                String fileUrl = resultData.getString(Constants.EXTRA_RESULT_DATA);
                String url = Constants.URL_STORY_NEW_START;

                List<NameValuePair> paramInfo = new ArrayList<>();
                paramInfo.add(new BasicNameValuePair("userId", user.getId()));
                paramInfo.add(new BasicNameValuePair("urlPath", fileUrl));

                HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(url, StoryUpdate.class, paramInfo, HttpMethod.POST, getContext());
                RequestFactory requestFactory = new RequestFactory();
                requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
                requestFactory.create(httpRequestVO, new HttpResponseCallback<StoryUpdate>() {
                    @Override
                    public void onResponse(StoryUpdate result) {
                        // reponse: {"code":"OK"}
                        Log.e(TAG, result.toString());

                        updateTime = result.getUpdateTime();
                        SharedPrefHelper.getInstance(getContext()).setSharedPreferences(SharedPrefHelper.STORY_UPDATE_TIME, updateTime);
                    }

                    @Override
                    public void onError(HttpNetworkError error) {
                        Log.e(TAG, "onClickBtnNewStory", error);
                    }
                }).execute();
            }
        });

        dialogFragment.show(getFragmentManager(), StoryDialogFragment.TAG);
    }
}
