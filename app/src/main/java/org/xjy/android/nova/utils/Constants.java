package org.xjy.android.nova.utils;

public class Constants {

    private Constants() {}

    public static final class BROADCAST_ACTIONS {
        public static final String NETWORK_STATE_CHANGE = "org.xjy.android.nova.broadcast.action.NETWORK_STATE_CHANGE";
        public static final String UPLOAD_MUSIC_QUEUE_CHANGE = "org.xjy.android.nova.broadcast.action.UPLOAD_MUSIC_QUEUE_CHANGE";
        public static final String UPLOAD_MUSIC_STATE_CHANGE = "org.xjy.android.nova.broadcast.action.UPLOAD_MUSIC_STATE_CHANGE";
        public static final String UPLOAD_MUSIC_FIRE_JOB = "org.xjy.android.nova.broadcast.action.UPLOAD_MUSIC_FIRE_JOB";
        public static final String UPLOAD_MUSIC_PROGRESS_CHANGE = "org.xjy.android.nova.broadcast.action.UPLOAD_MUSIC_PROGRESS_CHANGE";
        public static final String UPLOAD_MUSIC_PUBLISH = "org.xjy.android.nova.broadcast.action.UPLOAD_MUSIC_PUBLISH";
        public static final String UPLOAD_MUSIC_TRANSCODE = "org.xjy.android.nova.broadcast.action.UPLOAD_MUSIC_TRANSCODE";
    }

    public static final class SHARED_PREFS {
        public static final String NOVA = "nova";
        public static final String UPLOAD_PREF = "upload";
    }

    public static final class REQUEST_CODES {
        public static final int TAKE_PHOTO = 1001;
    }
}
