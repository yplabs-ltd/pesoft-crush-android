package net.pesofts.crush.Util;

import android.text.SpannableString;

public class StringUtil {
    public static final int EMPTY_INT = 0;
    public static final String EMPTY_STRING = "";

    public static String checkEmpty(String str) {
        return checkEmpty(str, EMPTY_STRING);
    }

    public static String checkEmpty(String str, String defaultString) {
        if (str == null || "".equals(str)) {
            return defaultString;
        }
        return str.trim();
    }

    public static int checkInt(String str) {
        return checkInt(str, EMPTY_INT);
    }

    public static int checkInt(String str, int defaultInt) {
        if (str == null) {
            return defaultInt;
        }

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return defaultInt;
        }
    }

    public static long checkLong(String str, long defaultLong) {
        if (str == null) {
            return defaultLong;
        }

        try {
            return Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return defaultLong;
        }
    }

    public static boolean isEmpty(String str) {
        return (str == null) || (str.trim().length() == 0);
    }

    public static boolean isEmpty(SpannableString str) {
        return ((str == null) || isEmpty(str.toString()));
    }

    public static boolean isNotEmpty(SpannableString str) {
        return !isEmpty(str);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isEmpty(StringBuilder sb) {
        return (sb == null || isEmpty(sb.toString()));
    }

    public static boolean isNotEmpty(StringBuilder sb) {
        return !isEmpty(sb);
    }

    public static boolean isEmpty(CharSequence str) {
        return ((str == null) || str.toString().trim().length() == 0);
    }

    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

}
