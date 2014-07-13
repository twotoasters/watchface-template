package com.twotoasters.watchfacetemplate.widget;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.ViewDebug.ExportedProperty;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Watch {

    /**
     * The default formatting pattern in 12-hour mode. This pattern is used
     * if {@link #setFormat12Hour(CharSequence)} is called with a null pattern
     * or if no pattern was specified when creating an instance of this class.
     *
     * This default pattern shows only the time, hours and minutes, and an am/pm
     * indicator.
     *
     * @see #setFormat12Hour(CharSequence)
     * @see #getFormat12Hour()
     *
     * @deprecated Let the system use locale-appropriate defaults instead.
     */
    public static final CharSequence DEFAULT_FORMAT_12_HOUR = "h:mm:ss a";

    /**
     * The default formatting pattern in 24-hour mode. This pattern is used
     * if {@link #setFormat24Hour(CharSequence)} is called with a null pattern
     * or if no pattern was specified when creating an instance of this class.
     *
     * This default pattern shows only the time, hours and minutes.
     *
     * @see #setFormat24Hour(CharSequence)
     * @see #getFormat24Hour()
     *
     * @deprecated Let the system use locale-appropriate defaults instead.
     */
    public static final CharSequence DEFAULT_FORMAT_24_HOUR = "H:mm:ss";

    private CharSequence mFormat12;
    private CharSequence mFormat24;

    @ExportedProperty
    private CharSequence mFormat;
    @ExportedProperty
    private boolean mHasSeconds;

    private boolean mAttached;

    private Calendar mTime;
    private String mTimeZone;

    private final ContentObserver mFormatChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            chooseFormat();
            onTimeChanged();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            chooseFormat();
            onTimeChanged();
        }
    };

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mTimeZone == null && Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                final String timeZone = intent.getStringExtra("time-zone");
                createTime(timeZone);
            }
            onTimeChanged();
        }
    };

    private final Runnable mTicker = new Runnable() {
        public void run() {
            onTimeChanged();

            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);

            if (hasWatchface())
                getWatchface().getHandler().postAtTime(mTicker, next);
        }
    };

    private WeakReference<IWatchface> watchfaceRef;

    public Watch(IWatchface watchface) {
        if (watchface == null) {
            throw new AssertionError("Watchface can not be null");
        }

        watchfaceRef = new WeakReference<IWatchface>(watchface);
        init(watchface);
    }


    private void init(@NonNull IWatchface watchface) {
        if (mFormat12 == null || mFormat24 == null) {
            Locale locale = watchface.getResources().getConfiguration().locale;
            if (mFormat12 == null) {
                mFormat12 = DEFAULT_FORMAT_12_HOUR;
            }
            if (mFormat24 == null) {
                mFormat24 = DEFAULT_FORMAT_24_HOUR;
            }
        }

        createTime(mTimeZone);
        // Wait until onAttachedToWindow() to handle the ticker
        chooseFormat(false);
    }

    private void createTime(String timeZone) {
        if (timeZone != null) {
            mTime = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        } else {
            mTime = Calendar.getInstance();
        }
    }

    /**
     * Returns the formatting pattern used to display the date and/or time
     * in 12-hour mode. The formatting pattern syntax is described in
     * {@link android.text.format.DateFormat}.
     *
     * @return A {@link CharSequence} or null.
     *
     * @see #setFormat12Hour(CharSequence)
     * @see #is24HourModeEnabled()
     */
    @ExportedProperty
    public CharSequence getFormat12Hour() {
        return mFormat12;
    }

    /**
     * <p>Specifies the formatting pattern used to display the date and/or time
     * in 12-hour mode. The formatting pattern syntax is described in
     * {@link android.text.format.DateFormat}.</p>
     *
     * <p>If this pattern is set to null, {@link #getFormat24Hour()} will be used
     * even in 12-hour mode. If both 24-hour and 12-hour formatting patterns
     * are set to null, the default pattern for the current locale will be used
     * instead.</p>
     *
     * <p><strong>Note:</strong> if styling is not needed, it is highly recommended
     * you supply a format string generated by
     * {@link android.text.format.DateFormat#getBestDateTimePattern(java.util.Locale, String)}. This method
     * takes care of generating a format string adapted to the desired locale.</p>
     *
     *
     * @param format A date/time formatting pattern as described in {@link android.text.format.DateFormat}
     *
     * @see #getFormat12Hour()
     * @see #is24HourModeEnabled()
     * @see android.text.format.DateFormat#getBestDateTimePattern(java.util.Locale, String)
     * @see android.text.format.DateFormat
     *
     * @attr ref android.R.styleable#TextClock_format12Hour
     */
    public void setFormat12Hour(CharSequence format) {
        mFormat12 = format;

        chooseFormat();
        onTimeChanged();
    }

    /**
     * Returns the formatting pattern used to display the date and/or time
     * in 24-hour mode. The formatting pattern syntax is described in
     * {@link android.text.format.DateFormat}.
     *
     * @return A {@link CharSequence} or null.
     *
     * @see #setFormat24Hour(CharSequence)
     * @see #is24HourModeEnabled()
     */
    @ExportedProperty
    public CharSequence getFormat24Hour() {
        return mFormat24;
    }

    /**
     * <p>Specifies the formatting pattern used to display the date and/or time
     * in 24-hour mode. The formatting pattern syntax is described in
     * {@link android.text.format.DateFormat}.</p>
     *
     * <p>If this pattern is set to null, {@link #getFormat24Hour()} will be used
     * even in 12-hour mode. If both 24-hour and 12-hour formatting patterns
     * are set to null, the default pattern for the current locale will be used
     * instead.</p>
     *
     * <p><strong>Note:</strong> if styling is not needed, it is highly recommended
     * you supply a format string generated by
     * {@link android.text.format.DateFormat#getBestDateTimePattern(java.util.Locale, String)}. This method
     * takes care of generating a format string adapted to the desired locale.</p>
     *
     * @param format A date/time formatting pattern as described in {@link android.text.format.DateFormat}
     *
     * @see #getFormat24Hour()
     * @see #is24HourModeEnabled()
     * @see android.text.format.DateFormat#getBestDateTimePattern(java.util.Locale, String)
     * @see android.text.format.DateFormat
     *
     * @attr ref android.R.styleable#TextClock_format24Hour
     */
    public void setFormat24Hour(CharSequence format) {
        mFormat24 = format;

        chooseFormat();
        onTimeChanged();
    }

    /**
     * Indicates whether the system is currently using the 24-hour mode.
     *
     * When the system is in 24-hour mode, this view will use the pattern
     * returned by {@link #getFormat24Hour()}. In 12-hour mode, the pattern
     * returned by {@link #getFormat12Hour()} is used instead.
     *
     * If either one of the formats is null, the other format is used. If
     * both formats are null, the default formats for the current locale are used.
     *
     * @return true if time should be displayed in 24-hour format, false if it
     *         should be displayed in 12-hour format.
     *
     * @see #setFormat12Hour(CharSequence)
     * @see #getFormat12Hour()
     * @see #setFormat24Hour(CharSequence)
     * @see #getFormat24Hour()
     */
    public boolean is24HourModeEnabled() {
        return hasWatchface() ? DateFormat.is24HourFormat(getWatchface().getContext()) : false;
    }

    /**
     * Indicates which time zone is currently used by this view.
     *
     * @return The ID of the current time zone or null if the default time zone,
     *         as set by the user, must be used
     *
     * @see TimeZone
     * @see java.util.TimeZone#getAvailableIDs()
     * @see #setTimeZone(String)
     */
    public String getTimeZone() {
        return mTimeZone;
    }

    /**
     * Sets the specified time zone to use in this clock. When the time zone
     * is set through this method, system time zone changes (when the user
     * sets the time zone in settings for instance) will be ignored.
     *
     * @param timeZone The desired time zone's ID as specified in {@link TimeZone}
     *                 or null to user the time zone specified by the user
     *                 (system time zone)
     *
     * @see #getTimeZone()
     * @see java.util.TimeZone#getAvailableIDs()
     * @see TimeZone#getTimeZone(String)
     *
     * @attr ref android.R.styleable#TextClock_timeZone
     */
    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;

        createTime(timeZone);
        onTimeChanged();
    }

    /**
     * Selects either one of {@link #getFormat12Hour()} or {@link #getFormat24Hour()}
     * depending on whether the user has selected 24-hour format.
     *
     * Calling this method does not schedule or unschedule the time ticker.
     */
    private void chooseFormat() {
        chooseFormat(true);
    }

    /**
     * Returns the current format string. Always valid after constructor has
     * finished, and will never be {@code null}.
     *
     * @hide
     */
    public CharSequence getFormat() {
        return mFormat;
    }

    /**
     * Selects either one of {@link #getFormat12Hour()} or {@link #getFormat24Hour()}
     * depending on whether the user has selected 24-hour format.
     *
     * @param handleTicker true if calling this method should schedule/unschedule the
     *                     time ticker, false otherwise
     */
    private void chooseFormat(boolean handleTicker) {
        final boolean format24Requested = is24HourModeEnabled();

        //LocaleData ld = LocaleData.get(getContext().getResources().getConfiguration().locale);

        if (format24Requested) {
            mFormat = abc(mFormat24, mFormat12, DEFAULT_FORMAT_12_HOUR);
        } else {
            mFormat = abc(mFormat12, mFormat24, DEFAULT_FORMAT_24_HOUR);
        }

        boolean hadSeconds = mHasSeconds;
        mHasSeconds = !TextUtils.isEmpty(mFormat) ? mFormat.toString().contains(String.valueOf(DateFormat.SECONDS)) : false;

        if (hasWatchface()) {
            if (handleTicker && mAttached && hadSeconds != mHasSeconds) {
                if (hadSeconds) getWatchface().getHandler().removeCallbacks(mTicker);
                else mTicker.run();
            }
        }
    }

    /**
     * Returns a if not null, else return b if not null, else return c.
     */
    private static CharSequence abc(CharSequence a, CharSequence b, CharSequence c) {
        return a == null ? (b == null ? c : b) : a;
    }

    protected void onAttachedToWindow() {
        if (!mAttached) {
            mAttached = true;

            registerReceiver();
            registerObserver();

            createTime(mTimeZone);

            if (mHasSeconds) {
                mTicker.run();
            } else {
                onTimeChanged();
            }
        }
    }

    protected void onDetachedFromWindow() {
        if (mAttached) {
            unregisterReceiver();
            unregisterObserver();

            if (hasWatchface())
                getWatchface().getHandler().removeCallbacks(mTicker);

            mAttached = false;
        }
    }

    private void registerReceiver() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        if (hasWatchface())
            getWatchface().getContext().registerReceiver(mIntentReceiver, filter, null, getWatchface().getHandler());
    }

    private void registerObserver() {
        if (hasWatchface()) {
            final Context context = getWatchface().getContext();
            final ContentResolver resolver = context.getContentResolver();
            resolver.registerContentObserver(Settings.System.CONTENT_URI, true, mFormatChangeObserver);
        }
    }

    private void unregisterReceiver() {
        if (hasWatchface())
            getWatchface().getContext().unregisterReceiver(mIntentReceiver);
    }

    private void unregisterObserver() {
        if (hasWatchface()) {
            final Context context = getWatchface().getContext();
            final ContentResolver resolver = context.getContentResolver();
            resolver.unregisterContentObserver(mFormatChangeObserver);
        }
    }

    private void onTimeChanged() {
        mTime.setTimeInMillis(System.currentTimeMillis());
        if (hasWatchface()) {
            getWatchface().onTimeChanged(mTime);
        }
    }

    private boolean hasWatchface() {
        return watchfaceRef != null && watchfaceRef.get() != null;
    }

    private IWatchface getWatchface() {
        return hasWatchface() ? watchfaceRef.get() : null;
    }
}
