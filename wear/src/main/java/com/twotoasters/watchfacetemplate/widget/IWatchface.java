package com.twotoasters.watchfacetemplate.widget;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;

import java.util.Calendar;

public interface IWatchface {

    // Implemented by the watchface
    public void onActiveStateChanged(boolean active);
    public void onTimeChanged(Calendar time);

    // Watchface should delegate these calls to the watch
    public void onAttachedToWindow();
    public void onDetachedFromWindow();

    // Watchfaces should extend view and get these for free
    public Context getContext();
    public Handler getHandler();
    public Resources getResources();
}
