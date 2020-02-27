package net.pesofts.crush.Util;

import java.util.Calendar;
import java.util.Locale;

public class DateUtil {

    public static String getAgeString(String birthday) {
        int age = calculateAge(birthday);
        if (age > 0) {
            return calculateAge(birthday) + "세";
        } else {
            return "";
        }
    }


    public static int calculateAge(String birthday) {
        try {
            Calendar now = Calendar.getInstance();

            int year = now.get(Calendar.YEAR);
            int birthYear = Integer.parseInt(birthday.substring(0, 4));

            return year - birthYear + 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isPastTime(long expireTime) {
        long diffTime = expireTime - System.currentTimeMillis();
        if (diffTime >= 0) {
            return false;
        } else {
            return true;
        }
    }

    public static String getRemainDayString(long expireTime) {
        String remainTime = "";

        if (isPastTime(expireTime)) {
            long diffTime = (System.currentTimeMillis() - expireTime) / 1000;
            int day = (int) (diffTime / (24 * 60 * 60));
            remainTime = (day + 1) + "일전";
        } else {
            remainTime = "오늘";
        }

        return remainTime;
    }

    public static String getRemainHourAndMinuteString(long expireTime) {
        String remainTime = "";

        long diffTime = (expireTime - System.currentTimeMillis()) / 1000;
        if (diffTime >= 0) {
            diffTime = diffTime % (24 * 60 * 60);
            int diffHour = (int) diffTime / (60 * 60);
            diffTime = diffTime % (60 * 60);
            int diffMin = (int) diffTime / 60;

            remainTime = String.format(Locale.KOREA, "다음 매칭까지 %02d시간 %02d분 남음", diffHour, diffMin);
        }

        return remainTime;
    }

    public static String getRemainHour(long expireTime) {
//        expireTime += 9 * 60 * 60 * 1000; // GMT +9
        String remainTime = "";

        if (isPastTime(expireTime)) {
            long diffTime = (System.currentTimeMillis() - expireTime) / 1000;
            int day = (int) (diffTime / (24 * 60 * 60));

            diffTime = diffTime % (24 * 60 * 60);
            int diffHour = (int) diffTime / (60 * 60);
            diffTime = diffTime % (60 * 60);
            int diffMin = (int) diffTime / 60;

            StringBuilder stringBuilder = new StringBuilder();
            if (day > 0) {
                return stringBuilder.append(day).append("일전").toString();
            }
            if (diffHour > 0) {
                return stringBuilder.append(diffHour).append("시간전").toString();
            }
            stringBuilder.append(diffMin).append("분전");

            remainTime = stringBuilder.toString();
        }

        return remainTime;
    }

    public static String getRemainHourAndMinute(long expireTime) {
        expireTime += 9 * 60 * 60 * 1000; // GMT +9
        String remainTime = "";

        if (isPastTime(expireTime)) {
            long diffTime = (System.currentTimeMillis() - expireTime) / 1000;
            int day = (int) (diffTime / (24 * 60 * 60));

            diffTime = diffTime % (24 * 60 * 60);
            int diffHour = (int) diffTime / (60 * 60);
            diffTime = diffTime % (60 * 60);
            int diffMin = (int) diffTime / 60;

            StringBuilder stringBuilder = new StringBuilder();
            if (day > 0) {
                return stringBuilder.append(day).append("일전").toString();
            }
            if (diffHour > 0) {
                return stringBuilder.append(diffHour).append("시간전").toString();
            }
            stringBuilder.append(diffMin).append("분전");

            remainTime = stringBuilder.toString();
        }

        return remainTime;
    }

    public static String getRemainHourAndMinuteAndSec(long expireTime) {
        String remainTime = "";

        long diffTime = (expireTime - System.currentTimeMillis()) / 1000;
        if (diffTime > 0) {
            diffTime = diffTime % (24 * 60 * 60);
            int diffHour = (int) diffTime / (60 * 60);
            diffTime = diffTime % (60 * 60);
            int diffMin = (int) diffTime / 60;
            int diffSec = (int) diffTime % 60;

            remainTime = String.format("%02d", diffHour) + ":" + String.format("%02d", diffMin) + ":" + String.format("%02d", diffSec);
        } else {
            remainTime = "0";
        }

        return remainTime;
    }

    public static int getProgressByRemainTime(long expireTime) {
        int diffTime = (int) (expireTime - System.currentTimeMillis()) / 1000;
        int refreshTime = 4 * 3600;
        if (refreshTime <= diffTime) {
            return 100;
        } else {
            return (refreshTime - diffTime) * 100 / refreshTime;
        }

    }

    public static String getRemainDay(long expireTime) {
        long remainTime = 0;

        long diffTime = (expireTime - System.currentTimeMillis()) / 1000;
        if (diffTime >= 0) {
            diffTime = diffTime / (24 * 60 * 60);

            remainTime = diffTime;
        }

        return Long.toString(remainTime);
    }

}
