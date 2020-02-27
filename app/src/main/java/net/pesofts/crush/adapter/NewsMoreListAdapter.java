package net.pesofts.crush.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import net.pesofts.crush.R;
import net.pesofts.crush.model.CardList;
import net.pesofts.crush.widget.NewsProfileLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewsMoreListAdapter extends RecyclerView.Adapter {
    private CardList cardList;
    private Context context;

    public class NewsMoreListViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.profile_layout)
        NewsProfileLayout profileLayout;
        @Bind(R.id.more_layout)
        RelativeLayout moreLayout;

        public NewsMoreListViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public NewsMoreListAdapter(Context context, CardList cardList) {
        this.cardList = cardList;
        this.context = context;
    }

    @Override
    public NewsMoreListViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_more_list, parent, false);
        NewsMoreListViewHolder vh = new NewsMoreListViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        NewsMoreListViewHolder newsMoreListViewHolder = (NewsMoreListViewHolder) holder;
        newsMoreListViewHolder.profileLayout.setView(cardList.getList().get(position), cardList.getNewsSectionType());

        if (position == 0 || position == 1 || position == 2) {
            newsMoreListViewHolder.moreLayout.setPadding(0, context.getResources().getDimensionPixelSize(R.dimen.new_more_list_item_padding), 0, 0);
        } else {
            newsMoreListViewHolder.moreLayout.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return cardList.getList().size();
    }

}
