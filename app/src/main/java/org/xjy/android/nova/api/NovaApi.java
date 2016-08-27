package org.xjy.android.nova.api;

import org.xjy.android.nova.transfer.upload.core.UploadObject;

public class NovaApi {

    public static UploadObject getUploadObject(String fileName, String type, String ext) {
        return null;
    }

    public static String refreshUploadToken(String bucketName, String objectName) {
        return "";
    }

    public static Object[] uploadMusicCheck(String md5, long fileLength, int bitrate, String ext) {
        return null;
    }

    public static Object[] uploadMusicInfo(String md5, String songId, long songFileId, String fileName, String song, String album, String artist, int bitrate, long coverId, long lyricId, long cueId, String lyricType) {
        return null;
    }

    public static Object[] getTranscodeState(String uploadedIds) {
        return null;
    }

    public static Object[] publishMusic(long id) {
        return null;
    }
}
