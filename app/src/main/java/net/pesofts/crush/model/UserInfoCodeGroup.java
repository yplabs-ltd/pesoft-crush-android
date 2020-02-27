package net.pesofts.crush.model;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.List;

public class UserInfoCodeGroup implements ParentListItem {
    private String title;
    private List<UserInfoCode> userInfoCodeList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<UserInfoCode> getUserInfoCodeList() {
        return userInfoCodeList;
    }

    public void setUserInfoCodeList(List<UserInfoCode> userInfoCodeList) {
        this.userInfoCodeList = userInfoCodeList;
    }

    @Override
    public List<?> getChildItemList() {
        return userInfoCodeList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
