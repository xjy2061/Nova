package org.xjy.android.nova.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.AsyncTaskLoader;

public abstract class NovaLoader<D> extends AsyncTaskLoader<D> {
    private Handler mHandler;

    private volatile Throwable mError;
    private Interceptor mInterceptor;

    public NovaLoader(Context context) {
        super(context);
        mHandler = new Handler(Looper.getMainLooper());
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

    public void publishProgress(final D data) {
        mHandler.post(new Runnable() {
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

    protected void onProgressUpdate(D data) {
    }

    public interface Interceptor<D> {
        void onPreComplete(D data);
        void onPreError(Throwable error);
        void onPreProgressUpdate(D data);
    }
}
