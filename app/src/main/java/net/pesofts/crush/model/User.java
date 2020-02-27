package net.pesofts.crush.model;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    private static final long serialVersionUID = -2226535488757480063L;

    private String id;
    private String name;
    private String mainImageUrl;
    private String gender;
    private String birthDate;
    private UserInfoCode bloodType;
    private UserInfoCode bodyType;
    private UserInfoCode religion;
    private UserInfoCode hometown;
    private UserInfoCode job;
    private UserInfoCode idealType;
    private List<UserInfoCode> hobbyList;
    private List<UserInfoCode> charmTypeList;
    private List<UserInfoCode> favoriteTypeList;
    private String school;
    private int height;
    private int point;
    private int loginPoint;
    private String status;
    private String grade;
    private String reply;
    private boolean isILike;
    private boolean isLikeMe;
    private boolean favorMatch;
    private boolean isHidden;
    private boolean likecheck;
    private String likeMeMessage;
    private String reviewStatus;
    private String reviewMessage;
    private List<Image> imageInfoList;
    private long expiredDttm;

    public boolean isLikecheck() {
        return likecheck;
    }

    public void setLikecheck(boolean likecheck) {
        this.likecheck = likecheck;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public boolean isFavorMatch() {
        return favorMatch;
    }

    public void setFavorMatch(boolean favorMatch) {
        this.favorMatch = favorMatch;
    }

    public int getLoginPoint() {
        return loginPoint;
    }

    public void setLoginPoint(int loginPoint) {
        this.loginPoint = loginPoint;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public void setReviewMessage(String reviewMessage) {
        this.reviewMessage = reviewMessage;
    }

    public long getExpiredDttm() {
        return expiredDttm;
    }

    public void setExpiredDttm(long expiredDttm) {
        this.expiredDttm = expiredDttm;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public UserInfoCode getBloodType() {
        return bloodType;
    }

    public void setBloodType(UserInfoCode bloodType) {
        this.bloodType = bloodType;
    }

    public UserInfoCode getBodyType() {
        return bodyType;
    }

    public void setBodyType(UserInfoCode bodyType) {
        this.bodyType = bodyType;
    }

    public UserInfoCode getReligion() {
        return religion;
    }

    public void setReligion(UserInfoCode religion) {
        this.religion = religion;
    }

    public UserInfoCode getHometown() {
        return hometown;
    }

    public void setHometown(UserInfoCode hometown) {
        this.hometown = hometown;
    }

    public UserInfoCode getJob() {
        return job;
    }

    public void setJob(UserInfoCode job) {
        this.job = job;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public boolean isILike() {
        return isILike;
    }

    public void setIsILike(boolean isILike) {
        this.isILike = isILike;
    }

    public boolean isLikeMe() {
        return isLikeMe;
    }

    public void setIsLikeMe(boolean isLikeMe) {
        this.isLikeMe = isLikeMe;
    }

    public String getLikeMeMessage() {
        return likeMeMessage;
    }

    public void setLikeMeMessage(String likeMeMessage) {
        this.likeMeMessage = likeMeMessage;
    }

    public List<Image> getImageInfoList() {
        return imageInfoList;
    }

    public void setImageInfoList(List<Image> imageInfoList) {
        this.imageInfoList = imageInfoList;
    }

    public UserInfoCode getIdealType() {
        return idealType;
    }

    public void setIdealType(UserInfoCode idealType) {
        this.idealType = idealType;
    }

    public List<UserInfoCode> getHobbyList() {
        return hobbyList;
    }

    public void setHobbyList(List<UserInfoCode> hobbyList) {
        this.hobbyList = hobbyList;
    }

    public List<UserInfoCode> getCharmTypeList() {
        return charmTypeList;
    }

    public void setCharmTypeList(List<UserInfoCode> charmTypeList) {
        this.charmTypeList = charmTypeList;
    }

    public List<UserInfoCode> getFavoriteTypeList() {
        return favoriteTypeList;
    }

    public void setFavoriteTypeList(List<UserInfoCode> favoriteTypeList) {
        this.favoriteTypeList = favoriteTypeList;
    }

    public boolean isMale() {
        if ("M".equals(gender)) {
            return true;
        }
        return false;
    }
}
