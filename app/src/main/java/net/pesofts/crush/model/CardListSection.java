package net.pesofts.crush.model;

import java.util.List;

public class CardListSection {

    private List<User> likeMeCards;
    private List<User> ILikeCards;
    private List<User> favorMatchCards;
    private List<User> favorMeHiddenCards;
    private List<User> IfavorCards;
    private List<User> historyCards;

    public List<User> getFavorMatchCards() {
        return favorMatchCards;
    }

    public void setFavorMatchCards(List<User> favorMatchCards) {
        this.favorMatchCards = favorMatchCards;
    }

    public List<User> getHistoryCards() {
        return historyCards;
    }

    public void setHistoryCards(List<User> historyCards) {
        this.historyCards = historyCards;
    }

    public List<User> getLikeMeCards() {
        return likeMeCards;
    }

    public void setLikeMeCards(List<User> likeMeCards) {
        this.likeMeCards = likeMeCards;
    }

    public List<User> getILikeCards() {
        return ILikeCards;
    }

    public void setILikeCards(List<User> ILikeCards) {
        this.ILikeCards = ILikeCards;
    }

    public List<User> getFavorMeHiddenCards() {
        return favorMeHiddenCards;
    }

    public void setFavorMeHiddenCards(List<User> favorMeHiddenCards) {
        this.favorMeHiddenCards = favorMeHiddenCards;
    }

    public List<User> getIfavorCards() {
        return IfavorCards;
    }

    public void setIfavorCards(List<User> ifavorCards) {
        IfavorCards = ifavorCards;
    }
}
