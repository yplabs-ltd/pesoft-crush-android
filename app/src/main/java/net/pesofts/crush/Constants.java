package net.pesofts.crush;

public class Constants {
    public static final String EXTRA_RECEIVER = "extra.receiver";
    public static final String EXTRA_RESULT_DATA = "extra.result.data";
    // TODO : 배포시 끌것
    public static boolean ENABLE_LOG = false;

    //    public static final int MAX_RETRY_TIME = 3;
    public static final int HTTP_CONNECTION_TIME_OUT = 10000;
//    public static final int HTTP_READ_TIME_OUT = 10000;
//    public static final int IMAGE_HTTP_TIMEOUT = 5000;

    public static final int BUCHI_COUNT_FOR_SHOW_MORE_PERSON = 30;
    public static final int BUCHI_COUNT_FOR_SHOW_MORE_POPULAR_PERSON = 50;
    public static final int BUCHI_COUNT_FOR_UNLOCK_CHAT = 30;
    public static final int BUCHI_COUNT_FOR_LIKE = 5;
    public static final int BUCHI_COUNT_FOR_OPEN_HIDDEN_CARD = 5;
    public static final int BUCHI_COUNT_FOR_MORE_PICTURE = 1;
    public static final int BUCHI_COUNT_FOR_LIKE_AGAIN = 15;
    public static final int BUCHI_COUNT_FOR_LIKE_PAST_CARD = 15;

    public static final String PESOFT_EMAIL = "mailto:help.ablesquare@gmail.com";
    public static final String NOTICE_URL = "http://devpesoft.cafe24.com/notice";
    public static final String PRIVATE_RULE_URL = "https://www.ablesquare.co.kr/clause/private";
    public static final String SERVICE_RULE_URL = "https://www.ablesquare.co.kr/clause/service";

    /**
     * 암호화된 Sendbird app ID
     */
    public static byte[] a={
            95,116,13,118,7,115,107,16,8,96,
            15,114,126,11,30,22,125,88,21,87,
            112,32,113,120,10,111,6,17,125,103,
            127,20,9,100,107,118
    };
    public static String key0 = "$OUIRO&^4"; //3번
    public static String key1 = "8o6f4UI^E"; //2번
    public static String key2 = "g75765*&%"; //0번
    public static String key3 = "$KJG&*$;o"; //1번

//    public static final String SENDBIRD_APP_ID_DEV = "5A9B46EE-CD02-488D-864E-1471E06F771A";

