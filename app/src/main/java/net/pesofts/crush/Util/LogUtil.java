package net.pesofts.crush.Util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import net.pesofts.crush.Constants;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by Administrator on 2015-11-26.
 */
public class LogUtil {
    public static final String TAG = "Crush";

    public static void e(Context context, Object... args) {
        if (args == null || args.length == 0) {
            e(TAG, context);
        } else {
            if (Constants.ENABLE_LOG) {
                Object[] newArgs = new Object[args.length + 1];
                newArgs[0] = context;
                for (int i = 0; i < args.length; i++) {
                    newArgs[i + 1] = args[i];
                }
                e(TAG, newArgs);
            }
        }
    }

    public static void d(String debugMessage) {
        d(TAG, debugMessage);
    }

    public static void e(Throwable tr) {
        e(TAG, getStackTraceString(tr));
    }

    public static void v(String tag, Object... args) {
        println(Log.VERBOSE, tag, args);
    }

    public static void d(String tag, Object... args) {
        println(Log.DEBUG, tag, args);
    }

    public static void i(String tag, Object... args) {
        println(Log.INFO, tag, args);
    }

    public static void w(String tag, Object... args) {
        println(Log.WARN, tag, args);
    }

    public static void e(String tag, Object... args) {
        println(Log.ERROR, tag, args);
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) return "";
        return Log.getStackTraceString(tr);
    }

    /**
     * 실제로 로그를 찍는 부분.
     *
     * @param level
     * @param tag
     * @param args
     */
    private static void println(int level, String tag, Object... args) {
        if (Constants.ENABLE_LOG) {
            if (tag != null && args != null && args.length > 0) {
                StringBuffer sb = new StringBuffer();
                StringBuffer sbLater = null; // Intent와 Throwable 은 뒤쪽에 좀 자세히 출력되도록 수정했다.
                for (Object arg : args) {
                    if (arg == null) {
                        if (sb.length() > 0) {
                            sb.append(' ');
                        }
                        sb.append("null");
                    } else if (arg instanceof Context) {
                        if (sb.length() > 0) {
                            sb.append(' ');
                        }
                        sb.append("[");
                        sb.append(arg.getClass().getSimpleName());
                        sb.append("]");
                    } else if (arg instanceof Intent) {
                        /* Intent 는 그 내용들을 자세히 출력한다. */
                        if (sbLater == null) {
                            sbLater = new StringBuffer();
                        }
                        Intent intent = (Intent) arg;
                        sbLater.append(printIntent(intent));
                    } else if (arg instanceof Throwable) {
						/* Exception 은 CallStack 을 전부 출력해준다. */
                        Throwable tr = (Throwable) arg;
                        if (tr.getMessage() != null) {
                            if (sb.length() > 0) {
                                sb.append(' ');
                            }
                            sb.append(tr.getMessage());
                        }
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        LogUtil.e(LogUtil.TAG, pw.toString());
                        if (sbLater == null) {
                            sbLater = new StringBuffer();
                        }
                        sbLater.append(sw.toString());
                    } else if (arg instanceof Calendar) {
                        if (sb.length() > 0) {
                            sb.append(' ');
                        }
                        sb.append(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(((Calendar) arg).getTime()));
                    } else {
                        if (sb.length() > 0) {
                            sb.append(' ');
                        }
                        sb.append(arg.toString());
                    }
                }

                if (sb != null) {
                    Log.println(level, tag, sb.toString());
                }
                if (sbLater != null) {
                    Log.println(level, tag, sbLater.toString());
                }

            }
        }
    }

    public static String printIntent(Intent intent) {
        StringBuilder sbLater = new StringBuilder();
        sbLater.append("Intent action:" + intent.getAction());
        sbLater.append("\n   type:" + intent.getType());
        sbLater.append("\n   data:" + intent.getData());
        sbLater.append("\n   extra:");
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Iterator<String> keys = extras.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                sbLater.append(" " + key + "=>" + extras.get(key));
            }
        }
        return sbLater.toString();
    }
}
