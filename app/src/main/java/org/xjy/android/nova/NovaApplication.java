package org.xjy.android.nova;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.net.ConnectivityManagerCompat;

import org.xjy.android.nova.util.PhoneInfoUtils;

import java.util.LinkedList;

public class NovaApplication extends Application {

    private static NovaApplication sNovaApplication;

    private volatile int mNetworkState;
    private LinkedList<NetworkStateChangeListener> mRegisteredNetworkStateChangeListeners = new LinkedList<NetworkStateChangeListener>();

    public static NovaApplication getInstance() {
        return sNovaApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sNovaApplication = this;
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkState = PhoneInfoUtils.getNetworkState(context);
                if (networkState != mNetworkState) {
                    for (NetworkStateChangeListener networkStateChangeListener : mRegisteredNetworkStateChangeListeners) {
                        networkStateChangeListener.onNetworkStateChange(mNetworkState, networkState);
                    }
                    mNetworkState = networkState;
                }
            }
        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mNetworkState = PhoneInfoUtils.getNetworkState(this);
    }

    public int getmNetworkState() {
        return mNetworkState;
    }

    public void registerNetworkStateChangeListener(NetworkStateChangeListener networkStateChangeListener) {
        mRegisteredNetworkStateChangeListeners.add(networkStateChangeListener);
    }

    public void unRegisterNetworkStateChangeListener(NetworkStateChangeListener networkStateChangeListener) {
        mRegisteredNetworkStateChangeListeners.remove(networkStateChangeListener);
    }

    public static interface NetworkStateChangeListener {
        public void onNetworkStateChange(int oldState, int newState);
    }
}
