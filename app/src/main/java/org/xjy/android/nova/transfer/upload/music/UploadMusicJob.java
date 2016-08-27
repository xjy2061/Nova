package org.xjy.android.nova.transfer.upload.music;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import org.xjy.android.nova.NovaApplication;
import org.xjy.android.nova.api.NovaApi;
import org.xjy.android.nova.transfer.common.ProgressListener;
import org.xjy.android.nova.transfer.common.QuitGuard;
import org.xjy.android.nova.transfer.common.TransferAgent;
import org.xjy.android.nova.transfer.common.TransferException;
import org.xjy.android.nova.transfer.upload.core.Uploader;
import org.xjy.android.nova.transfer.upload.utils.UploadUtils;
import org.xjy.android.nova.transfer.utils.TransferUtils;
import org.xjy.android.nova.utils.Constants;
import org.xjy.android.nova.utils.DeviceInfoUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class UploadMusicJob extends TransferAgent.TransferJob {
    private String mFilePath;
    private UploadMusicObject mUploadMusicObject;
    private long mUserId;

    UploadMusicJob(UploadMusicObject uploadMusicObject, long userId) {
        mFilePath = uploadMusicObject.getPath();
        mUploadMusicObject = uploadMusicObject;
        mUserId = userId;
    }

    UploadMusicJob() {}

    UploadMusicJob(String path) {
        mFilePath = path;
    }

    public String getId() {
        return mFilePath;
    }

    public void setId(String filePath) {
        mFilePath = filePath;
    }

    UploadMusicObject getUploadMusicObject() {
        return mUploadMusicObject;
    }

    public void start() {
        mState = STATE_FAILED;
        UploadMusicAgent uploadMusicAgent = UploadMusicAgent.getInstance();
        UploadMusicDao uploadMusicDao = UploadMusicDao.getInstance();
        try {
            File file = new File(mFilePath);
            if (!file.exists()) {
                uploadMusicDao.updateState(mFilePath, UploadMusicAgent.STATE_FAILED, UploadMusicAgent.FAIL_FILE_NOT_FOUND, mUserId);
                notifyStateChange(UploadMusicAgent.STATE_FAILED, UploadMusicAgent.FAIL_FILE_NOT_FOUND);
                return;
            }
            if (DeviceInfoUtils.getNetworkState() == DeviceInfoUtils.NETWORK_STATE_DISCONNECTED) {
                uploadMusicAgent.pause(false);
                return;
            }
            notifyStateChange(UploadMusicAgent.STATE_UPLOADING, 0);
            QuitGuard quitGuard = new QuitGuard() {
                @Override
                public boolean isQuit() {
                    return mState == STATE_QUIT;
                }
            };
            String md5 = mUploadMusicObject.getMd5();
            if (TextUtils.isEmpty(md5)) {
                md5 = UploadUtils.calcMD5(file, quitGuard);
                uploadMusicDao.updateMD5(mFilePath, md5, mUserId);
            }
            if (mState == STATE_QUIT) {
                return;
            }
            String checkId = mUploadMusicObject.getCheckId();
            long fileId = mUploadMusicObject.getFileId();
            String fileName = file.getName();
            if (TextUtils.isEmpty(checkId)) {
                int suffixIndex = fileName.lastIndexOf(".");
                Object[] checkResult = NovaApi.uploadMusicCheck(md5, file.length(), mUploadMusicObject.getBitrate() / 1000, suffixIndex > 0 ? fileName.substring(suffixIndex + 1) : "");
                int code = (int) checkResult[0];
                if (code == 200) {
                    checkId = (String) checkResult[1];
                    fileId = (boolean) checkResult[2] ? 0 : -1;
                    uploadMusicDao.updateCheckIdAndFileId(mFilePath, checkId, fileId, mUserId);
                } else {
                    int failReason = 0;
                    if (code == 501) {
                        failReason = UploadMusicAgent.FAIL_BIG_FILE;
                    } else if (code == 506) {
                        failReason = UploadMusicAgent.FAIL_LACK_OF_SPACE;
                        uploadMusicAgent.pause(true);
                    }
                    uploadMusicDao.updateState(mFilePath, UploadMusicAgent.STATE_FAILED, failReason, mUserId);
                    notifyStateChange(UploadMusicAgent.STATE_FAILED, failReason);
                    return;
                }
            }
            if (mState == STATE_QUIT) {
                return;
            }
            if (fileId == 0) {
                fileId = Uploader.upload(file, "audio", "audio/mpeg", md5, true, new ProgressListener() {
                    @Override
                    public void onProgressChanged(long progress, long max) {
                        if (mState == STATE_QUIT) {
                            throw new TransferException(TransferException.TYPE_QUIT);
                        }
                        notifyProgressChange(Constants.BROADCAST_ACTIONS.UPLOAD_MUSIC_PROGRESS_CHANGE, mFilePath, progress, max);
                    }
                }, quitGuard);
                if (fileId < 0) {
                    if (fileId == Uploader.ERROR_QUIT) {
                        return;
                    }
                    int failReason = 0;
                    if (fileId == Uploader.ERROR_FILE_NOT_EXIST) {
                        failReason = UploadMusicAgent.FAIL_FILE_NOT_FOUND;
                    } else if (fileId == Uploader.ERROR_NETWORK || fileId == Uploader.ERROR_DATA_RESTRICT) {
                        TimeUnit.SECONDS.sleep(1);
                        if (mState == STATE_QUIT) {
                            return;
                        }
                        failReason = UploadMusicAgent.FAIL_NETWORK_ERROR;
                    } else if (fileId == Uploader.ERROR_API_SERVER || fileId == Uploader.ERROR_UPLOAD_SERVER) {
                        failReason = UploadMusicAgent.FAIL_SERVER_ERROR;
                    }
                    uploadMusicDao.updateState(mFilePath, UploadMusicAgent.STATE_FAILED, failReason, mUserId);
                    notifyStateChange(UploadMusicAgent.STATE_FAILED, failReason);
                    return;
                } else {
                    uploadMusicDao.updateFileId(mFilePath, fileId, mUserId);
                }
            }
            if (mState == STATE_QUIT) {
                return;
            }
            Object[] uploadResult = NovaApi.uploadMusicInfo(md5, checkId, fileId, fileName, mUploadMusicObject.getName(), mUploadMusicObject.getAlbumName(), mUploadMusicObject.getArtistName(),
                    mUploadMusicObject.getBitrate() / 1000, 0, 0, 0, null);
            int code = (int) uploadResult[0];
            if (code == 200) {
                long uploadSongId = (long) uploadResult[1];
                uploadMusicDao.updateUploadSongIdAndState(mFilePath, uploadSongId, UploadMusicAgent.STATE_COMPLETED, mUserId);
                notifyStateChange(UploadMusicAgent.STATE_COMPLETED, 0);
                mState = STATE_SUCCESS;
                uploadMusicAgent.startPoll();
            } else {
                int failReason = code == 501 ? UploadMusicAgent.FAIL_BIG_FILE : UploadMusicAgent.FAIL_FILE_MODIFIED;
                uploadMusicDao.updateState(mFilePath, UploadMusicAgent.STATE_FAILED, failReason, mUserId);
                notifyStateChange(UploadMusicAgent.STATE_FAILED, failReason);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            int failReason = 0;
//            if (t instanceof AuthException) {
//                failReason = UploadMusicAgent.FAIL_NOT_LOGIN;
//            } else if (t instanceof ApiException) {
//                failReason = ((ApiException) t).getCode() == ApiException.CODE.IO_ERR ? UploadMusicAgent.FAIL_NETWORK_ERROR : UploadMusicAgent.FAIL_SERVER_ERROR;
//            } else if (t instanceof WifiOnlyException) {
//                failReason = UploadMusicAgent.FAIL_NETWORK_ERROR;
//            } else if (t instanceof TransferException) {
//                if (((TransferException) t).getType() == TransferException.TYPE_QUIT) {
//                    return;
//                }
//            }
            if (failReason == UploadMusicAgent.FAIL_NETWORK_ERROR) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mState == STATE_QUIT) {
                return;
            }
            uploadMusicDao.updateState(mFilePath, UploadMusicAgent.STATE_FAILED, failReason, mUserId);
            notifyStateChange(UploadMusicAgent.STATE_FAILED, failReason);
        }
    }

    private void notifyStateChange(int state, int failReason) {
        Intent intent = new Intent(Constants.BROADCAST_ACTIONS.UPLOAD_MUSIC_STATE_CHANGE);
        intent.putExtra(UploadMusicAgent.EXTRA_PATH, mFilePath);
        intent.putExtra(UploadMusicAgent.EXTRA_STATE, TransferUtils.parcelInt(state, failReason));
        LocalBroadcastManager.getInstance(NovaApplication.getInstance()).sendBroadcast(intent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UploadMusicJob that = (UploadMusicJob) o;

        return !(mFilePath != null ? !mFilePath.equals(that.mFilePath) : that.mFilePath != null);
    }

    @Override
    public int hashCode() {
        return mFilePath != null ? mFilePath.hashCode() : 0;
    }
}
