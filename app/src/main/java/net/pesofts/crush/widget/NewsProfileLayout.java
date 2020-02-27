package net.pesofts.crush.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sendbird.android.sample.SendBirdMessagingChannelListActivity;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.DateUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.activity.UserDetailActivity;
import net.pesofts.crush.fragment.AlertDialogFragment;
import net.pesofts.crush.model.NewsSectionType;
import net.pesofts.crush.model.Result;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpMethod;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;
import net.pesofts.crush.network.RequestManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class NewsProfileLayout extends RelativeLayout {
    @Bind(R.id.profile_image_view)
    CircularNetworkImageView profileImageView;
    @Bind(R.id.name_text)
    TextView nameText;
    @Bind(R.id.info_text)
    TextView infoText;
    @Bind(R.id.dday_text)
    TextView ddayText;
    @Bind(R.id.reply_text)
    TextView replyText;

    private Context context;

    public NewsProfileLayout(Context context) {
        super(context);
        this.context = context;

        initView();
    }

    public NewsProfileLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.item_news_profile, this);

        ButterKnife.bind(this);
    }

    public void setView(final User user, final NewsSectionType newsSectionType) {
        nameText.setText(user.getName());
        String ageString = DateUtil.getAgeString(user.getBirthDate());
        infoText.setText(ageString + ", " + user.getHometown().getValue());
        ddayText.setText("D-" + DateUtil.getRemainDay(user.getExpiredDttm()));

        if (newsSectionType == NewsSectionType.ILike) {
            if (user.isLikecheck()) {
                replyText.setBackgroundResource(R.drawable.home_ico_brownrounded);
                replyText.setText("좋아요확인");
            } else if ("A".equals(user.getReply())) {
                replyText.setBackgroundResource(R.drawable.home_ico_redrounded);
                replyText.setText("성공");
            } else if ("R".equals(user.getReply())) {
                replyText.setBackgroundResource(R.drawable.home_ico_greyrounded);
                replyText.setText("실패");
            } else {
                replyText.setBackgroundResource(R.drawable.home_ico_brownrounded);
                replyText.setText("진행중");
            }
            replyText.setVisibility(View.VISIBLE);
            replyText.setClickable(false);

        } else if (newsSectionType == NewsSectionType.FavorMatch) {
            replyText.setBackgroundResource(R.drawable.home_ico_redrounded);
            replyText.setText("대화하기");
            replyText.setVisibility(View.VISIBLE);
            replyText.setClickable(true);
            replyText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SendBirdMessagingChannelListActivity.class);
                    context.startActivity(intent);
                }
            });
        } else {
            replyText.setVisibility(View.GONE);
        }

        profileImageView.setImageUrl(user.getMainImageUrl(), RequestManager.getImageLoader());

        if (user.isHidden()) {
            setAlpha(profileImageView, 30);
            profileImageView.setColorFilter(0x85000000, PorterDuff.Mode.SRC_ATOP);
            profileImageView.setEnablePressEffect(false);
        } else {
            setAlpha(profileImageView, 255);
            profileImageView.clearColorFilter();
            profileImageView.setEnablePressEffect(true);
        }

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.isHidden()) {
                    if (CommonUtil.haveEnoughPoint((FragmentActivity) context, Constants.BUCHI_COUNT_FOR_OPEN_HIDDEN_CARD)) {
                        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, context.getString(R.string.open_hidden_card, user.getName()));
                        bundle.putString(AlertDialogFragment.DIALOG_ALERT_NAME, context.getString(R.string.needed_buchi_count, Constants.BUCHI_COUNT_FOR_OPEN_HIDDEN_CARD));
                        alertDialogFragment.setArguments(bundle);

                        alertDialogFragment.setEditableConfirmListener(new AlertDialogFragment.EditableConfirmListener() {
                            @Override
                            public void onDialogConfirmed(String editString) {
                                List<NameValuePair> paramInfo = new ArrayList<>();
                                paramInfo.add(new BasicNameValuePair("userid", user.getId()));

                                HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.OPEN_HIDDEN_FAVORED_URL, Result.class, paramInfo, HttpMethod.POST, context);
                                RequestFactory requestFactory = new RequestFactory();
                                requestFactory.setProgressHandler(new ProgressHandler((FragmentActivity) context, false));
                                requestFactory.create(httpRequestVO, new HttpResponseCallback<Result>() {
                                    @Override
                                    public void onResponse(Result result) {
                                        LogUtil.d("Result", result);
                                        if ("OK".equals(result.getCode())) {
                                            moveToDetail(user, newsSectionType);

                                            user.setHidden(false);
                                            setAlpha(profileImageView, 255);
                                            profileImageView.clearColorFilter();
                                            profileImageView.setEnablePressEffect(true);

                                            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.OPEN_HIDDEN_CARD));
                                        }
                                    }
                                }).execute();

                                CrushApplication.getInstance().loggingEvent(context.getString(R.string.news), context.getString(R.string.ga_open_hidden_favor), context.getString(R.string.ga_open_hidden_favor));
                            }
                        });
                        alertDialogFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "alertDialogFragment");
                    }

                } else {
                    moveToDetail(user, newsSectionType);
                }

            }
        });
    }

    private void setAlpha(CircularNetworkImageView view, int alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            profileImageView.setImageAlpha(alpha);
        } else {
            profileImageView.setAlpha(alpha);
        }

    }

    private void moveToDetail(User user, NewsSectionType newsSectionType) {
        final Intent intent = new Intent(context, UserDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("user", user);
        if (newsSectionType == NewsSectionType.History) {
            extras.putString("fromType", "H");
        } else if (newsSectionType == NewsSectionType.FavorMe) {
            extras.putBoolean("fromHidden", true);
        } else if (newsSectionType == NewsSectionType.LikeMe) {
            extras.putBoolean("fromILike", true);
        }
        intent.putExtras(extras);
        context.startActivity(intent);
    }

}