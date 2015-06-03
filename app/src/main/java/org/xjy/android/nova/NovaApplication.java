package org.xjy.android.nova;

import android.app.Application;

public class NovaApplication extends Application {

    private static NovaApplication sNovaApplication;

    public static NovaApplication getInstance() {
        return sNovaApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sNovaApplication = this;
    }
}