    public static final String CHAT_LOCK = "MSG_BLOCK";
    public static final String CHAT_UNLOCK = "MSG_UNBLOCK";

//    public static final String SERVER_DOMAIN = "http://52.79.122.43";
    public static final String SERVER_DOMAIN = "https://www.ablesquare.co.kr"; // real
//    public static final String SERVER_DOMAIN = "http://dev.ablesquare.co.kr";    // for DEV
    public static final String IMAGE_SERVER_URL = "http://pesofts-image.s3.amazonaws.com/";
    public static final String SYSTEM_CHECK_URL = SERVER_DOMAIN + "/systemCheck";
    public static final String SIGN_UP_URL = SERVER_DOMAIN + "/account/signup";
    public static final String SIGN_IN_URL = SERVER_DOMAIN + "/account/signin";
    public static final String TODAY_CARD_LIST_URL = SERVER_DOMAIN + "/card/list/today";
    public static final String TODAY_CARD_LIST_V2_URL = SERVER_DOMAIN + "/card/list/v2/today";
    public static final String TODAY_CARD_AGE = SERVER_DOMAIN + "/profile/matchTodayAge";
    public static final String TODAY_CARD_OPEN_MORE_URL = SERVER_DOMAIN + "/card/list/openMoreTodayList";
    public static final String TODAY_CARD_OPEN_POPULAR_MORE_URL = SERVER_DOMAIN + "/card/list/openExcellentTodayList";
//    public static final String TODAY_CARD_OPEN_REGION_MORE_URL = SERVER_DOMAIN + "/card/list/openLocalTodayList";
    public static final String CARD_LIST_URL = SERVER_DOMAIN + "/card/list/likefavor";
    public static final String LIKE_ME_CARD_LIST_URL = SERVER_DOMAIN + "/card/list/liked";
    public static final String I_LIKE_CARD_LIST_URL = SERVER_DOMAIN + "/card/list/like";
    public static final String I_FAVOR_CARD_LIST_URL = SERVER_DOMAIN + "/card/list/favor";
    public static final String FAVOR_ME_CARD_LIST_URL = SERVER_DOMAIN + "/card/list/hiddenfavored";
    public static final String OPEN_HIDDEN_FAVORED_URL = SERVER_DOMAIN + "/card/list/openHiddenFavored";
    public static final String FAVOR_MATCH_CARD_LIST_URL = SERVER_DOMAIN + "/card/list/favormatch";
    public static final String HISTORY_CARD_LIST_URL = SERVER_DOMAIN + "/card/list/history";
    public static final String EVALUATE_CARD_LIST_URL = SERVER_DOMAIN + "/card/list/evaluate";
    public static final String EVALUATE_USER_URL = SERVER_DOMAIN + "/card/evaluate";
    public static final String LIKE_CARD_URL = SERVER_DOMAIN + "/card/like";
    public static final String REPLY_LIKE_CARD_URL = SERVER_DOMAIN + "/card/reply";
    public static final String REPLY_FAVOR_CARD_URL = SERVER_DOMAIN + "/card/replyfavor";
    public static final String USER_DETAIL_URL = SERVER_DOMAIN + "/card/view";
    public static final String USER_OPEN_IMAGE_URL = SERVER_DOMAIN + "/card/openImage";
    public static final String EDIT_PROFILE_URL = SERVER_DOMAIN + "/profile/edit";
    public static final String UPDATE_PROFILE_URL = SERVER_DOMAIN + "/profile/update";
    public static final String PROFILE_CODE_URL = SERVER_DOMAIN + "/profile/codeInfo";
    public static final String CARD_UPDATE_INFO_URL = SERVER_DOMAIN + "/card/list/notification";
    public static final String MY_INFO_URL = SERVER_DOMAIN + "/account/myinfo";
    public static final String CHAT_UNLOCK_URL = SERVER_DOMAIN + "/chat/unlock";
    public static final String CHAT_LEAVE_URL = SERVER_DOMAIN + "/chat/leave";
    public static final String IMAGE_UPLOAD_INFO_URL = SERVER_DOMAIN + "/profile/uploadImage/s3";
    public static final String POINT_LOG_HISTORY_URL = SERVER_DOMAIN + "/point/loglist";
    public static final String SIGN_OUT_URL = SERVER_DOMAIN + "/account/signout";
    public static final String PAYMENT_URL = SERVER_DOMAIN + "/point/payment/android";
    public static final String SMS_INVITE_CHECK_URL = SERVER_DOMAIN + "/point/smsinvite/check";
    public static final String SMS_INVITE_URL = SERVER_DOMAIN + "/point/smsinvite";
    public static final String STORE_REVIEW_URL = SERVER_DOMAIN + "/point/storereview";

    public static final String URL_STORY_RANDOM_VOICE = SERVER_DOMAIN + "/voice/random/one";
    public static final String URL_STORY_STORE_LIST = SERVER_DOMAIN + "/voice/chat/rooms";
    public static final String URL_STORY_VOICE_UPLOAD = SERVER_DOMAIN + "/voice/uploadVoice/s3";
    public static final String URL_STORY_REPLY_START = SERVER_DOMAIN + "/voice/replyStart/record";
    public static final String URL_STORY_REPLY = SERVER_DOMAIN + "/voice/reply/record";
    public static final String URL_STORY_REPLY_PAY = SERVER_DOMAIN + "/voice/reply/record/buzzie";
    public static final String URL_STORY_NEW_START = SERVER_DOMAIN + "/voice/start/record";

    public static final String URL_STORY_PASS = SERVER_DOMAIN + "/voice/pass";
    public static final String URL_STORY_PASS_ROOM = SERVER_DOMAIN + "/voice/passRoom";
    public static final String URL_STORY_VOICE_CHAT = SERVER_DOMAIN + "/voice/chat/voices";
    public static final String URL_STORY_CHAT_CHECK = SERVER_DOMAIN + "/voice/chat/room/check";
}
