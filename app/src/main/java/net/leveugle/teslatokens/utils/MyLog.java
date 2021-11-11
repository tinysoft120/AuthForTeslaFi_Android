package net.leveugle.teslatokens.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyLog {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    private static final int MAX_BUFFER_LINES = 5000;
    private static List<String> buffer = new ArrayList<>();
    public static boolean debugEnabled = false;
    private static final String keyDebugEnabled = "keyDebugEnabled";
    private static final String sharedPrefName = "net.leveugle.teslatokens.MyLog";

    public static int e(String tag, String msg) {
        String s = DATE_FORMAT.format(new Date()) + " E/" + tag + ": " + msg;
        if (debugEnabled) {
            buffer.add(s);
        }
        //FirebaseCrashlytics.getInstance().log(s);
        return Log.e(tag, msg);
    }

    public static int e(String str, String str2, Throwable th) {
        String str3 = DATE_FORMAT.format(new Date()) + " E/" + str + ": " + str2 + '\n' + Log.getStackTraceString(th);
        if (debugEnabled) {
            buffer.add(str3);
        }
        //FirebaseCrashlytics.getInstance().log(str3);
        return Log.e(str, str2, th);
    }

    /* renamed from: i */
    public static int i(String str, String str2) {
        String str3 = DATE_FORMAT.format(new Date()) + " I/" + str + ": " + str2;
        if (debugEnabled) {
            buffer.add(str3);
        }
        //FirebaseCrashlytics.getInstance().log(str3);
        return Log.i(str, str2);
    }

    /* renamed from: d */
    public static int d(String str, String str2) {
        String str3 = DATE_FORMAT.format(new Date()) + " D/" + str + ": " + str2;
        if (debugEnabled) {
            buffer.add(str3);
        }
        //FirebaseCrashlytics.getInstance().log(str3);
        return Log.d(str, str2);
    }

    public static int w(String str, String str2) {
        String str3 = DATE_FORMAT.format(new Date()) + " W/" + str + ": " + str2;
        if (debugEnabled) {
            buffer.add(str3);
        }
        //FirebaseCrashlytics.getInstance().log(str3);
        return Log.w(str, str2);
    }
}
