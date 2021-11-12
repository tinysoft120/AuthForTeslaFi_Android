package com.teslafi.authandroid.utils;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MyLog {
    //private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

    public static int e(String tag, String msg) {
        //String logMsg = DATE_FORMAT.format(new Date()) + " E/" + tag + ": " + msg;
        //FirebaseCrashlytics.getInstance().log(logMsg);
        return Log.e(tag, msg);
    }

    public static int e(String str, String str2, Throwable th) {
        //String logMsg = DATE_FORMAT.format(new Date()) + " E/" + str + ": " + str2 + '\n' + Log.getStackTraceString(th);
        //FirebaseCrashlytics.getInstance().log(logMsg);
        return Log.e(str, str2, th);
    }

    public static int i(String str, String str2) {
        //String logMsg = DATE_FORMAT.format(new Date()) + " I/" + str + ": " + str2;
        //FirebaseCrashlytics.getInstance().log(logMsg);
        return Log.i(str, str2);
    }

    public static int d(String str, String str2) {
        //String logMsg = DATE_FORMAT.format(new Date()) + " D/" + str + ": " + str2;
        //FirebaseCrashlytics.getInstance().log(logMsg);
        return Log.d(str, str2);
    }

    public static int w(String tag, String msg) {
        //String logMsg = DATE_FORMAT.format(new Date()) + " W/" + tag + ": " + msg;
        //FirebaseCrashlytics.getInstance().log(logMsg);
        return Log.w(tag, msg);
    }
}
