package net.pesofts.crush.model;

import java.io.Serializable;
import java.util.List;

public class CardList implements Serializable {
    private static final long serialVersionUID = -582683946623543070L;

    private long nextDttm;
    private NewsSectionType newsSectionType;
    private List<User> list;

    public long getNextDttm() {
        return nextDttm;
    }

    public void setNextDttm(long nextDttm) {
        this.nextDttm = nextDttm;
    }

    public List<User> getList() {
        return list;
    }

    public void setList(List<User> list) {
        this.list = list;
    }

    public NewsSectionType getNewsSectionType() {
        return newsSectionType;
    }

    public void setNewsSectionType(NewsSectionType newsSectionType) {
        this.newsSectionType = newsSectionType;
    }
}
