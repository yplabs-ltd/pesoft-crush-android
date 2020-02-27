package com.sendbird.android.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.MessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdEventHandler;
import com.sendbird.android.SendBirdFileUploadEventHandler;
import com.sendbird.android.SendBirdNotificationHandler;
import com.sendbird.android.model.BroadcastMessage;
import com.sendbird.android.model.Channel;
import com.sendbird.android.model.FileInfo;
import com.sendbird.android.model.FileLink;
import com.sendbird.android.model.Mention;
import com.sendbird.android.model.Message;
import com.sendbird.android.model.MessageModel;
import com.sendbird.android.model.MessagingChannel;
import com.sendbird.android.model.ReadStatus;
import com.sendbird.android.model.SystemMessage;
import com.sendbird.android.model.TypeStatus;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.StringUtil;
import net.pesofts.crush.activity.UserDetailActivity;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.RequestManager;
import net.pesofts.crush.widget.CircularNetworkImageView;

import java.io.File;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


public class SendBirdMessagingActivity extends FragmentActivity {
    private static final int REQUEST_MESSAGING_CHANNEL_LIST = 100;

    private SendBirdChatFragment mSendBirdMessagingFragment;
    private SendBirdMessagingAdapter mSendBirdMessagingAdapter;

    private TextView mTxtChannelUrl;
    private CountDownTimer mTimer;
    private MessagingChannel mMessagingChannel;
    private Bundle mSendBirdInfo;

    public static Bundle makeMessagingJoinArgs(String appKey, String uuid, String nickname, String profileUrl, String channelUrl) {
        Bundle args = new Bundle();
        args.putBoolean("join", true);
        args.putString("appKey", appKey);
        args.putString("uuid", uuid);
        args.putString("nickname", nickname);
        args.putString("profileUrl", profileUrl);
        args.putString("channelUrl", channelUrl);
        return args;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendbird_messaging);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        CrushApplication.getInstance().loggingView(getString(R.string.ga_chat));

        initFragment();

        initUIComponents();
        initSendBird(getIntent().getExtras());

