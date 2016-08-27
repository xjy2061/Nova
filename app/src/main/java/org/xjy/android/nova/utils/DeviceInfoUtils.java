package org.xjy.android.nova.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.xjy.android.nova.NovaApplication;

public class DeviceInfoUtils {
    public static final int NETWORK_STATE_DISCONNECTED = -1;
    public static final int NETWORK_STATE_MOBILE = 0;
    public static final int NETWORK_STATE_WIFI = 1;

    public static int getNetworkState() {
        return getNetworkState(NovaApplication.getInstance());
    }

    public static int getNetworkState(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        if (networkInfo != null && networkInfo.isConnected()) {
            int networkType = networkInfo.getType();
            if (networkType == ConnectivityManager.TYPE_WIFI) {
                return NETWORK_STATE_WIFI;
            } else if (networkType == ConnectivityManager.TYPE_MOBILE) {
                return NETWORK_STATE_MOBILE;
            }
        }
        return NETWORK_STATE_DISCONNECTED;
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        try {
            return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        } catch (NullPointerException e) { //This happened on some device
            e.printStackTrace();
            return null;
        }
    }
}
