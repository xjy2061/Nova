package org.xjy.android.nova.transfer.upload.music;

import org.xjy.android.nova.transfer.common.TransferObject;

public class UploadMusicObject extends TransferObject {
    private String path;
    private String name;
    private String albumName;
    private String artistName;
    private int bitrate;
    private String md5;
    private String checkId;
    private long fileId;
    private long songId;
    private long fileLength;

    public UploadMusicObject(String path, String name, String albumName, String artistName, int bitrate) {
        this.path = path;
        this.name = name;
        this.albumName = albumName;
        this.artistName = artistName;
        this.bitrate = bitrate;
    }

    public UploadMusicObject() {}

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getCheckId() {
        return checkId;
    }

    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }
}
