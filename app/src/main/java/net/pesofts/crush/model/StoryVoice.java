package net.pesofts.crush.model;

/**
 * Created by erkas on 2017. 5. 4..
 */

public class StoryVoice {
    private long userid;
    private String imageurlpath;
    private int pointScore;
    private String replycount;
    private String voicecloudid;
    private int replyScore;
    private int maxReplyCount;
    private String voiceuripath;
    private int point;
    private int createScore;
    private int score;
    private String username;

    public long getUserid() {
        return userid;
    }

    public String getImageurlpath() {
        return imageurlpath;
    }

    public int getPointScore() {
        return pointScore;
    }

    public String getReplycount() {
        return replycount;
    }

    public String getVoicecloudid() {
        return voicecloudid;
    }

    public int getReplyScore() {
        return replyScore;
    }

    public int getMaxReplyCount() {
        return maxReplyCount;
    }

    public String getVoiceuripath() {
        return voiceuripath;
    }

    public int getPoint() {
        return point;
    }

    public int getCreateScore() {
        return createScore;
    }

    public int getScore() {
        return score;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "StoryVoice{" +
                "userid=" + userid +
                ", imageurlpath='" + imageurlpath + '\'' +
                ", pointScore=" + pointScore +
                ", replycount='" + replycount + '\'' +
                ", voicecloudid='" + voicecloudid + '\'' +
                ", replyScore=" + replyScore +
                ", maxReplyCount=" + maxReplyCount +
                ", voiceuripath='" + voiceuripath + '\'' +
                ", point=" + point +
                ", createScore=" + createScore +
                ", score=" + score +
                ", username='" + username + '\'' +
                '}';
    }
}
