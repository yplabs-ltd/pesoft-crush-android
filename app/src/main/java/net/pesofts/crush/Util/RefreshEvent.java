package net.pesofts.crush.Util;

public class RefreshEvent {

    public Action action;
    public String type;

    public RefreshEvent(Action action) {
        this.action = action;
    }

    public RefreshEvent(Action action, String type) {
        this.action = action;
        this.type = type;
    }

    public enum Action {
        STATUS_CHANGE,
        EVALUATE,
        LIKE,
        TODAY_EXPIRED,
        REPLY,
        PUSH,
        OPEN_HIDDEN_CARD,
        EXPIRED_REFRESH_TIME
    }
}
