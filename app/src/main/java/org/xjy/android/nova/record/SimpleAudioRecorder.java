package org.xjy.android.nova.record;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import org.xjy.android.nova.common.io.FileUtils;

import java.io.File;

public class SimpleAudioRecorder {
    private static final String TAG = "SimpleAudioRecorder";

    private static final int WHAT_START = 0;
    private static final int WHAT_STOP = 1;
    private static final int WHAT_RELEASE = 2;

    private static final int STATE_INITIAL = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_STOPPED = 2;
    private static final int STATE_ERROR = 3;
    private static final int STATE_RELEASED = 4;

    private int mChannels;
    private int mSampleRate;
    private int mEncodeBitRate;
    private int mSource;
    private int mOutputFormat;
    private int mEncoder;
    private Callback mCallback;
    private Handler mHandler;
    private Handler mEventHandler;

    private int mState;
    private MediaRecorder mRecorder;
    private long mRecordTime;

    private SimpleAudioRecorder(Builder builder) {
        mChannels = builder.mChannels;
        mSampleRate = builder.mSampleRate;
        mEncodeBitRate = builder.mEncodeBitRate;
        mSource = builder.mSource;
        mOutputFormat = builder.mOutputFormat;
        mEncoder = builder.mEncoder;
        mCallback = builder.mCallback;
        Looper looper = builder.mLooper;
        final boolean innerLooper = looper == null;
        if (innerLooper) {
            HandlerThread thread = new HandlerThread("SimpleAudioRecorderThread");
            thread.start();
            looper = thread.getLooper();
        }
        mHandler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == WHAT_START) {
                    startRecord((String) msg.obj);
                    mRecordTime = SystemClock.elapsedRealtime();
                } else if (msg.what == WHAT_STOP) {
                    long duration = SystemClock.elapsedRealtime() - mRecordTime;
                    if (duration < 1000) {
                        sendMessageDelayed(obtainMessage(msg.what, msg.obj), 1000 - duration);
                    } else {
                        stopRecord((SimpleAudioRecorder.Callback) msg.obj);
                    }
                } else if (msg.what == WHAT_RELEASE) {
                    releaseRecorder();
                    if (innerLooper) {
                        getLooper().quit();
                    }
                }
            }
        };
    }

    public void start(String outputFilePath) {
        mHandler.sendMessage(mHandler.obtainMessage(WHAT_START, outputFilePath));
    }

    public void stop(Callback callback) {
        mHandler.sendMessage(mHandler.obtainMessage(WHAT_STOP, callback));
    }

    public void release() {
        mHandler.sendEmptyMessage(WHAT_RELEASE);
    }

    private void startRecord(String outputFilePath) {
        if (mState != STATE_RELEASED) {
            try {
                if (mRecorder == null) {
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioChannels(mChannels);
                    mRecorder.setAudioSamplingRate(mSampleRate);
                    mRecorder.setAudioEncodingBitRate(mEncodeBitRate);
                    mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                        @Override
                        public void onError(MediaRecorder mr, int what, int extra) {
                            Log.e(TAG, "onError: " + what + " " + extra);
                            mState = STATE_ERROR;
                            handleEvent(mCallback, STATE_ERROR, what, extra);
                        }
                    });
                } else {
                    mRecorder.reset();
                }
                mState = STATE_INITIAL;
                mRecorder.setAudioSource(mSource);
                mRecorder.setOutputFormat(mOutputFormat);
                mRecorder.setAudioEncoder(mEncoder);
                File file = new File(outputFilePath);
                FileUtils.ensureDirectoryExist(file, false);
                mRecorder.setOutputFile(outputFilePath);
                mRecorder.prepare();
                mRecorder.start();
                mState = STATE_STARTED;
            } catch (Throwable t) {
                t.printStackTrace();
                mState = STATE_ERROR;
                handleEvent(mCallback, STATE_ERROR, 0, 0);
            }
        }
    }

    private void stopRecord(final Callback callback) {
        if (mState == STATE_STARTED && mRecorder != null) {
            try {
                mRecorder.stop();
                mState = STATE_STOPPED;
                handleEvent(callback != null ? callback : mCallback, STATE_STOPPED, 0, 0);
            } catch (Throwable t) {
                t.printStackTrace();
                mState = STATE_ERROR;
                handleEvent(mCallback, STATE_ERROR, 0, 0);
            }
        }
    }

    private void releaseRecorder() {
        if (mState != STATE_RELEASED && mRecorder != null) {
            try {
                mRecorder.release();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            mRecorder = null;
            mState = STATE_RELEASED;
        }
    }

    private void handleEvent(final Callback callback, final int state, final int what, final int extra) {
        if (callback != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                handleCallback(callback, state, what, extra);
            } else {
                if (mEventHandler == null) {
                    mEventHandler = new Handler(Looper.getMainLooper());
                }
                mEventHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleCallback(callback, state, what, extra);
                    }
                });
            }
        }
    }

    private void handleCallback(Callback callback, int state, int what, int extra) {
        if (state == STATE_STOPPED) {
            callback.onStop();
        } else if (state == STATE_ERROR) {
            callback.onError(what, extra);
        }
    }

    public interface Callback {
        void onStop();
        void onError(int what, int extra);
    }

    public static class Builder {
        private int mChannels;
        private int mSampleRate;
        private int mEncodeBitRate;
        private int mSource;
        private int mOutputFormat;
        private int mEncoder;
        private Callback mCallback;
        private Looper mLooper;

        public Builder setChannels(int channels) {
            mChannels = channels;
            return this;
        }

        public Builder setSampleRate(int sampleRate) {
            mSampleRate = sampleRate;
            return this;
        }

        public Builder setEncodeBitRate(int encodeBitRate) {
            mEncodeBitRate = encodeBitRate;
            return this;
        }

        public Builder setSource(int source) {
            mSource = source;
            return this;
        }

        public Builder setOutputFormat(int outputFormat) {
            mOutputFormat = outputFormat;
            return this;
        }

        public Builder setEncoder(int encoder) {
            mEncoder = encoder;
            return this;
        }

        public Builder setCallback(Callback callback) {
            mCallback = callback;
            return this;
        }

        public Builder setLooper(Looper looper) {
            mLooper = looper;
            return this;
        }

        public SimpleAudioRecorder build() {
            return new SimpleAudioRecorder(this);
        }
    }
}
