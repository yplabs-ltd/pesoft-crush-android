package net.pesofts.crush.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import net.pesofts.crush.R;
import net.pesofts.crush.activity.NewsMoreListActivity;
import net.pesofts.crush.model.CardList;
import net.pesofts.crush.model.NewsSectionType;
import net.pesofts.crush.model.User;
import net.pesofts.crush.widget.NewsProfileLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewsAdapter extends RecyclerView.Adapter {
    private List<CardList> cardLists;
    private Context context;

    public class TodayViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.title_image)
        ImageView titleImage;
        @Bind(R.id.list_more_button)
        ImageButton listMoreButton;
        @Bind(R.id.left_profile_layout)
        NewsProfileLayout leftProfileLayout;
        @Bind(R.id.center_profile_layout)
        NewsProfileLayout centerProfileLayout;
        @Bind(R.id.right_profile_layout)
        NewsProfileLayout rightProfileLayout;
        @Bind(R.id.left_second_profile_layout)
        NewsProfileLayout leftSecondProfileLayout;
        @Bind(R.id.center_second_profile_layout)
        NewsProfileLayout centerSecondProfileLayout;
        @Bind(R.id.right_second_profile_layout)
        NewsProfileLayout rightSecondProfileLayout;
        @Bind(R.id.footer_empty_view)
        View footerEmptyView;

        public TodayViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public NewsAdapter(Context context, List<CardList> cardLists) {
        this.cardLists = cardLists;
        this.context = context;
    }

    @Override
    public TodayViewHolder onCreateViewHolder(ViewGroup parent,
                                              int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        TodayViewHolder vh = new TodayViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final CardList cardList = cardLists.get(position);
        final List<User> users = cardList.getList();
        TodayViewHolder todayViewHolder = (TodayViewHolder) holder;

        if (cardList.getNewsSectionType() == NewsSectionType.LikeMe) {
            todayViewHolder.titleImage.setImageResource(R.drawable.home_icon_shelikesyou);
        } else if (cardList.getNewsSectionType() == NewsSectionType.ILike) {
            todayViewHolder.titleImage.setImageResource(R.drawable.home_icon_youlikeher);
        } else if (cardList.getNewsSectionType() == NewsSectionType.FavorMe) {
            todayViewHolder.titleImage.setImageResource(R.drawable.home_icon_shepicked);
        } else if (cardList.getNewsSectionType() == NewsSectionType.IFavor) {
            todayViewHolder.titleImage.setImageResource(R.drawable.home_icon_youpicked);
        } else if (cardList.getNewsSectionType() == NewsSectionType.History) {
            todayViewHolder.titleImage.setImageResource(R.drawable.home_icon_pastgirl);
        } else if (cardList.getNewsSectionType() == NewsSectionType.FavorMatch) {
            todayViewHolder.titleImage.setImageResource(R.drawable.home_icon_bothpicked);
        }

        if (users.size() > 0) {
            todayViewHolder.leftProfileLayout.setView(users.get(0), cardList.getNewsSectionType());
            todayViewHolder.leftProfileLayout.setVisibility(View.VISIBLE);
        } else {
            todayViewHolder.leftProfileLayout.setVisibility(View.GONE);
        }
        if (users.size() > 1) {
            todayViewHolder.centerProfileLayout.setView(users.get(1), cardList.getNewsSectionType());
            todayViewHolder.centerProfileLayout.setVisibility(View.VISIBLE);
        } else {
            todayViewHolder.centerProfileLayout.setVisibility(View.GONE);
        }
        if (users.size() > 2) {
            todayViewHolder.rightProfileLayout.setView(users.get(2), cardList.getNewsSectionType());
            todayViewHolder.rightProfileLayout.setVisibility(View.VISIBLE);
        } else {
            todayViewHolder.rightProfileLayout.setVisibility(View.GONE);
        }
        if (users.size() > 3) {
            todayViewHolder.leftSecondProfileLayout.setView(users.get(3), cardList.getNewsSectionType());
            todayViewHolder.leftSecondProfileLayout.setVisibility(View.VISIBLE);
        } else {
            todayViewHolder.leftSecondProfileLayout.setVisibility(View.GONE);
        }
        if (users.size() > 4) {
            todayViewHolder.centerSecondProfileLayout.setView(users.get(4), cardList.getNewsSectionType());
            todayViewHolder.centerSecondProfileLayout.setVisibility(View.VISIBLE);
        } else {
            todayViewHolder.centerSecondProfileLayout.setVisibility(View.GONE);
        }
        if (users.size() > 5) {
            todayViewHolder.rightSecondProfileLayout.setView(users.get(5), cardList.getNewsSectionType());
            todayViewHolder.rightSecondProfileLayout.setVisibility(View.VISIBLE);
        } else {
            todayViewHolder.rightSecondProfileLayout.setVisibility(View.GONE);
        }

        if (users.size() > 6) {
            todayViewHolder.listMoreButton.setVisibility(View.VISIBLE);
        } else {
            todayViewHolder.listMoreButton.setVisibility(View.GONE);
        }

        todayViewHolder.listMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(context, NewsMoreListActivity.class);
                Bundle extras = new Bundle();
                extras.putSerializable("cardList", cardList);
                intent.putExtras(extras);
                context.startActivity(intent);
            }
        });

        if (cardLists.size() == position + 1) {
            todayViewHolder.footerEmptyView.setVisibility(View.VISIBLE);
        } else {
            todayViewHolder.footerEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return cardLists.size();
    }

}
