package net.pesofts.crush.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sendbird.android.MessagingChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdNotificationHandler;
import com.sendbird.android.model.Mention;
import com.sendbird.android.model.MessagingChannel;
import com.sendbird.android.sample.SendBirdMessagingChannelListActivity;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.HappyCallUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.Util.StringUtil;
import net.pesofts.crush.fragment.AlertDialogFragment;
import net.pesofts.crush.fragment.AttendanceDialogFragment;
import net.pesofts.crush.fragment.BaseDialogFragment;
import net.pesofts.crush.fragment.DrawerFragment;
import net.pesofts.crush.fragment.EvaluateFragment;
import net.pesofts.crush.fragment.NewsFragment;
import net.pesofts.crush.fragment.OneButtonAlertDialogFragment;
import net.pesofts.crush.fragment.StoryFragment;
import net.pesofts.crush.fragment.TodayFragment;
import net.pesofts.crush.model.SystemCheck;
import net.pesofts.crush.model.UpdateInfo;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.RequestFactory;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class MainActivity extends FragmentActivity {

    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.today_text)
    TextView todayTextView;
    @Bind(R.id.story_text)
    TextView storyTextView;
    @Bind(R.id.evaluate_text)
    TextView evaluateTextView;
    @Bind(R.id.evaluate_count_text)
    TextView evaluateCountTextView;
    @Bind(R.id.news_text)
    TextView newsTextView;
    @Bind(R.id.news_count_text)
    TextView newsCountTextView;
    @Bind(R.id.chat_button)
    ImageButton chatButton;

    private User user;

    private long lastBackPressedTime;
    private Snackbar exitSnackbar;
    private boolean isFirst = true;
    private boolean needRefresh = false;
    private long resumeTime = 0;

    private static boolean isActive;
    public static boolean isActive() {
        return isActive;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String type = intent.getStringExtra("type");
        if (!TextUtils.isEmpty(type)) {
            goToViewPager(type);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //키값 이해하려고하지말고 변경되면 새로 만드는것도 한 방법임... -_-;
        String happy = new String(HappyCallUtil.save(Constants.a, HappyCallUtil.call(Constants.key2, Constants.key1, Constants.key0, Constants.key3).getBytes()));

        EventBus.getDefault().register(this);

        isActive = true;

        User user = (User) getIntent().getSerializableExtra("user");
        this.user = user;

        final SystemCheck systemCheckResponse = (SystemCheck) getIntent().getSerializableExtra("systemCheckResponse");
        Integer lastNoticeId = SharedPrefHelper.getInstance(getApplicationContext()).getSharedPreferences(SharedPrefHelper.NOTICE_ID, -1);

        if (systemCheckResponse != null
                && (StringUtil.isNotEmpty(systemCheckResponse.getTitle()) || StringUtil.isNotEmpty(systemCheckResponse.getDescription()))
                && systemCheckResponse.getNoticeid() > lastNoticeId) {
            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(AlertDialogFragment.DIALOG_CONFIRM_NAME, getString(R.string.dont_read_again));
            bundle.putString(AlertDialogFragment.DIALOG_CANCEL_NAME, getString(R.string.close));
            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, systemCheckResponse.getTitle());
            bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, systemCheckResponse.getDescription());
            alertDialogFragment.setArguments(bundle);
            alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                @Override
                public void onDialogConfirmed() {
                    SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.NOTICE_ID, systemCheckResponse.getNoticeid());
                }
            });

            alertDialogFragment.show(getSupportFragmentManager(), "alertDialogFragment");
        }

        SendBird.init(happy);
        SendBird.login(user.getId(), user.getName(), user.getMainImageUrl());

        setSendBird();

        setupViewPager();

        // Fragments
        final DrawerFragment drawerFragment = DrawerFragment.newInstance(user);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_view, drawerFragment).commit();

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawerFragment != null) {
                    drawerFragment.onOpenDrawer();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        getCardUpdateInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        isActive = false;
    }

    private void getCardUpdateInfo() {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.CARD_UPDATE_INFO_URL, UpdateInfo.class, null, getApplicationContext());
        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<UpdateInfo>() {
            @Override
            public void onResponse(UpdateInfo updateInfo) {
                LogUtil.d("updateInfo : " + updateInfo);

                setEvaluateCount(updateInfo.getEvaluateCount());
                setNewsCount(updateInfo.getLikeMeCount());
            }
        }).execute();
    }

    private void setSendBird() {
        SendBird.registerNotificationHandler(new SendBirdNotificationHandler() {
            @Override
            public void onMessagingChannelUpdated(MessagingChannel messagingChannel) {
                chatButton.setImageResource(R.drawable.home_btn_chat_new_normal);
            }

            @Override
            public void onMentionUpdated(Mention mention) {
            }
        });

        setChatCount();
    }

    public void doAfterLoading() {
        if (user.getLoginPoint() > 0) {
//            AttendanceDialogFragment fragment = AttendanceDialogFragment.newInstance(5);
//            fragment.show(getSupportFragmentManager(), null);
            AlertDialogFragment oneButtonAlertDialogFragment = AlertDialogFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, getString(R.string.bonus_buchi));
            bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, getString(R.string.bonus_buchi_desc));
            bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, getString(R.string.bonus_buchi_alert, user.getLoginPoint()));
            bundle.putString(AlertDialogFragment.DIALOG_CONFIRM_NAME, "확인");
            bundle.putString(AlertDialogFragment.DIALOG_CANCEL_NAME, "리뷰쓰러가기");
            bundle.putInt(AlertDialogFragment.DIALOG_CANCEL_COLOR, 0xFFF2503B);
            oneButtonAlertDialogFragment.setArguments(bundle);
            oneButtonAlertDialogFragment.setCancelListener(new BaseDialogFragment.CancelListener() {
                @Override
                public void onDialogCancelled(DialogFragment dialog) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                    startActivity(intent);
                }
            });
            oneButtonAlertDialogFragment.show(getSupportFragmentManager(), "AlertDialogFragment");

            user.setLoginPoint(0);
        }
    }

    private void setChatCount() {
        MessagingChannelListQuery messagingChannelListQuery = SendBird.queryMessagingChannelList();
        messagingChannelListQuery.setLimit(30);

        if (messagingChannelListQuery.isLoading()) {
            return;
        }

        if (messagingChannelListQuery.hasNext()) {
            messagingChannelListQuery.next(new MessagingChannelListQuery.MessagingChannelListQueryResult() {
                @Override
                public void onResult(List<MessagingChannel> messagingChannels) {
                    boolean hasNewMessage = false;
                    if (messagingChannels != null && messagingChannels.size() > 0) {
                        for (MessagingChannel messagingChannel : messagingChannels) {
                            if (messagingChannel.getUnreadMessageCount() > 0) {
                                hasNewMessage = true;
                                break;
                            } else if (messagingChannel.hasLastMessage() && Constants.CHAT_LOCK.equals(messagingChannel.getLastMessage().getMessage())) {
                                hasNewMessage = true;
                                break;
                            }
                        }
                        if (hasNewMessage) {
                            chatButton.setImageResource(R.drawable.home_btn_chat_new_normal);
                        } else {
                            chatButton.setImageResource(R.drawable.home_btn_chat_normal);
                        }
                    } else {
                        chatButton.setImageResource(R.drawable.home_btn_chat_disabled);
                    }
                }

                @Override
                public void onError(int i) {
                }
            });
        }
    }

    public void setEvaluateCount(int count) {
        if (count > 0) {
            evaluateCountTextView.setText(Integer.toString(count));
            evaluateCountTextView.setVisibility(View.VISIBLE);
        } else {
            evaluateCountTextView.setVisibility(View.GONE);
        }
    }

    public void setNewsCount(int count) {
        if (count > 0) {
            newsCountTextView.setText(Integer.toString(count));
            newsCountTextView.setVisibility(View.VISIBLE);
        } else {
            newsCountTextView.setVisibility(View.GONE);
        }
    }

    private void setupViewPager() {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                              @Override
                                              public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                              }

                                              @Override
                                              public void onPageSelected(int position) {
                                                  int dimColor = ContextCompat.getColor(getApplicationContext(), R.color.dimmed_text_color);
                                                  int selectedColor = ContextCompat.getColor(getApplicationContext(), R.color.main_text_color);

                                                  if (position == 0) {
                                                      evaluateTextView.setTextColor(selectedColor);
                                                      todayTextView.setTextColor(dimColor);
                                                      storyTextView.setTextColor(dimColor);
                                                      newsTextView.setTextColor(dimColor);
//                                                      evaluateFragment.refresh();

                                                      CrushApplication.getInstance().loggingView(getString(R.string.ga_evaluate));
                                                  } else if (position == 1) {
                                                      evaluateTextView.setTextColor(dimColor);
                                                      todayTextView.setTextColor(selectedColor);
                                                      storyTextView.setTextColor(dimColor);
                                                      newsTextView.setTextColor(dimColor);
//                                                      todayFragment.refresh();

                                                      CrushApplication.getInstance().loggingView(getString(R.string.ga_today));
                                                  } else if (position == 2) {
                                                      evaluateTextView.setTextColor(dimColor);
                                                      todayTextView.setTextColor(dimColor);
                                                      storyTextView.setTextColor(selectedColor);
                                                      newsTextView.setTextColor(dimColor);
//                                                      storyFragment.refresh();

                                                      CrushApplication.getInstance().loggingView(getString(R.string.ga_story));
                                                  } else {
                                                      evaluateTextView.setTextColor(dimColor);
                                                      todayTextView.setTextColor(dimColor);
                                                      storyTextView.setTextColor(dimColor);
                                                      newsTextView.setTextColor(selectedColor);
//                                                      newsFragment.refresh();

                                                      CrushApplication.getInstance().loggingView(getString(R.string.ga_news));
                                                  }
                                              }

                                              @Override
                                              public void onPageScrollStateChanged(int state) {
                                              }
                                          }

        );

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);

        int page = 1;
        String type = getIntent().getStringExtra("type");
        if (type != null) {
            // likefavor
            // profile
            // voicereply
            // evaluate

            switch (type) {
                case "likefavor":
                    page = 3;
                    break;
                case "voicereply":
                    page = 2;
                    break;
                case "evaluate":
                    page = 0;
                    break;
                default:
                    page = 1;
                    break;
            }
        }

        viewPager.setCurrentItem(page);
        CrushApplication.getInstance().loggingView(getString(R.string.ga_today));

    }

    class Adapter extends FragmentPagerAdapter {

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return EvaluateFragment.newInstance();
                case 1:
                    final TodayFragment todayFragment = TodayFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", user);
                    todayFragment.setArguments(bundle);
                    return todayFragment;
                case 2:
                    return StoryFragment.newInstance(user);
                case 3:
                    return NewsFragment.newInstance();
            }
