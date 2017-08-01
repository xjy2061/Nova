package org.xjy.android.nova.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DigitalFormatUtils {

    public String getLimitCount(long count, int digits) {
        long restrict = (long) Math.pow(10, digits);
        return count >= restrict ? (restrict - 1) + "+" : count + "";
    }

    public static String getHumanReadableByteCount(long bytes, int accuracy) {
        if (bytes < 1024)
            return bytes + "B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%." + accuracy + "f%s", bytes / Math.pow(1024, exp), pre);
    }

    private String formatCount(long count) {
        if (count < 10000) {
            return count + "";
        } else if (count < 10000000) {
            DecimalFormat format = new DecimalFormat("#####.#万");
            format.setRoundingMode(RoundingMode.FLOOR);
            return format.format(count / 10000.0);
        } else if (count < 100000000) {
            return count / 10000 + "万";
        } else {
            DecimalFormat format = new DecimalFormat("#####.#亿");
            format.setRoundingMode(RoundingMode.FLOOR);
            return format.format(count / 100000000.0);
        }
    }
}
