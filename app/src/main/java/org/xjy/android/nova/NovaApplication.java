package org.xjy.android.nova;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import org.xjy.android.nova.utils.AsyncTask;
import org.xjy.android.nova.utils.DeviceInfoUtils;

import java.util.LinkedList;

public class NovaApplication extends Application {

    private static NovaApplication sNovaApplication;

    private volatile int mNetworkState;
    private LinkedList<NetworkStateChangeListener> mRegisteredNetworkStateChangeListeners = new LinkedList<NetworkStateChangeListener>();

    public NovaApplication() {
        super();
        sNovaApplication = this;
    }

    public static NovaApplication getInstance() {
        return sNovaApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Class.forName(AsyncTask.class.getName()); //Make sure onPostExecute method run in main thread.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkState = DeviceInfoUtils.getNetworkState(context);
                if (networkState != mNetworkState) { //onReceive called multiple times
                    for (NetworkStateChangeListener networkStateChangeListener : mRegisteredNetworkStateChangeListeners) {
                        networkStateChangeListener.onNetworkStateChange(mNetworkState, networkState);
                    }
                    mNetworkState = networkState;
                }
            }
        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mNetworkState = DeviceInfoUtils.getNetworkState(this);
    }

    public int getNetworkState() {
        return mNetworkState;
    }

    public void registerNetworkStateChangeListener(NetworkStateChangeListener networkStateChangeListener) {
        mRegisteredNetworkStateChangeListeners.add(networkStateChangeListener);
    }

    public void unRegisterNetworkStateChangeListener(NetworkStateChangeListener networkStateChangeListener) {
        mRegisteredNetworkStateChangeListeners.remove(networkStateChangeListener);
    }

    public interface NetworkStateChangeListener {
        public void onNetworkStateChange(int oldState, int newState);
    }
}
