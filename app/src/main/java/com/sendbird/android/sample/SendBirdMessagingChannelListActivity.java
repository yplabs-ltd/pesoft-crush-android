package com.sendbird.android.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdEventHandler;
import com.sendbird.android.SendBirdNotificationHandler;
import com.sendbird.android.model.BroadcastMessage;
import com.sendbird.android.model.Channel;
import com.sendbird.android.model.FileLink;
import com.sendbird.android.model.Mention;
import com.sendbird.android.model.Message;
import com.sendbird.android.model.MessagingChannel;
import com.sendbird.android.model.ReadStatus;
import com.sendbird.android.model.SystemMessage;
import com.sendbird.android.model.TypeStatus;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.HappyCallUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.StringUtil;
import net.pesofts.crush.activity.NewsMoreListActivity;
import net.pesofts.crush.activity.UserDetailActivity;
import net.pesofts.crush.fragment.AlertDialogFragment;
import net.pesofts.crush.fragment.BaseDialogFragment;
import net.pesofts.crush.fragment.OneButtonAlertDialogFragment;
import net.pesofts.crush.model.NewsSectionType;
import net.pesofts.crush.model.Result;
import net.pesofts.crush.model.UpdateInfo;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.network.RequestManager;
import net.pesofts.crush.widget.CircularNetworkImageView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


public class SendBirdMessagingChannelListActivity extends FragmentActivity {
    private SendBirdMessagingChannelListFragment mSendBirdMessagingChannelListFragment;
    private SendBirdMessagingChannelAdapter mSendBirdMessagingChannelAdapter;

    private String uuid;
    private String nickname;
    private String profileUrl;

    private View likeInfoCount;
    private boolean isFirst = true;

    public static Bundle makeSendBirdArgs(String appKey, String uuid, String nickname, String profileUrl) {
        Bundle args = new Bundle();
        args.putString("appKey", appKey);
        args.putString("uuid", uuid);
        args.putString("nickname", nickname);
        args.putString("profileUrl", profileUrl);
        return args;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendbird_messaging_channel_list);

        CrushApplication.getInstance().loggingView(getString(R.string.ga_chat_list));

