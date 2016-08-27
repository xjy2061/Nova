package org.xjy.android.nova.transfer.utils;

import android.util.Pair;

public class TransferUtils {
    public static long parcelInt(int first, int second) {
        return ((long) first) << 32 | (second & 0xffffffffL);
    }

    public static Pair<Integer, Integer> unParcelInt(long parcel) {
        return new Pair<Integer, Integer>((int) (parcel >> 32), (int) parcel);
    }
}
