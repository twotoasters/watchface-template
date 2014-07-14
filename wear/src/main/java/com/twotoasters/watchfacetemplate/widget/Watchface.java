package com.twotoasters.watchfacetemplate.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.twotoasters.watchface.gears.widget.IWatchface;
import com.twotoasters.watchface.gears.widget.Watch;
import com.twotoasters.watchfacetemplate.R;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class Watchface extends FrameLayout implements IWatchface {

    @InjectView(R.id.face)              ImageView face;
    @InjectView(R.id.shadow_overlay)    ImageView shadowOverlay;
    @InjectView(R.id.hand_hour)         ImageView handHour;
    @InjectView(R.id.hand_minute)       ImageView handMinute;
    @InjectView(R.id.hand_second)       ImageView handSecond;

    private Watch mWatch;

    private boolean mInflated;
    private boolean mActive;

    public Watchface(Context context) {
        super(context);
        init(context, null, 0);
    }

    public Watchface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public Watchface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @DebugLog
    private void init(Context context, AttributeSet attrs, int defStyle) {
        mWatch = new Watch(this);
    }

    @DebugLog
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this, getRootView());
        mInflated = true;
    }

    @DebugLog
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWatch.onAttachedToWindow();
    }

    @DebugLog
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWatch.onDetachedFromWindow();
    }

    private void rotateHands(int hour, int minute, int second) {
        int rotHr = (int) (30 * hour + 0.5f * minute);
        int rotMin = 6 * minute;
        int rotSec = 6 * second;

        handHour.setRotation(rotHr);
        handMinute.setRotation(rotMin);
        handSecond.setRotation(rotSec);
    }

    @Override
    public void onTimeChanged(Calendar time) {
        Timber.v("onTimeChanged()");

        int hr = time.get(Calendar.HOUR_OF_DAY) % 12;
        int min = time.get(Calendar.MINUTE);
        int sec = time.get(Calendar.SECOND);

        rotateHands(hr, min, sec);
        invalidate();
    }

    @Override
    @DebugLog
    public void onActiveStateChanged(boolean active) {
        this.mActive = active;
        setImageResources();
    }

    @DebugLog
    private void setImageResources() {
        if (mInflated) {
            face.setImageResource(mActive ? R.drawable.watch_bg_normal : R.drawable.watch_bg_dimmed);
            shadowOverlay.setImageResource(mActive ? R.drawable.overlay_shadow_normal : R.drawable.overlay_shadow_dimmed);
            handHour.setImageResource(mActive ? R.drawable.hand_hour_normal : R.drawable.hand_hour_dimmed);
            handMinute.setImageResource(mActive ? R.drawable.hand_minute_normal : R.drawable.hand_minute_dimmed);
            handSecond.setImageResource(mActive ? R.drawable.hand_second_normal : R.drawable.hand_second_dimmed);
            handSecond.setVisibility(mActive ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private Typeface loadTypeface(int typefaceNameResId) {
        String typefaceName = getResources().getString(typefaceNameResId);
        return Typeface.createFromAsset(getContext().getAssets(), typefaceName);
    }
}
