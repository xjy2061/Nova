package org.xjy.android.nova.transfer.upload.core;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.facebook.common.util.ByteConstants;

import org.xjy.android.nova.NovaApplication;
import org.xjy.android.nova.utils.DeviceInfoUtils;

public class NetworkType {
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_MOBILE_SLOW = 1;
    public static final int TYPE_MOBILE_FAST = 2;
    public static final int TYPE_WIFI = 3;

    private int mType;
    private long mChunkSize;

    public NetworkType(int type, int chunkSize) {
        mType = type;
        mChunkSize = chunkSize;
    }

    public int getType() {
        return mType;
    }

    public long getChunkSize() {
        return mChunkSize;
    }

    public static NetworkType getNetworkType() {
        try {
            NetworkInfo networkInfo = DeviceInfoUtils.getNetworkInfo(NovaApplication.getInstance());
            if (networkInfo != null && networkInfo.isConnected()) {
                int type = networkInfo.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                    return new NetworkType(TYPE_WIFI, 4 * ByteConstants.MB);
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    int subType = networkInfo.getSubtype();
                    switch (subType) {
                        case TelephonyManager.NETWORK_TYPE_1xRTT:   // ~ 50-100 kbps
                        case TelephonyManager.NETWORK_TYPE_CDMA:    // ~ 14-64 kbps
                        case TelephonyManager.NETWORK_TYPE_EDGE:    // ~ 50-100 kbps
                        case TelephonyManager.NETWORK_TYPE_GPRS:    // ~ 100 kbps
                        case TelephonyManager.NETWORK_TYPE_IDEN:    // ~25 kbps
                            return new NetworkType(TYPE_MOBILE_SLOW, 512 * ByteConstants.KB);
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:  // ~ 400-1000 kbps
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:  // ~ 600-1400 kbps
                        case TelephonyManager.NETWORK_TYPE_UMTS:    // ~ 400-7000 kbps
                            return new NetworkType(TYPE_MOBILE_FAST, ByteConstants.MB);
                        case TelephonyManager.NETWORK_TYPE_HSPA:    // ~ 700-1700 kbps
                            return new NetworkType(TYPE_MOBILE_FAST, 2 * ByteConstants.MB);
                        case TelephonyManager.NETWORK_TYPE_EHRPD:   // ~ 1-2 Mbps
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:  // ~ 5 Mbps
                        case TelephonyManager.NETWORK_TYPE_HSDPA:   // ~ 2-14 Mbps
                        case TelephonyManager.NETWORK_TYPE_HSPAP:   // ~ 10-20 Mbps
                        case TelephonyManager.NETWORK_TYPE_HSUPA:   // ~ 1-23 Mbps
                        case TelephonyManager.NETWORK_TYPE_LTE:     // ~ 10+ Mbps
                            return new NetworkType(TYPE_MOBILE_FAST, 4 * ByteConstants.MB);
                        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        default:
                            return new NetworkType(TYPE_MOBILE_SLOW, ByteConstants.MB);
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return new NetworkType(TYPE_UNKNOWN, ByteConstants.MB);
    }
}
