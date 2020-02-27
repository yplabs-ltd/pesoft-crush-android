package net.pesofts.crush.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import net.pesofts.crush.Constants;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.ImageUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.StringUtil;
import net.pesofts.crush.VoicePlayerManager;
import net.pesofts.crush.network.HttpNetworkError;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.network.RequestManager;
import net.pesofts.crush.network.VolleyMultipartRequest;
import net.pesofts.crush.permission.PermissionListener;
import net.pesofts.crush.permission.PermissionManager;
import net.pesofts.crush.permission.PermissionUtils;
import net.pesofts.crush.widget.VoiceRecordView;

import java.io.File;
import java.util.Map;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by erkas on 2017. 4. 26..
 */

public class StoryDialogFragment extends DialogFragment {

    public static final String TAG = "StoryDialogFragment";
    private boolean isReply;
    private ResultReceiver receiver;

    public static StoryDialogFragment newInstance(boolean isReply, ResultReceiver receiver) {
        StoryDialogFragment fragment = new StoryDialogFragment();

        Bundle args = new Bundle();
        args.putBoolean("isReply", isReply);
        args.putParcelable(Constants.EXTRA_RECEIVER, receiver);

        fragment.setArguments(args);

        return fragment;
    }

    @Bind(R.id.id_text_title)
    TextView mTextTitle;
    @Bind(R.id.id_text_desc)
    TextView mTextDesc;
    @Bind(R.id.id_btn_voice_record)
    VoiceRecordView mBtnVoiceRecord;
    @Bind(R.id.id_btn_send)
    View mBtnSend;
    @Bind(R.id.id_btn_voice_reset)
    View mBtnVoiceReset;

    @OnClick(R.id.id_btn_cancel)
    public void onClickBtnCancel() {
        dismiss();
    }

    @OnClick(R.id.id_btn_send)
    public void onClickBtnSend() {

        getUploadVoiceInfoAndUploadVoice();
    }

    @OnClick(R.id.id_btn_voice_reset)
    public void onClickBtnVoiceReset() {
        mBtnVoiceRecord.resetVoiceRecord();
        mBtnVoiceReset.setVisibility(View.GONE);
        mTextDesc.setVisibility(View.VISIBLE);
        mBtnSend.setEnabled(false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TransparentDialogFragment);

        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            isReply = args.getBoolean("isReply");
            receiver = args.getParcelable(Constants.EXTRA_RECEIVER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_dialog, container, false);

        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        if (!isReply) {
            mTextTitle.setText("새 이야기 시작");

            // random value
            String[] storyExList = getResources().getStringArray(R.array.story_ex);
            int random = Math.abs(new Random().nextInt());
            mTextDesc.setText(storyExList[random % storyExList.length]);
        }

        mBtnVoiceRecord.setVoiceRecordListener(new VoiceRecordView.VoiceRecordListener() {
            @Override
            public void onRecord() {
                PermissionManager.getInstance().requestPermission(
                        getContext(),
                        PermissionUtils.MICROPHONE,
                        PermissionUtils.MICROPHONE_NAME,
                        new PermissionListener() {
                            @Override
                            public void onGranted() {
                                if (getContext() != null) {
                                    VoicePlayerManager.getInstance().voiceRecord(getContext());
                                    mBtnVoiceRecord.startRecordProgress(10000);
                                }
                            }

                            @Override
                            public void onDenied() {
                                mBtnVoiceRecord.resetVoiceRecord();
                            }
                        }
                );

            }

            @Override
            public void onStopRecord() {
                VoicePlayerManager.getInstance().voiceRecordStop();
                mBtnSend.setEnabled(true);
                mBtnVoiceReset.setVisibility(View.VISIBLE);
                mTextDesc.setVisibility(View.GONE);
            }

            @Override
            public void onPlay() {
                int duration = VoicePlayerManager.getInstance().voicePlay(getContext());
                mBtnVoiceRecord.startVoicePlayProgress(duration);
            }

            @Override
            public void onStopPlay() {
                VoicePlayerManager.getInstance().voicePlayStop();
            }
        });
    }

    private void getUploadVoiceInfoAndUploadVoice() {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.URL_STORY_VOICE_UPLOAD, Map.class, null, getContext());
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<Map>() {
            @Override
            public void onResponse(Map result) {
                Log.d("result", result.toString());

                uploadVoice(result);
            }

            @Override
            public void onError(HttpNetworkError error) {
                Log.e("result", null, error);
            }
        }).execute();
    }

    public void uploadVoice(Map updateInfo) {
        final String uploadImagePath = (String) updateInfo.get("uploadImagePath");

        String filePath = ImageUtil.getFilePathFromUri(getTempUri(), getContext());
        if (StringUtil.isEmpty(filePath)) {
            return;
        }

        final ProgressHandler progressHandler = new ProgressHandler(getActivity(), false);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Constants.IMAGE_SERVER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse : " + response);

                        progressHandler.onCancel();

                        Bundle data = new Bundle();
                        data.putString(Constants.EXTRA_RESULT_DATA, uploadImagePath);
                        receiver.send(1000, data);

                        dismiss();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse : " + error.getMessage());

                        progressHandler.onCancel();
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

    private Uri getTempUri() {
        Uri uri = null;
        try {
            uri = Uri.fromFile(new File(VoicePlayerManager.getInstance().getFileName()));
        } catch (Exception e) {
            LogUtil.w("getTempUri fail : " + e.getMessage());
        }
        return uri;
    }
}
