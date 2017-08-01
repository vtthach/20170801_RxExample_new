package com.sf0404.rxexample.rxexample;


import android.util.Log;

import java.util.Locale;

public class MyLog {
    private static final String MY_TAG = "MY_LOG";


    public static void log(String msg) {
        log("", msg);
    }

    public static void log(String tag, String msg) {
        Log.i(MY_TAG, String.format(Locale.getDefault(), "[Thread name: %s] %s-%s ", getCurrentThreadName(), tag, msg));
    }

    private static String getCurrentThreadName() {
        return Thread.currentThread().getName();
    }
}
