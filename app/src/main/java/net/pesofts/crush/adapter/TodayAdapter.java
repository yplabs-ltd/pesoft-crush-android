package net.pesofts.crush.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.DateUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.activity.UserDetailActivity;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.RequestManager;
import net.pesofts.crush.widget.CircularNetworkImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class TodayAdapter extends RecyclerView.Adapter {
    private List<User> users;
    private Context context;
    private Map taskMap = new HashMap<>();
    private Map timerMap = new HashMap<>();

    public class TodayViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.profile_image_view)
        CircularNetworkImageView profileImageView;
        @Bind(R.id.popular_text)
        TextView popularText;
        @Bind(R.id.name_text)
        TextView nameText;
        @Bind(R.id.info_text)
        TextView infoText;
        @Bind(R.id.info_text2)
        TextView infoText2;
        @Bind(R.id.remain_time_view)
        TextView remainTimeView;
        @Bind(R.id.empty_vertical_line)
        View emptyVerticalLine;
        @Bind(R.id.footer_empty_view)
        View footerEmptyView;

        public TodayViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);
        }
    }

    public TodayAdapter(Context context, List<User> users) {
        this.users = users;
        this.context = context;
    }

    @Override
    public TodayViewHolder onCreateViewHolder(ViewGroup parent,
                                              int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_today, parent, false);
        TodayViewHolder vh = new TodayViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        final User user = users.get(position);

        ((TodayViewHolder) holder).nameText.setText(user.getName());
        String ageString = DateUtil.getAgeString(user.getBirthDate());
        ((TodayViewHolder) holder).infoText.setText(ageString + ", " + user.getHometown().getValue());
        ((TodayViewHolder) holder).infoText2.setText(user.getJob().getValue());

        if ((position > 0 && user.getExpiredDttm() != users.get(position - 1).getExpiredDttm())) {
//            startCountWaitTime(((TodayViewHolder) holder).remainTimeView, user.getExpiredDttm());

            ((TodayViewHolder) holder).remainTimeView.setVisibility(View.VISIBLE);
            ((TodayViewHolder) holder).remainTimeView.setText(DateUtil.getRemainDayString(user.getExpiredDttm()));
            if (position == 0) {
                ((TodayViewHolder) holder).emptyVerticalLine.setVisibility(View.VISIBLE);
            } else {
                ((TodayViewHolder) holder).emptyVerticalLine.setVisibility(View.INVISIBLE);
            }
        } else {
//            stopCountWaitTime(((TodayViewHolder) holder).remainTimeView);

            ((TodayViewHolder) holder).remainTimeView.setVisibility(View.GONE);
            ((TodayViewHolder) holder).emptyVerticalLine.setVisibility(View.GONE);
        }

        if (!"N".equals(user.getGrade())) {
            ((TodayViewHolder) holder).profileImageView.setBackgroundResource(R.drawable.red_border_white_solid_large_circle);
            ((TodayViewHolder) holder).nameText.setTextColor(ContextCompat.getColor(context, R.color.main_red_color));
            ((TodayViewHolder) holder).infoText.setTextColor(ContextCompat.getColor(context, R.color.main_red_color));
            ((TodayViewHolder) holder).infoText2.setTextColor(ContextCompat.getColor(context, R.color.main_red_color));
            ((TodayViewHolder) holder).popularText.setBackgroundResource(R.drawable.home_ico_redrounded);
            ((TodayViewHolder) holder).popularText.setText(R.string.popular);
            ((TodayViewHolder) holder).popularText.setVisibility(View.VISIBLE);
        } else {
            ((TodayViewHolder) holder).profileImageView.setBackgroundResource(R.drawable.grey_border_white_solid_large_circle);
            ((TodayViewHolder) holder).nameText.setTextColor(ContextCompat.getColor(context, R.color.main_text_color));
            ((TodayViewHolder) holder).infoText.setTextColor(ContextCompat.getColor(context, R.color.main_text_color));
            ((TodayViewHolder) holder).infoText2.setTextColor(ContextCompat.getColor(context, R.color.main_text_color));
            ((TodayViewHolder) holder).popularText.setVisibility(View.GONE);
        }

        ((TodayViewHolder) holder).profileImageView.setImageUrl(user.getMainImageUrl(), RequestManager.getImageLoader());
        ((TodayViewHolder) holder).profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isCompleteProfileUserForToday(context)) {
                    final Intent intent = new Intent(context, UserDetailActivity.class);
                    Bundle extras = new Bundle();
                    extras.putSerializable("user", user);
                    if (!DateUtil.isPastTime(user.getExpiredDttm())) {
                        extras.putString("fromType", "N");
                    }
                    intent.putExtras(extras);
                    context.startActivity(intent);
                }

            }
        });

        if (position == users.size() - 1) {
            ((TodayViewHolder) holder).footerEmptyView.setVisibility(View.VISIBLE);
        } else {
            ((TodayViewHolder) holder).footerEmptyView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void startCountWaitTime(final TextView textView, final long expiredDttm) {
//        if (DateUtil.isPastTime(expiredDttm)) {
//            stopCountWaitTime(textView);
//            return;
//        }
//
//        TimerTask timerTask = (TimerTask) taskMap.get(textView);
//        if (timerTask != null) {
//            stopCountWaitTime(textView);
//        }
//
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                if (textView != null) {
//                    textView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (textView != null) {
//                                if (DateUtil.isPastTime(expiredDttm)) {
//                                    EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.TODAY_EXPIRED));
//                                    stopCountWaitTime(textView);
//                                    return;
//                                }
//                                String remainHourAndMinute = DateUtil.getRemainHourAndMinute(expiredDttm);
//                                if (textView.getText().toString().indexOf(":") > 0) {
//                                    remainHourAndMinute = remainHourAndMinute.replace(":", " ");
//                                }
//                                textView.setText(remainHourAndMinute);
//                            }
//                        }
//                    });
//                }
//            }
//        };
//
//        taskMap.put(textView, timerTask);
//
//        Timer timer = new Timer();
//        timer.schedule(timerTask, 0, 500);
//
//        timerMap.put(textView, timer);
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

}
