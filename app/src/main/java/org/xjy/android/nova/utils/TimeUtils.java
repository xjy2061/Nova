package org.xjy.android.nova.utils;

import android.text.format.DateUtils;

import java.util.Locale;

public class TimeUtils {
    public static String formatDuration(long duration) {
        if (duration >= DateUtils.HOUR_IN_MILLIS) {
            return String.format(Locale.US, "%d:%02d:%02d", duration / DateUtils.HOUR_IN_MILLIS, duration % DateUtils.HOUR_IN_MILLIS / DateUtils.MINUTE_IN_MILLIS, duration % DateUtils.MINUTE_IN_MILLIS / DateUtils.SECOND_IN_MILLIS);
        } else {
            return String.format(Locale.US, "%02d:%02d", duration / DateUtils.MINUTE_IN_MILLIS, duration % DateUtils.MINUTE_IN_MILLIS / DateUtils.SECOND_IN_MILLIS);
        }
    }
}
