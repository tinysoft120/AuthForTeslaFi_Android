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

    public static int e(String tag, String errMsg, Throwable th) {
        //String logMsg = DATE_FORMAT.format(new Date()) + " E/" + tag + ": " + errMsg + '\n' + Log.getStackTraceString(th);
        //FirebaseCrashlytics.getInstance().log(logMsg);
        return Log.e(tag, errMsg, th);
    }

    public static int i(String tag, String msg) {
        //String logMsg = DATE_FORMAT.format(new Date()) + " I/" + tag + ": " + msg;
        //FirebaseCrashlytics.getInstance().log(logMsg);
        return Log.i(tag, msg);
    }

    public static int d(String tag, String msg) {
        //String logMsg = DATE_FORMAT.format(new Date()) + " D/" + tag + ": " + msg;
        //FirebaseCrashlytics.getInstance().log(logMsg);
        return Log.d(tag, msg);
    }

    public static int w(String tag, String msg) {
        //String logMsg = DATE_FORMAT.format(new Date()) + " W/" + tag + ": " + msg;
        //FirebaseCrashlytics.getInstance().log(logMsg);
        return Log.w(tag, msg);
    }
}