        if (mSendBirdInfo.getBoolean("start")) {
            String[] targetUserIds = mSendBirdInfo.getStringArray("targetUserIds");
            SendBird.startMessaging(Arrays.asList(targetUserIds));
        } else if (mSendBirdInfo.getBoolean("join")) {
            String channelUrl = mSendBirdInfo.getString("channelUrl");
            SendBird.joinMessaging(channelUrl);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new CountDownTimer(60 * 60 * 24 * 7 * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mSendBirdMessagingAdapter != null) {
                    if (mSendBirdMessagingAdapter.checkTypeStatus()) {
                        mSendBirdMessagingAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFinish() {
            }
        };
        mTimer.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SendBird.disconnect();
    }

    @Override
    public void finish() {
        super.finish();
    }


    private void initFragment() {
        mSendBirdMessagingFragment = new SendBirdChatFragment();

        mSendBirdMessagingAdapter = new SendBirdMessagingAdapter(this);
        mSendBirdMessagingFragment.setSendBirdMessagingAdapter(mSendBirdMessagingAdapter);

        mSendBirdMessagingFragment.setSendBirdChatHandler(new SendBirdChatFragment.SendBirdChatHandler() {

            @Override
            public void onChannelListClicked() {
                startActivityForResult(new Intent(SendBirdMessagingActivity.this, SendBirdMessagingChannelListActivity.class), REQUEST_MESSAGING_CHANNEL_LIST);
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdMessagingFragment)
                .commit();
    }

    private static String getDisplayMemberNames(List<MessagingChannel.Member> members) {
        if (members.size() < 2) {
            return "No Members";
        } else if (members.size() == 2) {
            StringBuffer names = new StringBuffer();
            for (MessagingChannel.Member member : members) {
                if (member.getId().equals(SendBird.getUserId())) {
                    continue;
                }

                names.append(", " + member.getName());
            }
            return names.delete(0, 2).toString();
        } else {
            return "Group " + members.size();
        }
    }

    private void initSendBird(Bundle extras) {
        mSendBirdInfo = extras;

//        String appKey = extras.getString("appKey");
//        String uuid = extras.getString("uuid");
//        String nickname = extras.getString("nickname");
//        String profileUrl = extras.getString("profileUrl");
//
//        SendBird.init(appKey);
//        SendBird.login(uuid, nickname, profileUrl);
        SendBird.registerNotificationHandler(new SendBirdNotificationHandler() {
            @Override
            public void onMessagingChannelUpdated(MessagingChannel messagingChannel) {
                if (mMessagingChannel != null && mMessagingChannel.getId() == messagingChannel.getId()) {
                    updateMessagingChannel(messagingChannel);
                }
            }

            @Override
            public void onMentionUpdated(Mention mention) {

            }
        });

        SendBird.setEventHandler(new SendBirdEventHandler() {
            @Override
            public void onConnect(Channel channel) {
            }

            @Override
            public void onError(int code) {
                Log.e("SendBird", "Error code: " + code);
            }

            @Override
            public void onChannelLeft(Channel channel) {
            }

            @Override
            public void onMessageReceived(Message message) {
                SendBird.markAsRead();
                mSendBirdMessagingAdapter.addMessageModel(message);
            }

            @Override
            public void onSystemMessageReceived(SystemMessage systemMessage) {
                switch (systemMessage.getCategory()) {
                    case SystemMessage.CATEGORY_TOO_MANY_MESSAGES:
                        systemMessage.setMessage("Too many messages. Please try later.");
                        break;
                    case SystemMessage.CATEGORY_MESSAGING_USER_BLOCKED:
                        systemMessage.setMessage("해당 이성이 대화방을 나갔습니다.");
                        break;
                    case SystemMessage.CATEGORY_MESSAGING_USER_DEACTIVATED:
                        systemMessage.setMessage("Deactivated.");
                        break;
                }

                mSendBirdMessagingAdapter.addMessageModel(systemMessage);
            }

            @Override
            public void onBroadcastMessageReceived(BroadcastMessage broadcastMessage) {
                mSendBirdMessagingAdapter.addMessageModel(broadcastMessage);
            }

            @Override
            public void onFileReceived(FileLink fileLink) {
                mSendBirdMessagingAdapter.addMessageModel(fileLink);
            }

            @Override
            public void onReadReceived(ReadStatus readStatus) {
                mSendBirdMessagingAdapter.setReadStatus(readStatus.getUserId(), readStatus.getTimestamp());
            }

            @Override
            public void onTypeStartReceived(TypeStatus typeStatus) {
                mSendBirdMessagingAdapter.setTypeStatus(typeStatus.getUserId(), System.currentTimeMillis());
            }

            @Override
            public void onTypeEndReceived(TypeStatus typeStatus) {
                mSendBirdMessagingAdapter.setTypeStatus(typeStatus.getUserId(), 0);
            }

            @Override
            public void onAllDataReceived(SendBird.SendBirdDataType type, int count) {
                mSendBirdMessagingAdapter.notifyDataSetChanged();
                mSendBirdMessagingFragment.mListView.setSelection(mSendBirdMessagingAdapter.getCount() - 1);
            }

            @Override
            public void onMessageDelivery(boolean sent, String message, String data, String tempId) {
                if (!sent) {
                    mSendBirdMessagingFragment.mEtxtMessage.setText(message);
                }
            }

            @Override
            public void onMessagingStarted(final MessagingChannel messagingChannel) {
                mSendBirdMessagingAdapter.clear();
                updateMessagingChannel(messagingChannel);

                SendBird.queryMessageList(messagingChannel.getUrl()).load(Long.MAX_VALUE, 30, 10, new MessageListQuery.MessageListQueryResult() {
                    @Override
                    public void onResult(List<MessageModel> messageModels) {
                        for (MessageModel model : messageModels) {
                            mSendBirdMessagingAdapter.addMessageModel(model);
                        }
                        mSendBirdMessagingAdapter.notifyDataSetChanged();
                        mSendBirdMessagingFragment.mListView.setSelection(30);

                        SendBird.markAsRead(messagingChannel.getUrl());
                        SendBird.join(messagingChannel.getUrl());
                        SendBird.connect(mSendBirdMessagingAdapter.getMaxMessageTimestamp());
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }

            @Override
            public void onMessagingUpdated(MessagingChannel messagingChannel) {
                updateMessagingChannel(messagingChannel);
            }

            @Override
            public void onMessagingEnded(MessagingChannel messagingChannel) {
            }

            @Override
            public void onAllMessagingEnded() {
            }

            @Override
            public void onMessagingHidden(MessagingChannel messagingChannel) {
            }

            @Override
            public void onAllMessagingHidden() {
            }

        });

    }

    private void updateMessagingChannel(MessagingChannel messagingChannel) {
        mMessagingChannel = messagingChannel;
        mTxtChannelUrl.setText(getDisplayMemberNames(messagingChannel.getMembers()));

        Hashtable<String, Long> readStatus = new Hashtable<String, Long>();
        for (MessagingChannel.Member member : messagingChannel.getMembers()) {
            Long currentStatus = mSendBirdMessagingAdapter.mReadStatus.get(member.getId());
            if (currentStatus == null) {
                currentStatus = 0L;
            }
            readStatus.put(member.getId(), Math.max(currentStatus, messagingChannel.getLastReadMillis(member.getId())));
        }
        mSendBirdMessagingAdapter.resetReadStatus(readStatus);

        mSendBirdMessagingAdapter.setMembers(messagingChannel.getMembers());
        mSendBirdMessagingAdapter.notifyDataSetChanged();
    }

    private void initUIComponents() {
        mTxtChannelUrl = (TextView) findViewById(R.id.title_text);
        ImageButton mBtnClose = (ImageButton) findViewById(R.id.close_button);
        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static class SendBirdChatFragment extends Fragment {
        private static final int REQUEST_PICK_IMAGE = 100;

        private ListView mListView;
        private SendBirdMessagingAdapter mAdapter;
        private EditText mEtxtMessage;
        private Button mBtnSend;
        private ImageButton mBtnChannel;
        //        private ImageButton mBtnUpload;
//        private ProgressBar mProgressBtnUpload;
        private SendBirdChatHandler mHandler;

        public static interface SendBirdChatHandler {
            public void onChannelListClicked();
        }

        public void setSendBirdChatHandler(SendBirdChatHandler handler) {
            mHandler = handler;
        }

        public SendBirdChatFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sendbird_fragment_messaging, container, false);
            initUIComponents(rootView);
            return rootView;
        }


        private void initUIComponents(View rootView) {
            mListView = (ListView) rootView.findViewById(R.id.list);
            turnOffListViewDecoration(mListView);
            mListView.setAdapter(mAdapter);

            mBtnSend = (Button) rootView.findViewById(R.id.btn_send);
//            mBtnUpload = (ImageButton)rootView.findViewById(R.id.btn_upload);
//            mProgressBtnUpload = (ProgressBar)rootView.findViewById(R.id.progress_btn_upload);
            mEtxtMessage = (EditText) rootView.findViewById(R.id.etxt_message);

            mBtnSend.setEnabled(false);
            mBtnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    send();
                }
            });


//            mBtnUpload.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent();
//                    intent.setType("image/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE);
//                }
//            });

            mEtxtMessage.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            send();
                        }
                        return true; // Do not hide keyboard.
                    }

                    return false;
                }
            });
            mEtxtMessage.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            mEtxtMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mBtnSend.setEnabled(s.length() > 0);

                    if (s.length() > 0) {
                        SendBird.typeStart();
                    } else {
                        SendBird.typeEnd();
                    }
                }
            });
            mListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Helper.hideKeyboard(getActivity());
                    return false;
                }
            });
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_IDLE) {
                        if (view.getFirstVisiblePosition() == 0 && view.getChildCount() > 0 && view.getChildAt(0).getTop() == 0) {
                            SendBird.queryMessageList(SendBird.getChannelUrl()).prev(mAdapter.getMinMessageTimestamp(), 30, new MessageListQuery.MessageListQueryResult() {
                                @Override
                                public void onResult(List<MessageModel> messageModels) {
                                    if (messageModels.size() <= 0) {
                                        return;
                                    }

                                    for (MessageModel model : messageModels) {
                                        mAdapter.addMessageModel(model);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                    mListView.setSelection(messageModels.size());
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        } else if (view.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1 && view.getChildCount() > 0) {
                            SendBird.queryMessageList(SendBird.getChannelUrl()).next(mAdapter.getMaxMessageTimestamp(), 30, new MessageListQuery.MessageListQueryResult() {
                                @Override
                                public void onResult(List<MessageModel> messageModels) {
                                    if (messageModels.size() <= 0) {
                                        return;
                                    }

                                    for (MessageModel model : messageModels) {
                                        mAdapter.addMessageModel(model);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                }
            });
        }

        private void showUploadProgress(boolean tf) {
//            if(tf) {
//                mBtnUpload.setEnabled(false);
//                mBtnUpload.setVisibility(View.INVISIBLE);
//                mProgressBtnUpload.setVisibility(View.VISIBLE);
//            } else {
//                mBtnUpload.setEnabled(true);
//                mBtnUpload.setVisibility(View.VISIBLE);
//                mProgressBtnUpload.setVisibility(View.GONE);
//            }
        }

        private void turnOffListViewDecoration(ListView listView) {
            listView.setDivider(null);
            listView.setDividerHeight(0);
            listView.setHorizontalFadingEdgeEnabled(false);
            listView.setVerticalFadingEdgeEnabled(false);
            listView.setHorizontalScrollBarEnabled(false);
            listView.setVerticalScrollBarEnabled(true);
            listView.setSelector(new ColorDrawable(0x00ffffff));
            listView.setCacheColorHint(0x00000000); // For Gingerbread scrolling bug fix
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null) {
                    upload(data.getData());
                }
            }
        }

        private void send() {
            SendBird.send(mEtxtMessage.getText().toString());
            mEtxtMessage.setText("");

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Helper.hideKeyboard(getActivity());
            }
        }

        private void upload(Uri uri) {
            try {
                Cursor cursor = getActivity().getContentResolver().query(uri,
                        new String[]{
                                MediaStore.Images.Media.DATA,
                                MediaStore.Images.Media.MIME_TYPE,
                                MediaStore.Images.Media.SIZE,
                        },
                        null, null, null);
                cursor.moveToFirst();
                final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                final String mime = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                final int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                cursor.close();

                if (path == null) {
                    Toast.makeText(getActivity(), "Uploading file must be located in local storage.", Toast.LENGTH_LONG).show();
                } else {
                    showUploadProgress(true);
                    SendBird.uploadFile(new File(path), mime, size, "", new SendBirdFileUploadEventHandler() {
                        @Override
                        public void onUpload(FileInfo fileInfo, Exception e) {
                            showUploadProgress(false);
                            if (e != null) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), "Fail to upload the file.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            SendBird.sendFile(fileInfo);
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Fail to upload the file.", Toast.LENGTH_LONG).show();
            }
        }


        public void setSendBirdMessagingAdapter(SendBirdMessagingAdapter adapter) {
            mAdapter = adapter;
            if (mListView != null) {
                mListView.setAdapter(adapter);
            }
        }
    }

    public class SendBirdMessagingAdapter extends BaseAdapter {
        private static final int TYPE_UNSUPPORTED = 0;
        private static final int TYPE_MESSAGE = 1;
        private static final int TYPE_SYSTEM_MESSAGE = 2;
        private static final int TYPE_FILELINK = 3;
        private static final int TYPE_BROADCAST_MESSAGE = 4;
        private static final int TYPE_TYPING_INDICATOR = 5;

        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ArrayList<Object> mItemList;

        private Hashtable<String, Long> mReadStatus;
        private Hashtable<String, Long> mTypeStatus;
        private List<MessagingChannel.Member> mMembers;
        private long mMaxMessageTimestamp = Long.MIN_VALUE;
        private long mMinMessageTimestamp = Long.MAX_VALUE;

        public SendBirdMessagingAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItemList = new ArrayList<Object>();
            mReadStatus = new Hashtable<String, Long>();
            mTypeStatus = new Hashtable<String, Long>();
        }

        @Override
        public int getCount() {
            return mItemList.size() + ((mTypeStatus.size() <= 0) ? 0 : 1);
        }

        @Override
        public Object getItem(int position) {
            if (position >= mItemList.size()) {
                ArrayList<String> names = new ArrayList<String>();
                for (MessagingChannel.Member member : mMembers) {
                    if (mTypeStatus.containsKey(member.getId())) {
                        names.add(member.getName());
                    }
                }

                return names;
            }
            return mItemList.get(position);
        }

        public void clear() {
            mMaxMessageTimestamp = Long.MIN_VALUE;
            mMinMessageTimestamp = Long.MAX_VALUE;

            mReadStatus.clear();
            mTypeStatus.clear();
            mItemList.clear();
        }

        public void resetReadStatus(Hashtable<String, Long> readStatus) {
            mReadStatus = readStatus;
        }

        public void setReadStatus(String userId, long timestamp) {
            if (mReadStatus.get(userId) == null || mReadStatus.get(userId) < timestamp) {
                mReadStatus.put(userId, timestamp);
            }
        }

        public void setTypeStatus(String userId, long timestamp) {
            if (userId.equals(SendBird.getUserId())) {
                return;
            }

            if (timestamp <= 0) {
                mTypeStatus.remove(userId);
            } else {
                mTypeStatus.put(userId, timestamp);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addMessageModel(MessageModel messageModel) {
            if (messageModel.isPast()) {
                mItemList.add(0, messageModel);
            } else {
                mItemList.add(messageModel);
            }
            updateMessageTimestamp(messageModel);
        }

        private void updateMessageTimestamp(MessageModel model) {
            mMaxMessageTimestamp = mMaxMessageTimestamp < model.getTimestamp() ? model.getTimestamp() : mMaxMessageTimestamp;
            mMinMessageTimestamp = mMinMessageTimestamp > model.getTimestamp() ? model.getTimestamp() : mMinMessageTimestamp;
        }

        public long getMaxMessageTimestamp() {
            return mMaxMessageTimestamp == Long.MIN_VALUE ? Long.MAX_VALUE : mMaxMessageTimestamp;
        }

        public long getMinMessageTimestamp() {
            return mMinMessageTimestamp == Long.MAX_VALUE ? Long.MIN_VALUE : mMinMessageTimestamp;
        }

        public void setMembers(List<MessagingChannel.Member> members) {
            mMembers = members;
        }


        @Override
        public int getItemViewType(int position) {
            if (position >= mItemList.size()) {
                return TYPE_TYPING_INDICATOR;
            }

            Object item = mItemList.get(position);
            if (item instanceof Message) {
                return TYPE_MESSAGE;
            } else if (item instanceof FileLink) {
                return TYPE_FILELINK;
            } else if (item instanceof SystemMessage) {
                return TYPE_SYSTEM_MESSAGE;
            } else if (item instanceof BroadcastMessage) {
                return TYPE_BROADCAST_MESSAGE;
            }

            return TYPE_UNSUPPORTED;
        }

        @Override
        public int getViewTypeCount() {
            return 6;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            final Object item = getItem(position);

            if (convertView == null || ((ViewHolder) convertView.getTag()).getViewType() != getItemViewType(position)) {
                viewHolder = new ViewHolder();
                viewHolder.setViewType(getItemViewType(position));

                switch (getItemViewType(position)) {
                    case TYPE_UNSUPPORTED:
                        convertView = new View(mInflater.getContext());
                        convertView.setTag(viewHolder);
                        break;
                    case TYPE_MESSAGE: {
                        TextView tv;
                        ImageView iv;
                        View v;

                        convertView = mInflater.inflate(R.layout.sendbird_view_messaging_message, parent, false);

                        v = convertView.findViewById(R.id.left_container);
                        viewHolder.setView("left_container", v);
                        iv = (ImageView) convertView.findViewById(R.id.img_left_thumbnail);
                        viewHolder.setView("left_thumbnail", iv);
                        tv = (TextView) convertView.findViewById(R.id.txt_left);
                        viewHolder.setView("left_message", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_left_name);
                        viewHolder.setView("left_name", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_left_time);
                        viewHolder.setView("left_time", tv);

                        v = convertView.findViewById(R.id.right_container);
                        viewHolder.setView("right_container", v);
//                        iv = (ImageView) convertView.findViewById(R.id.img_right_thumbnail);
//                        viewHolder.setView("right_thumbnail", iv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right);
                        viewHolder.setView("right_message", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right_name);
                        viewHolder.setView("right_name", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right_time);
                        viewHolder.setView("right_time", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right_status);
                        viewHolder.setView("right_status", tv);

                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_SYSTEM_MESSAGE: {
                        convertView = mInflater.inflate(R.layout.sendbird_view_system_message, parent, false);
                        viewHolder.setView("message", convertView.findViewById(R.id.txt_message));
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_BROADCAST_MESSAGE: {
                        convertView = mInflater.inflate(R.layout.sendbird_view_system_message, parent, false);
                        viewHolder.setView("message", convertView.findViewById(R.id.txt_message));
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_FILELINK: {
                        TextView tv;
                        ImageView iv;
                        View v;

                        convertView = mInflater.inflate(R.layout.sendbird_view_messaging_filelink, parent, false);

                        v = convertView.findViewById(R.id.left_container);
                        viewHolder.setView("left_container", v);
                        iv = (ImageView) convertView.findViewById(R.id.img_left_thumbnail);
                        viewHolder.setView("left_thumbnail", iv);
                        iv = (ImageView) convertView.findViewById(R.id.img_left);
                        viewHolder.setView("left_image", iv);
                        tv = (TextView) convertView.findViewById(R.id.txt_left_name);
                        viewHolder.setView("left_name", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_left_time);
                        viewHolder.setView("left_time", tv);

                        v = convertView.findViewById(R.id.right_container);
                        viewHolder.setView("right_container", v);
//                        iv = (ImageView) convertView.findViewById(R.id.img_right_thumbnail);
//                        viewHolder.setView("right_thumbnail", iv);
                        iv = (ImageView) convertView.findViewById(R.id.img_right);
                        viewHolder.setView("right_image", iv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right_name);
                        viewHolder.setView("right_name", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right_time);
                        viewHolder.setView("right_time", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right_status);
                        viewHolder.setView("right_status", tv);

                        convertView.setTag(viewHolder);

                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(mContext)
                                        .setTitle("SendBird")
                                        .setMessage("Do you want to download this file?")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
//                                                try {
//                                                    downloadUrl((FileLink) item, mContext);
//                                                } catch (IOException e) {
//                                                    e.printStackTrace();
//                                                }
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .create()
                                        .show();
                            }
                        });
                        break;
                    }
                    case TYPE_TYPING_INDICATOR: {
                        convertView = mInflater.inflate(R.layout.sendbird_view_typing_indicator, parent, false);
                        viewHolder.setView("message", convertView.findViewById(R.id.txt_message));
                        convertView.setTag(viewHolder);
                        break;
                    }
                }
            }


            viewHolder = (ViewHolder) convertView.getTag();
            switch (getItemViewType(position)) {
                case TYPE_UNSUPPORTED:
                    break;
                case TYPE_MESSAGE:
                    final Message message = (Message) item;
                    if (Constants.CHAT_LOCK.equals(message.getMessage()) || Constants.CHAT_UNLOCK.equals(message.getMessage())) {
                        viewHolder.getView("left_container", View.class).setVisibility(View.GONE);
                        viewHolder.getView("right_container", View.class).setVisibility(View.GONE);

                        break;
                    }
                    if (message.getSenderId().equals(SendBird.getUserId())) {
                        viewHolder.getView("left_container", View.class).setVisibility(View.GONE);
                        viewHolder.getView("right_container", View.class).setVisibility(View.VISIBLE);

//                        displayUrlImage(viewHolder.getView("right_thumbnail", ImageView.class), message.getSenderImageUrl(), true);
                        viewHolder.getView("right_name", TextView.class).setText(message.getSenderName());
                        viewHolder.getView("right_message", TextView.class).setText(message.getMessage());
                        viewHolder.getView("right_time", TextView.class).setText(getDisplayDateTime(mContext, message.getTimestamp()));

                        int readCount = 0;
                        for (String key : mReadStatus.keySet()) {
                            if (key.equals(message.getSenderId())) {
                                readCount += 1;
                                continue;
                            }

                            if (mReadStatus.get(key) >= message.getTimestamp()) {
                                readCount += 1;
                            }
                        }
                        if (readCount < mReadStatus.size()) {
                            if (mReadStatus.size() - readCount > 1) {
                                viewHolder.getView("right_status", TextView.class).setText(getString(R.string.unread) + " " + (mReadStatus.size() - readCount));
                            } else {
                                viewHolder.getView("right_status", TextView.class).setText(getString(R.string.unread));
                            }
                        } else {
                            viewHolder.getView("right_status", TextView.class).setText("");
                        }
                    } else {
                        viewHolder.getView("left_container", View.class).setVisibility(View.VISIBLE);
                        viewHolder.getView("right_container", View.class).setVisibility(View.GONE);

                        viewHolder.getView("left_thumbnail", CircularNetworkImageView.class).setImageUrl(message.getSenderImageUrl(), RequestManager.getImageLoader());
                        viewHolder.getView("left_thumbnail", CircularNetworkImageView.class).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String memberId = message.getSenderId();
                                if (StringUtil.isEmpty(memberId)) {
                                    return;
                                }
                                final Intent intent = new Intent(SendBirdMessagingActivity.this, UserDetailActivity.class);
                                Bundle extras = new Bundle();
                                User user = new User();
                                user.setId(memberId);
                                extras.putSerializable("user", user);
                                extras.putBoolean("fromChat", true);
                                intent.putExtras(extras);
                                startActivity(intent);
                            }
                        });
                        viewHolder.getView("left_name", TextView.class).setText(message.getSenderName());
                        viewHolder.getView("left_message", TextView.class).setText(message.getMessage());
                        viewHolder.getView("left_time", TextView.class).setText(getDisplayDateTime(mContext, message.getTimestamp()));
                    }
                    break;
                case TYPE_SYSTEM_MESSAGE:
                    SystemMessage systemMessage = (SystemMessage) item;
                    viewHolder.getView("message", TextView.class).setText(Html.fromHtml(systemMessage.getMessage()));
                    break;
                case TYPE_BROADCAST_MESSAGE:
                    BroadcastMessage broadcastMessage = (BroadcastMessage) item;
                    viewHolder.getView("message", TextView.class).setText(Html.fromHtml(broadcastMessage.getMessage()));
                    break;
                case TYPE_FILELINK:
                    FileLink fileLink = (FileLink) item;

                    if (fileLink.getSenderId().equals(SendBird.getUserId())) {
                        viewHolder.getView("left_container", View.class).setVisibility(View.GONE);
                        viewHolder.getView("right_container", View.class).setVisibility(View.VISIBLE);

//                        displayUrlImage(viewHolder.getView("right_thumbnail", ImageView.class), fileLink.getSenderImageUrl(), true);
                        viewHolder.getView("right_name", TextView.class).setText(fileLink.getSenderName());
                        if (fileLink.getFileInfo().getType().toLowerCase().startsWith("image")) {
//                            displayUrlImage(viewHolder.getView("right_image", ImageView.class), fileLink.getFileInfo().getUrl());
                        } else {
                            viewHolder.getView("right_image", ImageView.class).setImageResource(R.drawable.sendbird_icon_file);
                        }
                        viewHolder.getView("right_time", TextView.class).setText(getDisplayDateTime(mContext, fileLink.getTimestamp()));
                        int readCount = 0;
                        for (String key : mReadStatus.keySet()) {
                            if (key.equals(fileLink.getSenderId())) {
                                continue;
                            }

                            if (mReadStatus.get(key) < fileLink.getTimestamp()) {
                                readCount += 1;
                            }
                        }
                        if (readCount < mReadStatus.size() - 1) {
                            viewHolder.getView("right_status", TextView.class).setText(getString(R.string.unread));
                        } else {
                            viewHolder.getView("right_status", TextView.class).setText("");
                        }
                    } else {
                        viewHolder.getView("left_container", View.class).setVisibility(View.VISIBLE);
                        viewHolder.getView("right_container", View.class).setVisibility(View.GONE);

//                        displayUrlImage(viewHolder.getView("left_thumbnail", ImageView.class), fileLink.getSenderImageUrl(), true);
                        viewHolder.getView("left_name", TextView.class).setText(fileLink.getSenderName());
                        if (fileLink.getFileInfo().getType().toLowerCase().startsWith("image")) {
//                            displayUrlImage(viewHolder.getView("left_image", ImageView.class), fileLink.getFileInfo().getUrl());
                        } else {
                            viewHolder.getView("left_image", ImageView.class).setImageResource(R.drawable.sendbird_icon_file);
                        }
                        viewHolder.getView("left_time", TextView.class).setText(getDisplayDateTime(mContext, fileLink.getTimestamp()));
                    }
                    break;

                case TYPE_TYPING_INDICATOR: {
                    int itemCount = ((List) item).size();
                    String typeMsg = ((List) item).get(0)
                            + ((itemCount > 1) ? " +" + (itemCount - 1) : "")
                            + ((itemCount > 1) ? " are " : " is ")
                            + "typing...";
                    viewHolder.getView("message", TextView.class).setText(typeMsg);
                    break;
                }
            }

            return convertView;
        }

        public boolean checkTypeStatus() {
            /**
             * Clear an old type status.
             */
            for (String key : mTypeStatus.keySet()) {
                Long ts = mTypeStatus.get(key);
                if (System.currentTimeMillis() - ts > 10 * 1000L) {
                    mTypeStatus.remove(key);
                    return true;
                }
            }

            return false;
        }


        private class ViewHolder {
            private Hashtable<String, View> holder = new Hashtable<String, View>();
            private int type;

            public int getViewType() {
                return this.type;
            }

            public void setViewType(int type) {
                this.type = type;
            }

            public void setView(String k, View v) {
                holder.put(k, v);
            }

            public View getView(String k) {
                return holder.get(k);
            }

            public <T> T getView(String k, Class<T> type) {
                return type.cast(getView(k));
            }
        }
    }

    private static String getDisplayDateTime(Context context, long milli) {
        Date date = new Date(milli);

        if (System.currentTimeMillis() - milli < 60 * 60 * 24 * 1000l) {
            return DateFormat.getTimeFormat(context).format(date);
        }

        return DateFormat.getDateFormat(context).format(date) + " " + DateFormat.getTimeFormat(context).format(date);
    }

    public static class Helper {
        public static String generateDeviceUUID(Context context) {
            String serial = android.os.Build.SERIAL;
            String androidID = Settings.Secure.ANDROID_ID;
            String deviceUUID = serial + androidID;

        /*
         * SHA-1
         */
            MessageDigest digest;
            byte[] result;
            try {
                digest = MessageDigest.getInstance("SHA-1");
                result = digest.digest(deviceUUID.getBytes("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02X", b));
            }

            return sb.toString();
        }

        public static void hideKeyboard(Activity activity) {
            if (activity == null || activity.getCurrentFocus() == null) {
                return;
            }

            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }

        public static String readableFileSize(long size) {
            if (size <= 0) return "0KB";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

    }

}
