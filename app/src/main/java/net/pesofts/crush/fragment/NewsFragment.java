package net.pesofts.crush.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.pesofts.crush.Constants;
import net.pesofts.crush.R;
import net.pesofts.crush.Util.CommonUtil;
import net.pesofts.crush.Util.HttpUtil;
import net.pesofts.crush.Util.LogUtil;
import net.pesofts.crush.Util.RefreshEvent;
import net.pesofts.crush.activity.MainActivity;
import net.pesofts.crush.activity.ProfileActivity;
import net.pesofts.crush.adapter.NewsAdapter;
import net.pesofts.crush.model.CardList;
import net.pesofts.crush.model.CardListSection;
import net.pesofts.crush.model.NewsSectionType;
import net.pesofts.crush.network.HttpNetworkError;
import net.pesofts.crush.network.HttpRequestVO;
import net.pesofts.crush.network.HttpResponseCallback;
import net.pesofts.crush.network.ProgressHandler;
import net.pesofts.crush.network.RequestFactory;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class NewsFragment extends Fragment {

    @Bind(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.empty_list_desc)
    TextView emptyListDesc;
    @Bind(R.id.move_fill_profile_button)
    TextView moveFillProfileButton;
    @Bind(R.id.empty_list_layout)
    RelativeLayout emptyListLayout;

    private boolean needRefresh = false;
    private boolean isActive = true;

    public static NewsFragment newInstance() {
        Bundle args = new Bundle();

        NewsFragment fragment = new NewsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainLayout = inflater.inflate(
                R.layout.fragment_news, container, false);

        ButterKnife.bind(this, mainLayout);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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
            HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.CARD_LIST_URL, CardListSection.class, null, getContext());
            RequestFactory requestFactory = new RequestFactory();
//            requestFactory.setProgressHandler(new ProgressHandler(getActivity(), false));
            requestFactory.create(httpRequestVO, requestCallback).execute();
        } else if (CommonUtil.isFirstReviewingUser(getActivity())) {
            recyclerView.setVisibility(View.GONE);
            emptyListLayout.setVisibility(View.VISIBLE);
            emptyListDesc.setText(getString(R.string.review_profile_and_wait_desc));
            moveFillProfileButton.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyListLayout.setVisibility(View.VISIBLE);
            emptyListDesc.setText(getString(R.string.no_profile_desc, getString(R.string.news)));
            moveFillProfileButton.setVisibility(View.VISIBLE);
        }
    }

    private HttpResponseCallback<CardListSection> requestCallback = new HttpResponseCallback<CardListSection>() {
        @Override
        public void onResponse(CardListSection cardListSection) {
            if (cardListSection == null ||
                    ((cardListSection.getLikeMeCards() == null || cardListSection.getLikeMeCards().size() == 0) &&
                            (cardListSection.getFavorMeHiddenCards() == null || cardListSection.getFavorMeHiddenCards().size() == 0) &&
                            (cardListSection.getIfavorCards() == null || cardListSection.getIfavorCards().size() == 0) &&
                            (cardListSection.getILikeCards() == null || cardListSection.getILikeCards().size() == 0) &&
                            (cardListSection.getHistoryCards() == null || cardListSection.getHistoryCards().size() == 0))) {
                setEmptyView();
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyListLayout.setVisibility(View.GONE);
                NewsAdapter adapter = new NewsAdapter(getActivity(), getCardListsForNewsSection(cardListSection));
                recyclerView.setAdapter(adapter);
            }
        }

        @Override
        public void onError(HttpNetworkError error) {
        }
    };

    private void setEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyListLayout.setVisibility(View.VISIBLE);
        emptyListDesc.setText(R.string.no_list_news);
        moveFillProfileButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.move_fill_profile_button)
    void onClickMoveFillProfile() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);
    }

    private List<CardList> getCardListsForNewsSection(CardListSection cardListSection) {
        List<CardList> cardLists = new ArrayList<>();

        if (cardListSection.getLikeMeCards() != null && cardListSection.getLikeMeCards().size() > 0) {
            CardList cardList1 = new CardList();
            cardList1.setList(cardListSection.getLikeMeCards());
            cardList1.setNewsSectionType(NewsSectionType.LikeMe);
            cardLists.add(cardList1);
        }
        if (cardListSection.getILikeCards() != null && cardListSection.getILikeCards().size() > 0) {
            CardList cardList2 = new CardList();
            cardList2.setList(cardListSection.getILikeCards());
            cardList2.setNewsSectionType(NewsSectionType.ILike);
            cardLists.add(cardList2);
        }
        if (cardListSection.getFavorMatchCards() != null && cardListSection.getFavorMatchCards().size() > 0) {
            CardList cardList3 = new CardList();
            cardList3.setList(cardListSection.getFavorMatchCards());
            cardList3.setNewsSectionType(NewsSectionType.FavorMatch);
            cardLists.add(cardList3);
        }
        if (cardListSection.getFavorMeHiddenCards() != null && cardListSection.getFavorMeHiddenCards().size() > 0) {
            CardList cardList6 = new CardList();
            cardList6.setList(cardListSection.getFavorMeHiddenCards());
            cardList6.setNewsSectionType(NewsSectionType.FavorMe);
            cardLists.add(cardList6);
        }
        if (cardListSection.getIfavorCards() != null && cardListSection.getIfavorCards().size() > 0) {
            CardList cardList4 = new CardList();
            cardList4.setList(cardListSection.getIfavorCards());
            cardList4.setNewsSectionType(NewsSectionType.IFavor);
            cardLists.add(cardList4);
        }
        if (cardListSection.getHistoryCards() != null && cardListSection.getHistoryCards().size() > 0) {
            CardList cardList5 = new CardList();
            cardList5.setList(cardListSection.getHistoryCards());
            cardList5.setNewsSectionType(NewsSectionType.History);
            cardLists.add(cardList5);
        }

        return cardLists;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(RefreshEvent refreshEvent) {
        if (refreshEvent.action == RefreshEvent.Action.PUSH) {
            needRefresh = true;
            refresh();
        }
        LogUtil.d("NewsFragment RefreshEvent : " + refreshEvent.action);
    }

    public void refresh() {
        if (needRefresh) {
            if (getActivity() != null && ((MainActivity) getActivity()).getCurrentItem() == 3 && isActive) {
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