//            return mFragmentList.get(position);
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

    }

    @OnClick(R.id.today_text)
    void onCilckTodayText() {
        viewPager.setCurrentItem(1);
    }

    @OnClick(R.id.id_text_evaluate)
    void onCilckEvaluateText() {
        viewPager.setCurrentItem(0);
    }

    @OnClick(R.id.story_text)
    void onCilckStoryText() {
        viewPager.setCurrentItem(2);
    }

    @OnClick(R.id.id_text_news)
    void onCilckNewsText() {
        viewPager.setCurrentItem(3);
    }

    @OnClick(R.id.menu_button)
    void onCilckMenuButton() {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @OnClick(R.id.chat_button)
    void onCilckChatButton() {
        Intent intent = new Intent(this, SendBirdMessagingChannelListActivity.class);
//        Bundle args = SendBirdMessagingChannelListActivity.makeSendBirdArgs(Constants.SENDBIRD_APP_ID, user.getId(), user.getName(), user.getMainImageUrl());
//        intent.putExtras(args);

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        long currentTime = SystemClock.elapsedRealtime();

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (lastBackPressedTime <= 0 || (currentTime - lastBackPressedTime) > 2000L) {
            lastBackPressedTime = currentTime;
            if (exitSnackbar != null) {
                exitSnackbar.dismiss();
                exitSnackbar = null;
            }
            exitSnackbar = Snackbar.make(drawerLayout, getString(R.string.back_button_pressed_finish_notice), Snackbar.LENGTH_SHORT);
            View view = exitSnackbar.getView();
            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setTextColor(Color.WHITE);
            exitSnackbar.show();
        } else {
            super.onBackPressed();
        }
    }

    protected void onResume() {
        super.onResume();
        SendBird.join("");
        SendBird.connect();

        if (!isFirst) {
            setSendBird();
            CommonUtil.syncPointAndStatus(this);

            if (resumeTime > 0) {
                int diffTime = (int) (System.currentTimeMillis() - resumeTime) / 1000;
                int refreshTime = 1 * 600; // 10분
                if (refreshTime <= diffTime) {
                    EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.EXPIRED_REFRESH_TIME));
                    needRefresh = true;
                    resumeTime = System.currentTimeMillis();
                }
            }
            if (needRefresh) {
                getCardUpdateInfo();
            }
        } else {
            resumeTime = System.currentTimeMillis();
        }

        isFirst = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        SendBird.disconnect();
    }

    public void onEvent(RefreshEvent refreshEvent) {
        if (refreshEvent.action == RefreshEvent.Action.REPLY) {
            needRefresh = true;
        } else if (refreshEvent.action == RefreshEvent.Action.PUSH) {
            String type = refreshEvent.type;
//            Log.e("main", "onEvent : " + type);
            goToViewPager(type);
        }

        Log.d("onEvent", "MainActivity RefreshEvent : " + refreshEvent.action);
    }

    private void goToViewPager(String type) {
        // likefavor
        // profile
        // voicereply
        // evaluate
        if (type != null) {
            int page;
            switch (type) {
                case "likefavor":
                    page = 3;
                    break;
                case "voicereply":
                    page = 2;
                    break;
                case "evaluate":
                    page = 0;
                    break;
                default:
                    page = 1;
                    break;
            }

            viewPager.setCurrentItem(page, true);
        }
    }

    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    public int getCurrentItem() {
        if (viewPager != null) {
            return viewPager.getCurrentItem();
        } else {
            return -1;
        }
    }
}
