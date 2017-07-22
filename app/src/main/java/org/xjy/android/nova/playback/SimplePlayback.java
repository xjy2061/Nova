package org.xjy.android.nova.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;

public class SimplePlayback {
    private static final float VOLUME_DUCK = 0.2f;
    private static final float VOLUME_NORMAL = 1.0f;

    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    private static final int AUDIO_FOCUSED = 2;

    private final Context mContext;
    private final WifiManager.WifiLock mWifiLock;

    private final AudioManager mAudioManager;
    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private int mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
    private boolean mPlayOnFocusGain;

    private final IntentFilter mAudioNoisyIntentFilter;
    private final BroadcastReceiver mAudioNoisyReceiver;
    private boolean mAudioNoisyReceiverRegistered;

    private final MediaPlayer.OnPreparedListener mOnPreparedListener;
    private final MediaPlayer.OnCompletionListener mOnCompletionListener;
    private final MediaPlayer.OnErrorListener mOnErrorListener;

    private MediaPlayer mMediaPlayer;
    private Uri mUri;
    private boolean mLooping;
    private Callback mCallback;

    public SimplePlayback(Context context) {
        mContext = context.getApplicationContext();
        mWifiLock = ((WifiManager) mContext.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "ar_playback");
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        mCurrentAudioFocusState = AUDIO_FOCUSED;
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK;
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                        mPlayOnFocusGain = isPlaying();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                        break;
                }

                if (mMediaPlayer != null) {
                    configurePlayerState();
                }
            }
        };

        mAudioNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mAudioNoisyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                    if (isPlaying()) {
                        pause();
                    }
                }
            }
        };

        mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                start();
            }
        };
        mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mLooping) {
                    play(mUri);
                }
            }
        };
        mOnErrorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (mCallback != null) {
                    mCallback.onError(mp, what, extra);
                }
                releaseResources(true);
                return false;
            }
        };
    }

    public void play(Uri uri) {
        releaseResources(false);

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
        } else {
            try {
                mMediaPlayer.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mUri = uri;
        try {
            mMediaPlayer.setDataSource(mContext, uri);
        } catch (Exception e) {
            e.printStackTrace();
            if (mCallback != null) {
                mCallback.onError(mMediaPlayer, 0, 0);
            }
            releaseResources(true);
            return;
        }

        mWifiLock.acquire();

        mMediaPlayer.prepareAsync();
    }

    public void start() {
        mPlayOnFocusGain = true;
        tryToGetAudioFocus();
        registerAudioNoisyReceiver();
        configurePlayerState();
    }

    public void stop() {
        giveUpAudioFocus();
        unregisterAudioNoisyReceiver();
        releaseResources(true);
    }

    public void pause() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.pause();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        releaseResources(false);
        unregisterAudioNoisyReceiver();
        if (mCallback != null) {
            mCallback.onPause();
        }
    }

    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            registerAudioNoisyReceiver();
            try {
                mMediaPlayer.seekTo(position);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLooping(boolean looping) {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.setLooping(looping);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLogicLooping(boolean looping) {
        mLooping = looping;
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            try {
                return mMediaPlayer.isPlaying();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            try {
                return mMediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private void tryToGetAudioFocus() {
        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_FOCUSED;
        } else {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    private void giveUpAudioFocus() {
        if (mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    private void configurePlayerState() {
        if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
            pause();
        } else {
            registerAudioNoisyReceiver();

            if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK);
            } else {
                mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL);
            }

            if (mPlayOnFocusGain) {
                try {
                    mMediaPlayer.start();
                    if (mCallback != null) {
                        mCallback.onStart();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                mPlayOnFocusGain = false;
            }
        }
    }

    private void releaseResources(boolean releasePlayer) {
        if (releasePlayer && mMediaPlayer != null) {
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.release();
            mMediaPlayer = null;
            mPlayOnFocusGain = false;
        }

        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

    public interface Callback {
        void onStart();
        void onPause();
        void onError(MediaPlayer mp, int what, int extra);
    }
}
