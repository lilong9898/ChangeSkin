package com.lilong.skinchange.base;

import com.lilong.skinchange.manager.SkinManager;

import android.app.Application;
import android.util.Log;

/**
 * skinizable application should inherit this base class
 */
public class SkinApplication extends Application {

    private SkinManager skinManager;

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
