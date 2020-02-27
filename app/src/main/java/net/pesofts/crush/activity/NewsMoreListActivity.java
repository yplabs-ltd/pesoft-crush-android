package net.pesofts.crush.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import net.pesofts.crush.Constants;
import net.pesofts.crush.CrushApplication;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.adapter.NewsMoreListAdapter;
import net.pesofts.crush.model.CardList;
import net.pesofts.crush.model.NewsSectionType;
import net.pesofts.crush.model.User;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewsMoreListActivity extends FragmentActivity {

    @Bind(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.name_text)
    TextView nameText;

    private CardList cardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_more_list);

        ButterKnife.bind(this);

        CrushApplication.getInstance().loggingView(getString(R.string.ga_news_more));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        cardList = (CardList) getIntent().getSerializableExtra("cardList");
        if (cardList != null && cardList.getNewsSectionType() != null) {
            final NewsSectionType newsSectionType = cardList.getNewsSectionType();
            String url = "";
            if (newsSectionType == NewsSectionType.LikeMe) {
                url = Constants.LIKE_ME_CARD_LIST_URL;
            } else if (newsSectionType == NewsSectionType.ILike) {
                url = Constants.I_LIKE_CARD_LIST_URL;
            } else if (newsSectionType == NewsSectionType.IFavor) {
                url = Constants.I_FAVOR_CARD_LIST_URL;
            } else if (newsSectionType == NewsSectionType.FavorMe) {
                url = Constants.FAVOR_ME_CARD_LIST_URL;
            } else if (newsSectionType == NewsSectionType.FavorMatch) {
                url = Constants.FAVOR_MATCH_CARD_LIST_URL;
            } else if (newsSectionType == NewsSectionType.History) {
                url = Constants.HISTORY_CARD_LIST_URL;
            }

            Type type = new TypeToken<List<User>>() {
            }.getType();
            HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(url, type, null, this);
            RequestFactory requestFactory = new RequestFactory();
            requestFactory.setProgressHandler(new ProgressHandler(this, false));
            requestFactory.create(httpRequestVO, new HttpResponseCallback<List<User>>() {
                @Override
                public void onResponse(List<User> userList) {
                    if (userList != null && userList.size() > 0) {
                        CardList cardList = new CardList();
                        cardList.setList(userList);
                        cardList.setNewsSectionType(newsSectionType);

                        setViews(cardList);
                    }
                }
            }).execute();

        }
    }

    private void setViews(CardList cardList) {
        if (cardList.getNewsSectionType() == NewsSectionType.LikeMe) {
            nameText.setText(R.string.like_me);
        } else if (cardList.getNewsSectionType() == NewsSectionType.ILike) {
            nameText.setText(R.string.i_like);
        } else if (cardList.getNewsSectionType() == NewsSectionType.FavorMe) {
            nameText.setText(R.string.favor_me);
        } else if (cardList.getNewsSectionType() == NewsSectionType.IFavor) {
            nameText.setText(R.string.i_favor);
        } else if (cardList.getNewsSectionType() == NewsSectionType.History) {
            nameText.setText(R.string.history);
        } else if (cardList.getNewsSectionType() == NewsSectionType.FavorMatch) {
            nameText.setText(R.string.favor_match);
        }

        final NewsMoreListAdapter adapter = new NewsMoreListAdapter(this, cardList);
        recyclerView.setAdapter(adapter);
    }

    @OnClick(R.id.close_button)
    void onCilckCloseButton() {
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }

}
