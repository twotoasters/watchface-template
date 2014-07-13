package com.twotoasters.watchfacetemplate.activity;

import android.app.Activity;
import android.os.Bundle;

import com.twotoasters.watchfacetemplate.R;
import com.twotoasters.watchfacetemplate.widget.IWatchface;
import com.twotoasters.watchfacetemplate.widget.Watchface;

public class WatchfaceActivity extends Activity {

    private IWatchface watchface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watchface);
        watchface = (Watchface) findViewById(R.id.watchface);
    }

    @Override
    protected void onResume() {
        super.onResume();
        watchface.onActiveStateChanged(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        watchface.onActiveStateChanged(false);
    }
}
