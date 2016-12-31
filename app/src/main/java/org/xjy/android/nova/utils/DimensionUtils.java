package org.xjy.android.nova.utils;


import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.xjy.android.nova.NovaApplication;

public class DimensionUtils {
    public static final DisplayMetrics DISPLAY_METRICS = NovaApplication.getInstance().getResources().getDisplayMetrics();
    public static final float DENSITY = DISPLAY_METRICS.density;

    public static int dpToPx(float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, DISPLAY_METRICS) + 0.5);
    }

    public static float pxToDp(int px) {
        return px / DENSITY;
    }
}
