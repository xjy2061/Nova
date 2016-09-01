package org.xjy.android.nova.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.AsyncTaskLoader;

public abstract class NovaLoader<D> extends AsyncTaskLoader<D> {
    private static Handler sHandler;

    private volatile Throwable mError;
    private Interceptor mInterceptor;

    public NovaLoader(Context context) {
        super(context);
    }

    public void setInterceptor(Interceptor interceptor) {
        mInterceptor = interceptor;
    }

    @Override
    protected D onLoadInBackground() {
        try {
            return loadInBackground();
        } catch (final Throwable t) {
            mError = t;
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deliverResult(D data) {
        if (mError == null) {
            if (mInterceptor != null) {
                mInterceptor.onPreComplete(data);
            }
            onComplete(data);
        } else {
            if (mInterceptor != null) {
                mInterceptor.onPreError(mError);
            }
            onError(mError);
            mError = null;
        }
    }

    @SuppressWarnings({"unused", "unchecked"})
    public void publishProgress(final D data) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mInterceptor != null) {
                    mInterceptor.onPreProgressUpdate(data);
                }
                onProgressUpdate(data);
            }
        });
    }

    public abstract void onComplete(D data);

    public abstract void onError(Throwable error);

    @SuppressWarnings("unused")
    protected void onProgressUpdate(D data) {}

    private static Handler getHandler() {
        synchronized (NovaLoader.class) {
            if (sHandler == null) {
                sHandler = new Handler(Looper.getMainLooper());
            }
            return sHandler;
        }
    }

    public interface Interceptor<D> {
        void onPreComplete(D data);
        void onPreError(Throwable error);
        void onPreProgressUpdate(D data);
    }
}
