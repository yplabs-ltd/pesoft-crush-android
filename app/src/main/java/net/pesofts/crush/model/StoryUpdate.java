package net.pesofts.crush.model;

/**
 * Created by erkas on 2017. 6. 23..
 */

public class StoryUpdate {
    private long updateTime;

    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public String toString() {
        return "StoryUpdate{" +
                "updateTime=" + updateTime +
                '}';
    }
}
