package com.lilong.skinchange.demo;

import com.lilong.skinchange.manager.SkinManager;

import android.app.Application;
import android.util.Log;

/**
 * Created by Administrator on 2016/9/3.
 */
public class MainApplication extends Application {

    SkinManager skinManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.currentThread().setUncaughtExceptionHandler(new MainUncaughtExceptionHandler());
        skinManager = SkinManager.getInstance(this);
        skinManager.loadSkinApksFromAsset(this);
    }

    private class MainUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private static final String TAG = "UncaughtException";

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
        }
    }

}
