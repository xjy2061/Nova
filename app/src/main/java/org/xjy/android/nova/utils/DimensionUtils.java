package org.xjy.android.nova.utils;


import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.xjy.android.nova.NovaApplication;

public class DimensionUtils {
    public static final DisplayMetrics DISPLAY_METRICS;
    public static final float DENSITY;
    public static final int SCREEN_WIDTH_PORTRAIT;
    public static final int SCREEN_HEIGHT_PORTRAIT;

    static {
        Resources resources = NovaApplication.getInstance().getResources();
        boolean portrait = resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        DISPLAY_METRICS = resources.getDisplayMetrics();
        DENSITY = DISPLAY_METRICS.density;
        SCREEN_WIDTH_PORTRAIT = portrait ? DISPLAY_METRICS.widthPixels : DISPLAY_METRICS.heightPixels;
        SCREEN_HEIGHT_PORTRAIT = portrait ? DISPLAY_METRICS.heightPixels : DISPLAY_METRICS.widthPixels;
    }

    public static float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, DISPLAY_METRICS);
    }

    public static int dpToIntPx(float dp) {
        return (int) (dpToPx(dp) + 0.5);
    }

    public static float pxToDp(int px) {
        return px / DENSITY;
    }
}
