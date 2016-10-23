package org.xjy.android.nova.utils;

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
}