        ImageButton mBtnClose = (ImageButton) findViewById(R.id.close_button);
        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        likeInfoCount = findViewById(R.id.like_info_layout);
        likeInfoCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(SendBirdMessagingChannelListActivity.this, NewsMoreListActivity.class);
                Bundle extras = new Bundle();
                extras.putSerializable("newsSectionType", NewsSectionType.LikeMe);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        initLikeCount();
        initFragment();
        initSendBird(getIntent().getExtras());
    }

    private void initLikeCount() {
        final TextView likeCountText = (TextView) findViewById(R.id.like_count_text);

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.CARD_UPDATE_INFO_URL, UpdateInfo.class, null, getApplicationContext());
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<UpdateInfo>() {
            @Override
            public void onResponse(UpdateInfo updateInfo) {
                LogUtil.d("updateInfo : " + updateInfo);
                likeCountText.setText(" " + updateInfo.getLikeMeCount() + "개");

                if (updateInfo.getLikeMeCount() > 0) {
                    likeInfoCount.setEnabled(true);
                } else {
                    likeInfoCount.setEnabled(false);
                }
            }
        }).execute();
    }

    private void initSendBird(Bundle extras) {
//        if (extras != null) {
//            String appKey = extras.getString("appKey");
//            uuid = extras.getString("uuid");
//            nickname = extras.getString("nickname");
//            profileUrl = extras.getString("profileUrl");

//            SendBird.init(appKey);
//            SendBird.login(uuid, nickname, profileUrl);

        SendBird.registerNotificationHandler(new SendBirdNotificationHandler() {
            @Override
            public void onMessagingChannelUpdated(MessagingChannel messagingChannel) {
                mSendBirdMessagingChannelAdapter.replace(messagingChannel);
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

            }

            @Override
            public void onSystemMessageReceived(SystemMessage systemMessage) {

            }

            @Override
            public void onBroadcastMessageReceived(BroadcastMessage broadcastMessage) {

            }

            @Override
            public void onFileReceived(FileLink fileLink) {

            }

            @Override
            public void onReadReceived(ReadStatus readStatus) {

            }

            @Override
            public void onTypeStartReceived(TypeStatus typeStatus) {

            }

            @Override
            public void onTypeEndReceived(TypeStatus typeStatus) {

            }

            @Override
            public void onAllDataReceived(SendBird.SendBirdDataType sendbirdDataType, int i) {

            }

            @Override
            public void onMessageDelivery(boolean b, String s, String s2, String s3) {

            }

            @Override
            public void onMessagingStarted(MessagingChannel messagingChannel) {

            }

            @Override
            public void onMessagingUpdated(MessagingChannel messagingChannel) {

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

//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SendBird.join("");
        SendBird.connect();

        if (!isFirst && mSendBirdMessagingChannelListFragment != null) {
            mSendBirdMessagingChannelListFragment.refresh();
            initSendBird(getIntent().getExtras());
        }

        isFirst = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        SendBird.disconnect();
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void initFragment() {
        mSendBirdMessagingChannelAdapter = new SendBirdMessagingChannelAdapter(this);

        mSendBirdMessagingChannelListFragment = new SendBirdMessagingChannelListFragment();
        mSendBirdMessagingChannelListFragment.setSendBirdMessagingChannelAdapter(mSendBirdMessagingChannelAdapter);
        mSendBirdMessagingChannelListFragment.setSendBirdMessagingChannelListHandler(new SendBirdMessagingChannelListFragment.SendBirdMessagingChannelListHandler() {
            @Override
            public void onMessagingChannelSelected(final MessagingChannel messagingChannel) {

                if (messagingChannel.hasLastMessage() && Constants.CHAT_LOCK.equals(messagingChannel.getLastMessage().getMessage())) {

                    if (CommonUtil.haveEnoughPoint(SendBirdMessagingChannelListActivity.this, Constants.BUCHI_COUNT_FOR_UNLOCK_CHAT)) {
                        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.unlock_chat, getDisplayMemberNames(messagingChannel.getMembers())));
                        bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, getString(R.string.unlock_chat_info));
                        bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, getString(R.string.needed_buchi_count, Constants.BUCHI_COUNT_FOR_UNLOCK_CHAT));
                        alertDialogFragment.setArguments(bundle);

                        alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                            @Override
                            public void onDialogConfirmed() {
                                String memberId = getMemberId(messagingChannel.getMembers());
                                if (StringUtil.isEmpty(memberId)) {
                                    // TODO : 혼자 있을때 처리?(확인필요)
                                    return;
                                }
                                List<NameValuePair> paramInfo = new ArrayList<>();
                                paramInfo.add(new BasicNameValuePair("userid", memberId));
                                HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.CHAT_UNLOCK_URL, Result.class, paramInfo, HttpMethod.POST, getApplicationContext());
                                RequestFactory requestFactory = new RequestFactory();
                                requestFactory.setProgressHandler(new ProgressHandler(SendBirdMessagingChannelListActivity.this, false));
                                requestFactory.create(httpRequestVO, new HttpResponseCallback<Result>() {
                                    @Override
                                    public void onResponse(Result result) {
                                        if ("OK".equals(result.getCode())) {
                                            CommonUtil.syncPoint(getApplicationContext());
                                            moveToMessageActivity(messagingChannel.getUrl());
                                        } else if ("NoDataToUnblock".equals(result.getCode())) {
                                            // TODO : 확인필요
                                            mSendBirdMessagingChannelAdapter.remove(mSendBirdMessagingChannelAdapter.getList().indexOf(messagingChannel));
                                            mSendBirdMessagingChannelAdapter.notifyDataSetChanged();
                                            SendBird.endMessaging(messagingChannel.getUrl());

                                            OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
                                            Bundle bundle = new Bundle();
                                            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, result.getDescription());
                                            oneButtonAlertDialogFragment.setArguments(bundle);
                                            oneButtonAlertDialogFragment.show(getSupportFragmentManager(), "oneButtonAlertDialogFragment");
                                        }
                                    }
                                }).execute();
                            }
                        });

                        alertDialogFragment.show(getSupportFragmentManager(), "alertDialogFragment");
                    }

                } else {
                    moveToMessageActivity(messagingChannel.getUrl());
                }


            }

        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdMessagingChannelListFragment)
                .commit();
    }

    private String getMemberId(List<MessagingChannel.Member> members) {
        if (members.size() < 2) {
            return null;
        }

        String id = "";
        for (MessagingChannel.Member member : members) {
            if (member.getId().equals(SendBird.getUserId())) {
                continue;
            }

            id = member.getId();
            break;
        }
        return id;
    }

    private void moveToMessageActivity(String messagingChannelUrl) {
        Intent intent = new Intent(SendBirdMessagingChannelListActivity.this, SendBirdMessagingActivity.class);
        //키값 이해하려고하지말고 변경되면 새로 만드는것도 한 방법임... -_-;
        String happy = new String(HappyCallUtil.save(Constants.a, HappyCallUtil.call(Constants.key2, Constants.key1, Constants.key0, Constants.key3).getBytes()));
        Bundle args = SendBirdMessagingActivity.makeMessagingJoinArgs(happy, uuid, nickname, profileUrl, messagingChannelUrl);
        intent.putExtras(args);

        startActivity(intent);
    }

    public class SendBirdMessagingChannelAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ArrayList<MessagingChannel> mItemList;
        private long mCurrentChannelId;

        public List<MessagingChannel> sortMessagingChannels(List<MessagingChannel> messagingChannels) {
            Collections.sort(messagingChannels, new Comparator<MessagingChannel>() {
                @Override
                public int compare(MessagingChannel lhs, MessagingChannel rhs) {
                    long lhsv = lhs.getLastMessageTimestamp();
                    long rhsv = rhs.getLastMessageTimestamp();
                    return (lhsv == rhsv) ? 0 : (lhsv < rhsv) ? 1 : -1;
                }
            });

            return messagingChannels;
        }


        public SendBirdMessagingChannelAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItemList = new ArrayList<MessagingChannel>();
        }

        public void setCurrentChannelId(long channelId) {
            mCurrentChannelId = channelId;
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public MessagingChannel getItem(int position) {
            return mItemList.get(position);
        }

        public ArrayList<MessagingChannel> getList() {
            return mItemList;
        }

        public void clear() {
            mItemList.clear();
        }

        public MessagingChannel remove(int index) {
            return mItemList.remove(index);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void add(MessagingChannel channel) {
            mItemList.add(channel);
            notifyDataSetChanged();
        }

        public void addAll(List<MessagingChannel> channels) {
            mItemList.addAll(channels);
            notifyDataSetChanged();
        }

        public void replace(MessagingChannel newChannel) {
            for (MessagingChannel oldChannel : mItemList) {
                if (oldChannel.getId() == newChannel.getId()) {
                    mItemList.remove(oldChannel);
                    break;
                }
            }

            mItemList.add(0, newChannel);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.sendbird_view_messaging_channel, parent, false);
                viewHolder.setView("img_thumbnail", convertView.findViewById(R.id.img_thumbnail));
                viewHolder.setView("txt_topic", convertView.findViewById(R.id.txt_topic));
                viewHolder.setView("txt_unread_count", convertView.findViewById(R.id.txt_unread_count));
                viewHolder.setView("txt_date", convertView.findViewById(R.id.txt_date));
                viewHolder.setView("txt_desc", convertView.findViewById(R.id.txt_desc));

                convertView.setTag(viewHolder);
            }

            final MessagingChannel item = getItem(position);
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.getView("img_thumbnail", CircularNetworkImageView.class).setImageUrl(getDisplayCoverImageUrl(item.getMembers()), RequestManager.getImageLoader());

            viewHolder.getView("img_thumbnail", CircularNetworkImageView.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String memberId = getMemberId(item.getMembers());
                    if (StringUtil.isEmpty(memberId)) {
                        return;
                    }
                    final Intent intent = new Intent(SendBirdMessagingChannelListActivity.this, UserDetailActivity.class);
                    Bundle extras = new Bundle();
                    User user = new User();
                    user.setId(memberId);
                    extras.putSerializable("user", user);
                    extras.putBoolean("fromChat", true);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });

            viewHolder.getView("txt_topic", TextView.class).setText(getDisplayMemberNames(item.getMembers()));

            if (item.hasLastMessage() && Constants.CHAT_LOCK.equals(item.getLastMessage().getMessage())) {
                viewHolder.getView("txt_unread_count", TextView.class).setVisibility(View.VISIBLE);
                viewHolder.getView("txt_unread_count", TextView.class).setText("N");
            } else if (item.getUnreadMessageCount() > 0) {
                viewHolder.getView("txt_unread_count", TextView.class).setVisibility(View.VISIBLE);
                viewHolder.getView("txt_unread_count", TextView.class).setText("" + item.getUnreadMessageCount());
            } else {
                viewHolder.getView("txt_unread_count", TextView.class).setVisibility(View.INVISIBLE);
            }

            if (item.hasLastMessage()) {
                Message message = item.getLastMessage();
                viewHolder.getView("txt_date", TextView.class).setText(getDisplayTimeOrDate(mContext, message.getTimestamp()));
                if (Constants.CHAT_LOCK.equals(message.getMessage())) {
                    viewHolder.getView("txt_desc", TextView.class).setText(R.string.lock_chat);
                } else if (Constants.CHAT_UNLOCK.equals(message.getMessage())) {
                    viewHolder.getView("txt_desc", TextView.class).setText(R.string.start_chat);
                } else {
                    viewHolder.getView("txt_desc", TextView.class).setText("" + message.getMessage());
                }
            } else {
                viewHolder.getView("txt_date", TextView.class).setText("");
                viewHolder.getView("txt_desc", TextView.class).setText("");
            }

            return convertView;
        }

        private class ViewHolder {
            private Hashtable<String, View> holder = new Hashtable<String, View>();

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

    private static String getDisplayCoverImageUrl(List<MessagingChannel.Member> members) {
        for (MessagingChannel.Member member : members) {
            if (member.getId().equals(SendBird.getUserId())) {
                continue;
            }

            return member.getImageUrl();
        }

        return "";
    }

    private static String getDisplayMemberNames(List<MessagingChannel.Member> members) {
        if (members.size() < 2) {
            return "No Members";
        }

        StringBuffer names = new StringBuffer();
        for (MessagingChannel.Member member : members) {
            if (member.getId().equals(SendBird.getUserId())) {
                continue;
            }

            names.append(", " + member.getName());
        }
        return names.delete(0, 2).toString();
    }

    private static String getDisplayTimeOrDate(Context context, long milli) {
        Date date = new Date(milli);

        if (System.currentTimeMillis() - milli > 60 * 60 * 24 * 1000l) {
            return DateFormat.getDateFormat(context).format(date);
        } else {
            return DateFormat.getTimeFormat(context).format(date);
        }
    }

}
