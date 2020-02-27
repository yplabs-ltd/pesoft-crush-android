package com.sendbird.android.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sendbird.android.MessagingChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.model.Channel;
import com.sendbird.android.model.MessagingChannel;

import net.pesofts.crush.Constants;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.fragment.AlertDialogFragment;
import net.pesofts.crush.fragment.BaseDialogFragment;
import net.pesofts.crush.model.Result;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SendBirdMessagingChannelListFragment extends Fragment {
    private SendBirdMessagingChannelListHandler mHandler;
    private ListView mListView;
    private SendBirdMessagingChannelListActivity.SendBirdMessagingChannelAdapter mAdapter;
    private Channel mCurrentChannel;
    private MessagingChannelListQuery mMessagingChannelListQuery;

    public static interface SendBirdMessagingChannelListHandler {
        public void onMessagingChannelSelected(MessagingChannel channel);
    }

    public void setSendBirdMessagingChannelAdapter(SendBirdMessagingChannelListActivity.SendBirdMessagingChannelAdapter adapter) {
        mAdapter = adapter;
        if (mListView != null) {
            mListView.setAdapter(adapter);
        }
    }

    public void setSendBirdMessagingChannelListHandler(SendBirdMessagingChannelListHandler handler) {
        mHandler = handler;
    }

    public SendBirdMessagingChannelListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sendbird_fragment_messaging_channel_list, container, false);
        initUIComponents(rootView);
        return rootView;

    }

    private void initUIComponents(View rootView) {
        mListView = (ListView) rootView.findViewById(R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessagingChannel channel = mAdapter.getItem(position);
                if (mHandler != null) {
                    mHandler.onMessagingChannelSelected(channel);
                }
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= (int) (totalItemCount * 0.8f)) {
                    loadMoreChannels();
                }
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final MessagingChannel channel = mAdapter.getItem(position);

                AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.leave_chat));
                alertDialogFragment.setArguments(bundle);
                alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                    @Override
                    public void onDialogConfirmed() {
                        List<NameValuePair> paramInfo = new ArrayList<>();
                        paramInfo.add(new BasicNameValuePair("userid", getMemberId(channel.getMembers())));
                        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.CHAT_LEAVE_URL, Result.class, paramInfo, HttpMethod.POST, getActivity());
                        RequestFactory requestFactory = new RequestFactory();
                        requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
                        requestFactory.create(httpRequestVO, new HttpResponseCallback<Result>() {
                            @Override
                            public void onResponse(Result result) {
                                mAdapter.remove(position);
                                mAdapter.notifyDataSetChanged();
                                SendBird.endMessaging(channel.getUrl());
                            }
                        }).execute();
                    }
                });
                alertDialogFragment.show(getFragmentManager(), "alertDialogFragment");

//                    new AlertDialog.Builder(getActivity())
//                            .setTitle("Leave")
//                            .setMessage("Do you want to leave this channel?")
//                            .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    mAdapter.remove(position);
//                                    mAdapter.notifyDataSetChanged();
//                                    SendBird.endMessaging(channel.getUrl());
//                                }
//                            })
//                            .setNeutralButton("Hide", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    mAdapter.remove(position);
//                                    mAdapter.notifyDataSetChanged();
//                                    SendBird.hideMessaging(channel.getUrl());
//                                }
//                            })
//                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                }
//                            }).create().show();
                return true;
            }
        });
        mListView.setAdapter(mAdapter);
    }

    private void loadMoreChannels() {
        if (mMessagingChannelListQuery == null) {
            mMessagingChannelListQuery = SendBird.queryMessagingChannelList();
            mMessagingChannelListQuery.setLimit(30);
        }

        if (mMessagingChannelListQuery.isLoading()) {
            return;
        }

        if (mMessagingChannelListQuery.hasNext()) {
            mMessagingChannelListQuery.next(new MessagingChannelListQuery.MessagingChannelListQueryResult() {
                @Override
                public void onResult(List<MessagingChannel> messagingChannels) {
                    mAdapter.addAll(messagingChannels);
                }

                @Override
                public void onError(int i) {
                }
            });
        }
    }

    public void refresh() {
        mAdapter.clear();
        mMessagingChannelListQuery = null;
        loadMoreChannels();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mCurrentChannel = SendBird.getCurrentChannel();
            mAdapter.setCurrentChannelId(mCurrentChannel.getId());
        } catch (IOException e) {
        }

        mAdapter.notifyDataSetChanged();
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

}