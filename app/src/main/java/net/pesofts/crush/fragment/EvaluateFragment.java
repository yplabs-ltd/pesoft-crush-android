package net.pesofts.crush.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.onesignal.OneSignal;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.DateUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.OneSignalUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.Util.StringUtil;
import net.pesofts.crush.activity.MainActivity;
import net.pesofts.crush.activity.ProfileActivity;
import net.pesofts.crush.activity.UserDetailActivity;
import net.pesofts.crush.model.CardList;
import net.pesofts.crush.model.Result;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpNetworkError;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.network.RequestManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class EvaluateFragment extends Fragment {

    @Bind(R.id.evaluate_layout)
    RelativeLayout evaluateLayout;
    @Bind(R.id.profile_image_view)
    NetworkImageView profileImageView;
    @Bind(R.id.name_text)
    TextView nameTextView;
    @Bind(R.id.info_text)
    TextView infoTextView;
    @Bind(R.id.empty_list_desc)
    TextView emptyListDesc;
    @Bind(R.id.wait_count_text)
    TextView waitCountText;
    @Bind(R.id.move_fill_profile_button)
    TextView moveFillProfileButton;
    @Bind(R.id.empty_list_layout)
    RelativeLayout emptyListLayout;
    @Bind(R.id.wait_evaluate_layout)
    RelativeLayout waitEvaluateLayout;
    @Bind(R.id.circle_progress)
    DonutProgress circleProgress;

    private CardList cardList;
    private List<User> users;
    private TimerTask timerTask;
    private Timer timer;
    private final Handler handler = new Handler();
    private boolean needRefresh = false;
    private boolean isActive = true;

    public static EvaluateFragment newInstance() {
        Bundle args = new Bundle();

        EvaluateFragment fragment = new EvaluateFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainLayout = inflater.inflate(
                R.layout.fragment_evaluate, container, false);

        ButterKnife.bind(this, mainLayout);


        return mainLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EventBus.getDefault().register(this);
        setViews();
    }

    private void setViews() {
        if (CommonUtil.isCompleteProfileUser(getActivity())) {
            getEvaluateUsers();
        } else if (CommonUtil.isFirstReviewingUser(getActivity())) {
            evaluateLayout.setVisibility(View.GONE);
            waitEvaluateLayout.setVisibility(View.GONE);
            emptyListLayout.setVisibility(View.VISIBLE);
            emptyListDesc.setText(getString(R.string.review_profile_and_wait_desc, getString(R.string.evaluate)));
            moveFillProfileButton.setVisibility(View.GONE);
        } else {
            evaluateLayout.setVisibility(View.GONE);
            waitEvaluateLayout.setVisibility(View.GONE);
            emptyListLayout.setVisibility(View.VISIBLE);
            emptyListDesc.setText(getString(R.string.no_profile_desc, getString(R.string.evaluate)));
            moveFillProfileButton.setVisibility(View.VISIBLE);
        }
    }

    private void getEvaluateUsers() {
        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.EVALUATE_CARD_LIST_URL, CardList.class, null, getContext());
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<CardList>() {
            @Override
            public void onResponse(CardList evaluateCardList) {

                evaluateLayout.setVisibility(View.VISIBLE);
                waitEvaluateLayout.setVisibility(View.GONE);
                emptyListLayout.setVisibility(View.GONE);

                cardList = evaluateCardList;
                users = evaluateCardList.getList();

                if (users != null && users.size() > 0) {
                    updateTabCount(users.size());
                    OneSignal.sendTag(OneSignalUtil.WAIT_EVALUATE, "false");
                }
                setEvaluateUser();

            }
        }).execute();
    }

    private void setWaitEvaluateView() {
        evaluateLayout.setVisibility(View.GONE);
        waitEvaluateLayout.setVisibility(View.VISIBLE);
        emptyListLayout.setVisibility(View.GONE);

        startCountWaitTime();

        OneSignal.sendTag(OneSignalUtil.WAIT_EVALUATE, "true");
    }

    private void updateTabCount(int count) {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setEvaluateCount(count);
        }
    }

    private void setWaitCount() {
        if ("0".equals(DateUtil.getRemainHourAndMinuteAndSec(cardList.getNextDttm()))) {
            if (timerTask != null) {
                timerTask.cancel();
            }
            if (timer != null) {
                timer.cancel();
            }

            needRefresh = true;
            refresh();

            return;
        }

        if (waitCountText != null) {
            waitCountText.setText("-" + DateUtil.getRemainHourAndMinuteAndSec(cardList.getNextDttm()));
        }
        if (circleProgress != null) {
            circleProgress.setProgress(DateUtil.getProgressByRemainTime(cardList.getNextDttm()));
        }
    }

    private void setEvaluateUser() {
        if (users.size() < 1) {
            setWaitEvaluateView();
            return;
        }
        final User user = users.get(0);

        profileImageView.setImageUrl(user.getMainImageUrl(), RequestManager.getImageLoader());

        if (user.getName() != null) {
            nameTextView.setText(user.getName());
        } else {
            nameTextView.setText("");
        }

        String infoString = "";
        if (user.getBirthDate() != null || user.getHometown() != null || user.getJob() != null) {
            if (user.getBirthDate() != null) {
                infoString = DateUtil.getAgeString(user.getBirthDate());
            }
            if (user.getHometown() != null) {
                if (StringUtil.isNotEmpty(infoString)) {
                    infoString = infoString + ", ";
                }
                infoString = infoString + user.getHometown().getValue();
            }
            if (user.getJob() != null) {
                if (StringUtil.isNotEmpty(infoString)) {
                    infoString = infoString + ", ";
                }
                infoString = infoString + user.getJob().getValue();
            }
        }

        infoTextView.setText(infoString);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToUserDetail();
            }
        });

    }

    private void moveToUserDetail() {
        final Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("user", users.get(0));
        extras.putString("fromType", "EV");
        intent.putExtras(extras);
        startActivity(intent);
    }

    @OnClick(R.id.move_fill_profile_button)
    void onClickMoveFillProfile() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.bad_button)
    void badButtonClick() {
        evaluateUser("B");
        CrushApplication.getInstance().loggingEvent(getString(R.string.ga_evaluate), getString(R.string.ga_evaluate), getString(R.string.ga_bad));
    }

    @OnClick(R.id.good_button)
    void goodButtonClick() {
        evaluateUser("G");
        CrushApplication.getInstance().loggingEvent(getString(R.string.ga_evaluate), getString(R.string.ga_evaluate), getString(R.string.ga_good));
    }

    @OnClick(R.id.profile_more_button)
    void profileMoreButtonClick() {
        moveToUserDetail();
    }

    private void evaluateUser(final String evaluate) {
        List<NameValuePair> paramInfo = new ArrayList<>();
        paramInfo.add(new BasicNameValuePair("userid", users.get(0).getId()));
        paramInfo.add(new BasicNameValuePair("value", evaluate));

        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.EVALUATE_USER_URL, Result.class, paramInfo, HttpMethod.POST, getContext());
        RequestFactory requestFactory = new RequestFactory();
