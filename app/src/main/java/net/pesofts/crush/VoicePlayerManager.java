package net.pesofts.crush;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by erkas on 2017. 2. 27..
 */

public final class VoicePlayerManager {

    private static final String TAG = "VoicePlayerManager";
    private static final String VOICE_FILE_NAME = "/audiorecordtest.m4a";

    public interface VoiceUploadListener {
        void onUploadSuccess();
        void onUploadFailed();
    }

    private static volatile VoicePlayerManager instance;
    private String mFileName;
    private MediaPlayer mPlayer;
    private MediaRecorder mRecorder;

    private AtomicBoolean isPlay = new AtomicBoolean(false);

    private VoicePlayerManager() {
    }

    public static VoicePlayerManager getInstance() {
        if (instance == null) {
            synchronized (VoicePlayerManager.class) {
                if (instance == null) {
                    VoicePlayerManager temp = new VoicePlayerManager();
                    instance = temp;
                }
            }
        }

        return instance;
    }

    public String getFileName() {
        return mFileName;
    }

    public void voiceRecord(Context context) {
        mRecorder = new MediaRecorder();

        mFileName = context.getExternalCacheDir().getAbsolutePath();
        mFileName += VOICE_FILE_NAME;

        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mRecorder.setOutputFile(mFileName);
            mRecorder.prepare();
        } catch (Exception e) {
//            Log.e(LOG_TAG, "prepare() failed");
            Log.e(TAG, null, e);
        }

        mRecorder.start();
    }

    public void voiceRecordStop() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public void voiceUpload(final VoiceUploadListener voiceUploadListener) {
        File file = new File(mFileName);
        if (!file.exists()) {
            Log.e(TAG, "file not found!!! : " + mFileName);
        }

//        Log.d(TAG, "file size : " + Helper.readableFileSize(file.length()));
//
//        IonHelper.requestPostUrl(Constants.HOST + Constants.URL_FILE_UPLOAD, true)
//                .setMultipartParameter("fileType", "voice")
//                .setMultipartFile("file", "multipart/form-data", file)
//                .as(new TypeToken<ApiResult<FileUploadData>>(){})
//                .setCallback(new FutureCallback<ApiResult<FileUploadData>>() {
//                    @Override
//                    public void onCompleted(Exception e, ApiResult<FileUploadData> result) {
//                        if (e != null) {
//                            Log.e(TAG, null, e);
//                            voiceUploadListener.onUploadFailed();
//                            return;
//                        }
//
//                        if (result.getResult() == null || result.getResult().getCode() != 200) {
//                            Log.e(TAG, result.toString());
//                            voiceUploadListener.onUploadFailed();
//                            return;
//                        }
//
//                        Log.d(TAG, "result : " + result);
//                        requestVoiceRegister(result.getContent().getDownloadPath(), voiceUploadListener);
//                    }
//                });
    }

    private void requestVoiceRegister(final String path, final VoiceUploadListener voiceUploadListener) {
//        IonHelper.requestPostUrl(Constants.HOST + Constants.URL_VOICE_REGISTER, true)
//                .setBodyParameter("voiceFilePath", path)
//                .as(new TypeToken<ApiResult>(){})
//                .setCallback(new FutureCallback<ApiResult>() {
//                    @Override
//                    public void onCompleted(Exception e, ApiResult result) {
//                        if (e != null) {
//                            Log.e(TAG, null, e);
//                            voiceUploadListener.onUploadFailed();
//                            return;
//                        }
//
//                        if (result.getResult() == null || result.getResult().getCode() != 200) {
//                            Log.e(TAG, result.toString());
//                            voiceUploadListener.onUploadFailed();
//                            return;
//                        }
//
//                        Log.d(TAG, "result : " + result);
//                        MuseLoginManager.getInstance().setVoiceFile(path);
//
//                        voiceUploadListener.onUploadSuccess();
//                    }
//                });
    }

    public int voicePlay(Context context) {
        mFileName = context.getExternalCacheDir().getAbsolutePath();
        mFileName += VOICE_FILE_NAME;

        return voicePlay(mFileName);
    }

    public int voicePlay(String filePath) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }

        if (isPlay.getAndSet(true)) {
            return -1;
        }

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.reset();
                isPlay.set(false);
                Log.e(TAG, "onCompletion()");
            }
        });

        try {
            mPlayer.reset();
            mPlayer.setDataSource(filePath);
            mPlayer.prepare();
            Log.e(TAG, "onPrepared()");
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        return mPlayer.getDuration();
    }

    public void voicePlayStop() {
        isPlay.set(false);

        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }
}
