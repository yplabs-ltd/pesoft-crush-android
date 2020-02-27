package net.pesofts.crush.Util;

import com.onesignal.OneSignal;

public class OneSignalUtil {
    public static final String USER_ID = "userId";
    public static final String ENABLE_CARD = "enableCard";
    public static final String ENABLE_LIKE = "enableLike";
    public static final String ENABLE_CHAT = "enableChat";
    public static final String WAIT_EVALUATE = "waitEvaluate";

    public static void resetTags(String userId) {
        OneSignal.sendTag(USER_ID, userId);
        OneSignal.sendTag(ENABLE_CARD, "true");
        OneSignal.sendTag(ENABLE_LIKE, "true");
        OneSignal.sendTag(ENABLE_CHAT, "true");
        OneSignal.sendTag(WAIT_EVALUATE, "false");
    }

    public static void deleteTags() {
        OneSignal.deleteTag(USER_ID);
        OneSignal.deleteTag(ENABLE_CARD);
        OneSignal.deleteTag(ENABLE_LIKE);
        OneSignal.deleteTag(ENABLE_CHAT);
        OneSignal.deleteTag(WAIT_EVALUATE);
    }
}
