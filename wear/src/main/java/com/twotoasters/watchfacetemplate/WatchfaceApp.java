package com.twotoasters.watchfacetemplate;

import android.app.Application;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class WatchfaceApp extends Application {

    private static WatchfaceApp app;

    public static WatchfaceApp getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        Timber.plant(new DebugTree());
    }
}
