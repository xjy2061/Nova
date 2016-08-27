package org.xjy.android.nova.transfer.storage;

import android.content.Context;
import android.content.SharedPreferences;

import org.xjy.android.nova.NovaApplication;
import org.xjy.android.nova.transfer.upload.core.NetworkType;
import org.xjy.android.nova.utils.Constants;
import org.xjy.android.nova.utils.JSONUtils;

import java.util.ArrayList;

public class UploadStorage {
    private static final String KEY_NETWORK_TYPE = "network_type";
    private static final String KEY_LBS_TIME = "lbs_time";
    private static final String KEY_UPLOAD_SERVER = "upload_server";
    private static final String KEY_LBS_SERVER = "lbs_server";

    public static int getNetworkType(String bucketName) {
        return getPreferences().getInt(bucketName + KEY_NETWORK_TYPE, NetworkType.TYPE_UNKNOWN);
    }

    public static void saveNetworkType(String bucketName, int type) {
        getPreferences().edit().putInt(bucketName + KEY_NETWORK_TYPE, type).commit();
    }

    public static long getLbsTime(String bucketName) {
        return getPreferences().getLong(bucketName + KEY_LBS_TIME, 0);
    }

    public static void saveLbsTime(String bucketName, long time) {
        getPreferences().edit().putLong(bucketName + KEY_LBS_TIME, time).commit();
    }

    public static ArrayList<String> getUploadServer(String bucketName) {
        return JSONUtils.jsonArrayStringToStringList(getPreferences().getString(bucketName + KEY_UPLOAD_SERVER, null));
    }

    public static void saveUploadServer(String bucketName, String uploadServer) {
        getPreferences().edit().putString(bucketName + KEY_UPLOAD_SERVER, uploadServer).commit();
    }

    public static String getLbsServer(String bucketName) {
        return getPreferences().getString(bucketName + KEY_LBS_SERVER, null);
    }

    public static void saveLbsServer(String bucketName, String server) {
        getPreferences().edit().putString(bucketName + KEY_LBS_SERVER, server).commit();
    }

    private static SharedPreferences getPreferences() {
        return NovaApplication.getInstance().getSharedPreferences(Constants.SHARED_PREFS.UPLOAD_PREF, Context.MODE_PRIVATE);
    }
}
