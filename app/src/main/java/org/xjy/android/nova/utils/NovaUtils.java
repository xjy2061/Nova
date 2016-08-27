package org.xjy.android.nova.utils;

import android.util.TypedValue;

import org.xjy.android.nova.NovaApplication;

public class NovaUtils {

    public static int dpToPx(float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, NovaApplication.getInstance().getResources().getDisplayMetrics()) + 0.5);
    }

    public static float pxToDp(int px) {
        return px / NovaApplication.getInstance().getResources().getDisplayMetrics().density;
    }
}
