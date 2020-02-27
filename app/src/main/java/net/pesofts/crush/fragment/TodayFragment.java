package net.pesofts.crush.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.DateUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.activity.MainActivity;
import net.pesofts.crush.adapter.TodayAdapter;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class TodayFragment extends Fragment {

    @Bind(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.vertical_line)
    View verticalLine;
    @Bind(R.id.more_button)
    View moreButtonLayout;
    @Bind(R.id.empty_list_desc)
    TextView emptyListDesc;
    @Bind(R.id.move_fill_profile_button)
    TextView moveFillProfileButton;
    @Bind(R.id.empty_list_layout)
    RelativeLayout emptyListLayout;
    @Bind(R.id.id_text_title)
    TextView mTextTitle;

    private List<User> todayCardList;
    private TodayAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean needRefresh = false;
    private boolean isActive = true;
    private boolean isEmptyToday = false;
    private User user;

    public static TodayFragment newInstance() {
        Bundle args = new Bundle();

        TodayFragment fragment = new TodayFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainLayout = inflater.inflate(
                R.layout.fragment_today, container, false);

        ButterKnife.bind(this, mainLayout);

        EventBus.getDefault().register(this);

        linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    visibleByAnimationMoreLayout();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy != 0) {
                    goneByAnimationMoreLayout();
                }
            }
        });

        user = (User) getArguments().getSerializable("user");

        return mainLayout;
    }

    private Map taskMap = new HashMap<>();
    private Map timerMap = new HashMap<>();

    private void startCountWaitTime(final TextView textView, final long expiredDttm) {
        if (DateUtil.isPastTime(expiredDttm)) {
            stopCountWaitTime(textView);
            return;
        }

        TimerTask timerTask = (TimerTask) taskMap.get(textView);
        if (timerTask != null) {
            stopCountWaitTime(textView);
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (textView != null) {
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (textView != null) {
                                String remainHourAndMinute = DateUtil.getRemainHourAndMinuteString(expiredDttm);
                                textView.setText(remainHourAndMinute);
                            }
                        }
                    });
                }
            }
        };

        taskMap.put(textView, timerTask);

        Timer timer = new Timer();
        timer.schedule(timerTask, 0, 500);

        timerMap.put(textView, timer);
    }

    private void stopCountWaitTime(TextView textView) {
        TimerTask timerTask = (TimerTask) taskMap.get(textView);
        Timer timer = (Timer) timerMap.get(textView);
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    public void goneByAnimationMoreLayout() {
        if (moreButtonLayout != null && moreButtonLayout.getVisibility() == View.VISIBLE) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) moreButtonLayout.getLayoutParams();
            TranslateAnimation animate = new TranslateAnimation(0, 0, 0, moreButtonLayout.getHeight() + layoutParams.bottomMargin);
            animate.setDuration(100);
            animate.setFillAfter(true);
            moreButtonLayout.startAnimation(animate);
            moreButtonLayout.setVisibility(View.GONE);
        }
    }

    public void visibleByAnimationMoreLayout() {
        if (moreButtonLayout != null && moreButtonLayout.getVisibility() == View.GONE && !isEmptyToday) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) moreButtonLayout.getLayoutParams();
            TranslateAnimation animate = new TranslateAnimation(0, 0, moreButtonLayout.getHeight() + layoutParams.bottomMargin, 0);
            animate.setDuration(100);
            animate.setFillAfter(true);
            moreButtonLayout.startAnimation(animate);
            moreButtonLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
            calendar.add(Calendar.DATE, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        startCountWaitTime(mTextTitle, calendar.getTimeInMillis());

        getTodayCard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);

        stopCountWaitTime(mTextTitle);
    }

    private void getTodayCard() {
        Type type = new TypeToken<List<User>>() {
        }.getType();
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.TODAY_CARD_LIST_V2_URL, type, null, getContext());
        RequestFactory requestFactory = new RequestFactory();
//        requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<List<User>>() {
            @Override
            public void onResponse(List<User> todayCardList) {
                if (todayCardList != null && todayCardList.size() > 0) {
                    setTodayList(todayCardList);
                } else {
                    setEmptyView();
                }

                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).doAfterLoading();
                }
            }
        }).execute();
    }

    private void setTodayList(List<User> todayCardList) {
        recyclerView.setVisibility(View.VISIBLE);
        emptyListLayout.setVisibility(View.GONE);
        moreButtonLayout.setVisibility(View.VISIBLE);
        isEmptyToday = false;

        TodayFragment.this.todayCardList = todayCardList;
        adapter = new TodayAdapter(getActivity(), todayCardList);
        recyclerView.setAdapter(adapter);
        verticalLine.setVisibility(View.VISIBLE);
    }

    private void setEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyListLayout.setVisibility(View.VISIBLE);
        moreButtonLayout.setVisibility(View.GONE);
        isEmptyToday = true;

        emptyListDesc.setText(R.string.no_list_today);
        moveFillProfileButton.setText(R.string.get_today_card);
    }

    @OnClick(R.id.move_fill_profile_button)
    void onClickTotalMore() {
        Type type = new TypeToken<List<User>>() {
        }.getType();
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.TODAY_CARD_LIST_URL, type, null, getContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<List<User>>() {
            @Override
            public void onResponse(List<User> todayCardList) {
                setTodayList(todayCardList);
            }
        }).execute();
    }

    private void moveToTheTop() {
        linearLayoutManager.scrollToPosition(0);
    }

    @OnClick(R.id.more_button)
    void onClickMore() {
        if (user == null) {
            return;
        }
        if (CommonUtil.isCompleteProfileUserForToday(getActivity())) {
            if (CommonUtil.haveEnoughPoint(getActivity(), Constants.BUCHI_COUNT_FOR_SHOW_MORE_PERSON)) {
                AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                Bundle bundle = new Bundle();
                String title = getString(R.string.more_get_person);
                bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, title);
                bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, getString(R.string.needed_buchi_count, Constants.BUCHI_COUNT_FOR_SHOW_MORE_PERSON));
                alertDialogFragment.setArguments(bundle);

                alertDialogFragment.setConfirmListener(new BaseDialogFragment.ConfirmListener() {
                    @Override
                    public void onDialogConfirmed() {
                        Type type = new TypeToken<List<User>>() {
                        }.getType();
                        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.TODAY_CARD_OPEN_MORE_URL, type, null, HttpMethod.POST, getContext());
                        RequestFactory requestFactory = new RequestFactory();
                        requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
                        requestFactory.create(httpRequestVO, new HttpResponseCallback<List<User>>() {
                            @Override
                            public void onResponse(List<User> userList) {
                                CommonUtil.syncPoint(getActivity());

                                todayCardList.addAll(0, userList);
                                adapter.notifyDataSetChanged();
                                moveToTheTop();
                            }
                        }).execute();

                        CrushApplication.getInstance().loggingEvent(getString(R.string.ga_today), getString(R.string.ga_today_more), getString(R.string.ga_confirm));
                    }
                });

                alertDialogFragment.show(getActivity().getSupportFragmentManager(), "alertDialogFragment");
            }
        }
    }

    public void onEvent(RefreshEvent refreshEvent) {
        if (refreshEvent.action == RefreshEvent.Action.TODAY_EXPIRED || refreshEvent.action == RefreshEvent.Action.PUSH) {
            needRefresh = true;
            refresh();
        }
        LogUtil.d("TodayFragment RefreshEvent : " + refreshEvent.action);
    }

    public void refresh() {
        if (needRefresh) {
            if (getActivity() != null && ((MainActivity) getActivity()).getCurrentItem() == 1 && isActive) {
                getTodayCard();
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
}
