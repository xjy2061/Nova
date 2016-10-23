package org.xjy.android.nova.utils;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NovaDateUtils extends DateUtils {

    public static String formatDate(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        return format.format(new Date(millis));
    }
}