//        requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
        requestFactory.create(httpRequestVO, new HttpResponseCallback<Result>() {
            @Override
            public void onResponse(Result result) {
                if (users.size() > 0) {
                    users.remove(0);
                }

                updateTabCount(users.size());

                setEvaluateUser();

                if ("G".equals(evaluate)) {
                    EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.EVALUATE));
                }
            }

            @Override
            public void onError(HttpNetworkError error) {
                if (users.size() > 0) {
                    users.remove(0);
                }

                updateTabCount(users.size());

                setEvaluateUser();
            }
        }).execute();
    }

    private void startCountWaitTime() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Update();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);
    }


    private void Update() {
        Runnable updater = new Runnable() {
            public void run() {
                setWaitCount();
            }
        };
        handler.post(updater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(RefreshEvent refreshEvent) {
        if (refreshEvent.action == RefreshEvent.Action.STATUS_CHANGE || refreshEvent.action == RefreshEvent.Action.PUSH) {
            needRefresh = true;
            refresh();
        }
        LogUtil.d("EvaluateFragment RefreshEvent : " + refreshEvent.action);
    }

    public void refresh() {
        if (needRefresh) {
            if (getActivity() != null && ((MainActivity) getActivity()).getCurrentItem() == 0 && isActive) {
                setViews();
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
