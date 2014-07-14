package com.twotoasters.watchfacetemplate;

import com.twotoasters.watchface.gears.GearsWatchfaceApp;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class WatchfaceApp extends GearsWatchfaceApp {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new DebugTree());
    }
}
