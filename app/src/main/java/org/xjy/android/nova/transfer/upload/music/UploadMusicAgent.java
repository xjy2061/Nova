package org.xjy.android.nova.transfer.upload.music;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.xjy.android.nova.NovaApplication;
import org.xjy.android.nova.account.AccountManager;
import org.xjy.android.nova.api.NovaApi;
import org.xjy.android.nova.transfer.common.TransferAgent;
import org.xjy.android.nova.transfer.utils.TransferUtils;
import org.xjy.android.nova.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UploadMusicAgent extends TransferAgent {
    public static final int STATE_ABSENT = -1;
    public static final int STATE_WAIT = 0;
    public static final int STATE_WAITING = 1;
    public static final int STATE_UPLOADING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_FAILED = 4;
    public static final int STATE_COMPLETED = 5;
    public static final int STATE_TRANSCODE_FAILED = 6;
    public static final int STATE_TRANSCODE_COMPLETED = 7;
    public static final int STATE_PUBLISH_FAILED = 8;
    public static final int STATE_PUBLISH_SUCCESS = 9;

    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_TRANSCODE_SUCCESS_PATHS = "transcode_success_paths";
    public static final String EXTRA_TRANSCODE_FAILED_PATHS = "transcode_failed_paths";
    public static final String EXTRA_PUBLISH_PATHS = "publish_paths";
    public static final String EXTRA_PUBLISH_STATE = "publish_state";
    public static final String EXTRA_FAILED_COUNT = "failed_count";

    public static final int FAIL_FILE_NOT_FOUND = 1;
    public static final int FAIL_BIG_FILE = 2;
    public static final int FAIL_LACK_OF_SPACE = 3;
    public static final int FAIL_FILE_MODIFIED = 4;
    public static final int FAIL_NOT_LOGIN = 5;
    public static final int FAIL_NETWORK_ERROR = 6;
    public static final int FAIL_SERVER_ERROR = 7;
    public static final int FAIL_RESTRICT_SONG = 8;

    public static final class UploadAction extends Action {
        public static final int UPLOAD_MUSIC = 1;
        public static final int UPLOAD_MUSICS = 2;
        public static final int UPLOAD_ENTRY = 3;
        public static final int UPLOAD_ENTRIES = 4;
        public static final int PAUSE_ENTRY = 5;
        public static final int PAUSE_ENTRIES = 6;
        public static final int DELETE_MUSIC = 7;
        public static final int DELETE_MUSICS = 8;
        public static final int AUTO_PAUSE = 9;
    }

    public static final int POLL_CHECK = 1;
    public static final int POLL_QUIT = 2;

    public static final String TAG = "UPLOAD_MUSIC";

    private static final UploadMusicAgent sUploadMusicAgent = new UploadMusicAgent();

    private UploadMusicDao mUploadMusicDao;
    private Handler mPollHandler;
    private HashSet mCurrentFailedJobs = new HashSet();

    private UploadMusicAgent() {
        mUploadMusicDao = UploadMusicDao.getInstance();
    }

    public static UploadMusicAgent getInstance() {
        return sUploadMusicAgent;
    }

//    public void upload(LocalMusicInfo music) {
//        initHandler();
//        mHandler.sendMessage(mHandler.obtainMessage(UploadAction.UPLOAD_MUSIC, music));
//    }
//
//    public void upload(ArrayList<LocalMusicInfo> musics) {
//        initHandler();
//        mHandler.sendMessage(mHandler.obtainMessage(UploadAction.UPLOAD_MUSICS, musics));
//    }
//
//    public void uploadEntry(UploadMusicActivity.UploadMusicEntry entry) {
//        initHandler();
//        mHandler.sendMessage(mHandler.obtainMessage(UploadAction.UPLOAD_ENTRY, entry));
//    }
//
//    public void uploadEntries(ArrayList<UploadMusicActivity.UploadMusicEntry> entries) {
//        initHandler();
//        mHandler.sendMessage(mHandler.obtainMessage(UploadAction.UPLOAD_ENTRIES, entries));
//    }
//
//    public void pauseEntry(UploadMusicActivity.UploadMusicEntry entry) {
//        initHandler();
//        mHandler.sendMessage(mHandler.obtainMessage(UploadAction.PAUSE_ENTRY, entry));
//    }
//
//    public void pauseEntries(ArrayList<UploadMusicActivity.UploadMusicEntry> entries) {
//        initHandler();
//        mHandler.sendMessage(mHandler.obtainMessage(UploadAction.PAUSE_ENTRIES, entries));
//    }

    public void delete(String path) {
        initHandler();
        mHandler.sendMessage(mHandler.obtainMessage(UploadAction.DELETE_MUSIC, path));
    }

    public void delete(HashSet<String> paths) {
        initHandler();
        mHandler.sendMessage(mHandler.obtainMessage(UploadAction.DELETE_MUSICS, paths));
    }

    public void autoPause() {
        initHandler();
        mHandler.sendMessage(mHandler.obtainMessage(UploadAction.AUTO_PAUSE));
    }

    public Pair<Integer, Integer> getUploadProgress() {
        return mUploadMusicDao.getUploadProgress(AccountManager.getCurrentUserId());
    }

    public int getCount() {
        return mUploadMusicDao.getCount(AccountManager.getCurrentUserId());
    }

//    public Pair<ArrayList<UploadMusicActivity.UploadMusicEntry>, HashMap<String, UploadMusicActivity.UploadMusicEntry>> getUploadMusicEntry() {
//        return mUploadMusicDao.getUploadMusicEntry(AccountManager.getCurrentUserId());
//    }

    int getUploadMusicState(String path, int recordState, long userId) {
        if (recordState < 0) {
            recordState = mUploadMusicDao.getUploadState(path, userId);
        }
        if (recordState == STATE_WAIT) {
            UploadMusicJob uploadMusicJob = new UploadMusicJob(path);
            if (isCurrentJob(uploadMusicJob)) {
                recordState = STATE_UPLOADING;
            } else if (mTransferQueue.contains(uploadMusicJob)) {
                recordState = STATE_WAITING;
            }
        }
        return recordState;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (mPollHandler != null) {
            mPollHandler.getLooper().quit();
        }
    }

//    private void uploadMusics(ArrayList<LocalMusicInfo> musics) {
//        mDoingJobType = JOB_TYPE_ADD;
//        ArrayList<String> paths = new ArrayList<>();
//        ArrayList<UploadMusicObject> uploadMusicObjects = new ArrayList<>();
//        for (int i = 0, size = musics.size(); i < size; i++) {
//            LocalMusicInfo music = musics.get(i);
//            String path = music.getFilePath();
//            paths.add(path);
//            uploadMusicObjects.add(new UploadMusicObject(path, music.getMusicName(false), music.getAlbumName(false), music.getSingerName(false), music.getBitrate()));
//        }
//        uploadObjects(paths, uploadMusicObjects);
//    }

//    private void uploadObjects(ArrayList<UploadMusicActivity.UploadMusicEntry> entries) {
//        mDoingJobType = JOB_TYPE_ADD;
//        ArrayList<String> paths = new ArrayList<>();
//        ArrayList<UploadMusicObject> uploadMusicObjects = new ArrayList<>();
//        for (int i = 0, size = entries.size(); i < size; i++) {
//            UploadMusicActivity.UploadMusicEntry entry = entries.get(i);
//            if (entry != null) {
//                UploadMusicObject uploadMusicObject = entry.getUploadMusicObject();
//                int state = uploadMusicObject.getState();
//                if (state >= STATE_COMPLETED && state <= STATE_TRANSCODE_COMPLETED) {
//                    continue;
//                }
//                paths.add(uploadMusicObject.getPath());
//                uploadMusicObjects.add(uploadMusicObject);
//            }
//        }
//        uploadObjects(paths, uploadMusicObjects);
//    }

    private void uploadObjects(ArrayList<String> paths, ArrayList<UploadMusicObject> uploadMusicObjects) {
        long userId = AccountManager.getCurrentUserId();
        HashMap<String, UploadMusicObject> uploadInfoMap = mUploadMusicDao.getUploadInfo(paths, userId);
        ArrayList<UploadMusicJob> jobs = new ArrayList<>();
        for (int i = 0, size = uploadMusicObjects.size(); i < size; i++) {
            UploadMusicObject uploadMusicObject = uploadMusicObjects.get(i);
            UploadMusicObject uploadInfo = uploadInfoMap.get(uploadMusicObject.getPath());
            if (uploadInfo != null) {
                int state = uploadInfo.getState();
                if (state >= STATE_COMPLETED && state <= STATE_TRANSCODE_COMPLETED) {
                    continue;
                }
                uploadMusicObject.setMd5(uploadInfo.getMd5());
                uploadMusicObject.setCheckId(uploadInfo.getCheckId());
                uploadMusicObject.setFileId(uploadInfo.getFileId());
                uploadMusicObject.setSongId(uploadInfo.getSongId());
            }
            UploadMusicJob job = new UploadMusicJob(uploadMusicObject, userId);
            if (!isCurrentJob(job) && !mTransferQueue.contains(job)) {
                jobs.add(job);
            }
        }
        int size = jobs.size();
        if (size > 0 && mUploadMusicDao.insertMusics(jobs, userId) > 0) {
            HashSet<String> ids = new HashSet<>();
            for (int i = 0; i < size; i++) {
                UploadMusicJob job = jobs.get(i);
                mTransferQueue.offer(job);
                ids.add(job.getId());
            }
            notifyQueueChanged(TransferAgent.QUEUE_ADD, ids, false);
            startTransferThread();
        } else {
            quit();
        }
    }

//    private void pauseObjects(ArrayList<UploadMusicActivity.UploadMusicEntry> entries) {
//        mDoingJobType = JOB_TYPE_DELETE;
//        long userId = AccountManager.getCurrentUserId();
//        HashSet<String> paths = new HashSet<>();
//        UploadMusicJob fakeJob = new UploadMusicJob();
//        for (int i = 0, size = entries.size(); i < size; i++) {
//            UploadMusicActivity.UploadMusicEntry entry = entries.get(i);
//            if (entry != null) {
//                UploadMusicObject uploadMusicObject = entry.getUploadMusicObject();
//                int state = uploadMusicObject.getState();
//                if (state >= STATE_COMPLETED && state <= STATE_TRANSCODE_COMPLETED) {
//                    continue;
//                }
//                String path = uploadMusicObject.getPath();
//                paths.add(path);
//                fakeJob.setId(path);
//                mTransferQueue.remove(fakeJob);
//                quitCurrentJob(fakeJob);
//            }
//        }
//        if (mUploadMusicDao.updateState(paths, STATE_PAUSED, -1, userId) > 0) {
//            notifyQueueChanged(QUEUE_PAUSE, paths, false);
//        }
//        quit();
//    }

    private void deleteMusics(HashSet<String> paths) {
        mDoingJobType = JOB_TYPE_DELETE;
        long userId = AccountManager.getCurrentUserId();
        UploadMusicJob fakeJob = new UploadMusicJob();
        for (String path : paths) {
            fakeJob.setId(path);
            mTransferQueue.remove(fakeJob);
            quitCurrentJob(fakeJob);
        }
        if (mUploadMusicDao.deleteMusics(paths, userId) > 0) {
            notifyQueueChanged(QUEUE_REMOVE, paths, false);
        }
        quit();
    }

    void pause(boolean cloudDiskFull) {
        mDoingJobType = JOB_TYPE_DELETE;
        mTransferQueue.clear();
        HashSet paths = new HashSet<>();
        paths.add(quitCurrentJob());
        notifyQueueChanged(QUEUE_AUTO_PAUSE, paths, cloudDiskFull);
        quit();
    }

    private void notifyQueueChanged(int type, HashSet paths, boolean cloudDiskFull) {
        int failedCount = 0;
        synchronized (mCurrentJobs) {
            if (type == QUEUE_ADD) {
                mCurrentJobs.addAll(paths);
            } else if (type == QUEUE_REMOVE) {
                mCurrentJobs.removeAll(paths);
                mCurrentFiredJobs.removeAll(paths);
                mCurrentSuccessJobs.removeAll(paths);
                mCurrentFailedJobs.removeAll(paths);
            } else if (type == QUEUE_PAUSE) {
                mCurrentFiredJobs.addAll(paths);
            } else if (type == QUEUE_AUTO_PAUSE) {
                mCurrentJobs.removeAll(mCurrentFiredJobs);
                paths.addAll(mCurrentJobs);
                mCurrentJobs.clear();
            }
            if (mCurrentFiredJobs.size() >= mCurrentJobs.size()) {
                failedCount = cloudDiskFull ? Integer.MAX_VALUE : mCurrentFailedJobs.size();
                mCurrentJobs.clear();
                mCurrentFiredJobs.clear();
                mCurrentSuccessJobs.clear();
                mCurrentFailedJobs.clear();
            }
        }
        Intent intent = new Intent(Constants.BROADCAST_ACTIONS.UPLOAD_MUSIC_QUEUE_CHANGE);
        intent.putExtra(EXTRA_QUEUE_CHANGE_TYPE, type);
        intent.putExtra(EXTRA_QUEUE_CHANGE_IDS, paths);
        intent.putExtra(EXTRA_FAILED_COUNT, failedCount);
        LocalBroadcastManager.getInstance(NovaApplication.getInstance()).sendBroadcast(intent);
    }

    @Override
    protected void handleTransferMessage(Message msg) {
//        if (msg.what == UploadAction.UPLOAD_MUSIC) {
//            ArrayList<LocalMusicInfo> musics = new ArrayList<>();
//            musics.add((LocalMusicInfo) msg.obj);
//            uploadMusics(musics);
//        } else if (msg.what == UploadAction.UPLOAD_MUSICS) {
//            uploadMusics((ArrayList<LocalMusicInfo>) msg.obj);
//        } else if (msg.what == UploadAction.UPLOAD_ENTRY) {
//            ArrayList<UploadMusicActivity.UploadMusicEntry> entries = new ArrayList<>();
//            entries.add((UploadMusicActivity.UploadMusicEntry) msg.obj);
//            uploadObjects(entries);
//        } else if (msg.what == UploadAction.UPLOAD_ENTRIES) {
//            uploadObjects((ArrayList<UploadMusicActivity.UploadMusicEntry>)msg.obj);
//        } else if (msg.what == UploadAction.PAUSE_ENTRY) {
//            ArrayList<UploadMusicActivity.UploadMusicEntry> entries = new ArrayList<>();
//            entries.add((UploadMusicActivity.UploadMusicEntry) msg.obj);
//            pauseObjects(entries);
//        } else if (msg.what == UploadAction.PAUSE_ENTRIES) {
//            pauseObjects((ArrayList<UploadMusicActivity.UploadMusicEntry>)msg.obj);
//        } else if (msg.what == UploadAction.DELETE_MUSIC) {
//            HashSet<String> paths = new HashSet<>();
//            paths.add((String) msg.obj);
//            deleteMusics(paths);
//        } else if (msg.what == UploadAction.DELETE_MUSICS) {
//            deleteMusics((HashSet<String>) msg.obj);
//        } else if (msg.what == UploadAction.AUTO_PAUSE) {
//            pause(false);
//        }
    }

    @Override
    protected boolean hasTransferMessage() {
        return mHandler != null && (mHandler.hasMessages(UploadAction.UPLOAD_MUSIC) || mHandler.hasMessages(UploadAction.UPLOAD_MUSICS) || mHandler.hasMessages(UploadAction.UPLOAD_ENTRY)
                || mHandler.hasMessages(UploadAction.UPLOAD_ENTRIES) || mHandler.hasMessages(UploadAction.PAUSE_ENTRY) || mHandler.hasMessages(UploadAction.DELETE_MUSIC) || mHandler.hasMessages(UploadAction.DELETE_MUSICS)
                || mHandler.hasMessages(UploadAction.AUTO_PAUSE));
    }

    @Override
    protected void fireJob(TransferJob job) {
        synchronized (mCurrentJobs) {
            Object id = job.getId();
            if (mCurrentJobs.contains(id)) {
                mCurrentFiredJobs.add(id);
                int state = job.getState();
                if (state == TransferJob.STATE_SUCCESS) {
                    mCurrentSuccessJobs.add(id);
                } else if (state == TransferJob.STATE_FAILED) {
                    mCurrentFailedJobs.add(id);
                }
                int failedCount = 0;
                if (mCurrentFiredJobs.size() >= mCurrentJobs.size()) {
                    failedCount = mCurrentFailedJobs.size();
                    mCurrentJobs.clear();
                    mCurrentFiredJobs.clear();
                    mCurrentSuccessJobs.clear();
                    mCurrentFailedJobs.clear();
                }
                Intent intent = new Intent(Constants.BROADCAST_ACTIONS.UPLOAD_MUSIC_FIRE_JOB);
                intent.putExtra(EXTRA_FAILED_COUNT, failedCount);
                LocalBroadcastManager.getInstance(NovaApplication.getInstance()).sendBroadcast(intent);
            }
        }
    }

    public void startPoll() {
        if (mPollHandler == null || !mPollHandler.getLooper().getThread().isAlive()) {
            HandlerThread pollThread = new HandlerThread("upload music poll thread");
            pollThread.start();
            mPollHandler = new PollHandler(pollThread.getLooper());
        }
        if (!mPollHandler.hasMessages(POLL_CHECK)) {
            mPollHandler.sendEmptyMessage(POLL_CHECK);
        }
    }

    class PollHandler extends Handler {
        public PollHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case POLL_CHECK:
                    if (!AccountManager.isLogin()) {
                        return;
                    }
                    long userId = AccountManager.getCurrentUserId();
                    Pair<HashSet<Long>, HashSet<Long>> uploadSongIds = mUploadMusicDao.getUploadSongIds(userId);
                    HashSet<Long> uploadedIds = uploadSongIds.first;
                    HashSet<Long> transcodedIds = uploadSongIds.second;
                    long interval = DateUtils.MINUTE_IN_MILLIS;
                    if (uploadedIds.size() > 0) {
                        try {
                            Object[] states = NovaApi.getTranscodeState(new JSONArray(uploadedIds).toString());
                            if (states != null) {
                                HashSet<Long> successIds = (HashSet<Long>) states[0];
                                HashSet<Long> failedIds = (HashSet<Long>) states[1];
                                interval = ((long) states[2]) * 1000;
                                Pair<HashSet<String>, HashSet<String>> updatedPaths = mUploadMusicDao.updateTranscodeState(successIds, failedIds, userId);
                                Intent intent = new Intent(Constants.BROADCAST_ACTIONS.UPLOAD_MUSIC_TRANSCODE);
                                intent.putExtra(EXTRA_TRANSCODE_SUCCESS_PATHS, updatedPaths.first);
                                intent.putExtra(EXTRA_TRANSCODE_FAILED_PATHS, updatedPaths.second);
                                LocalBroadcastManager.getInstance(NovaApplication.getInstance()).sendBroadcast(intent);
                                uploadedIds.removeAll(successIds);
                                uploadedIds.removeAll(failedIds);
                                transcodedIds.addAll(successIds);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    for (long id : transcodedIds) {
                        HashSet<String> paths = mUploadMusicDao.getPathsBySongId(id, userId);
                        if (paths.size() > 0) {
                            int state = STATE_PUBLISH_FAILED;
                            int failReason = FAIL_SERVER_ERROR;
//                            try {
//                                Object[] results = NovaApi.publishMusic(id);
//                                int code =  (int) results[0];
//                                Log.d(TAG, "publish code: " + code);
//                                MusicInfo finalMusicInfo = (MusicInfo) results[1];
//                                if (code == 200 || code == 201) {
//                                    mUploadMusicDao.deleteBySongId(id, userId);
//                                    state = STATE_PUBLISH_SUCCESS;
//                                } else if (code == 506) {
//                                    failReason = FAIL_LACK_OF_SPACE;
//                                } else if (code == 520 || code == 523) {
//                                    failReason = FAIL_RESTRICT_SONG;
//                                }
//                            } catch (Throwable t) {
//                                t.printStackTrace();
//                                if (t instanceof IOException) {
//                                    failReason = FAIL_NETWORK_ERROR;
//                                } else if (t instanceof WifiOnlyException) {
//                                    state = -1;
//                                } else if (t instanceof AuthException) {
//                                    failReason = FAIL_NOT_LOGIN;
//                                }
//                            }
                            if (state > 0) {
                                if (state == STATE_PUBLISH_FAILED) {
                                    mUploadMusicDao.updateState(paths, STATE_PUBLISH_FAILED, failReason, userId);
                                }
                                Intent intent = new Intent(Constants.BROADCAST_ACTIONS.UPLOAD_MUSIC_PUBLISH);
                                intent.putExtra(EXTRA_PUBLISH_PATHS, paths);
                                intent.putExtra(EXTRA_PUBLISH_STATE, TransferUtils.parcelInt(state, failReason));
                                LocalBroadcastManager.getInstance(NovaApplication.getInstance()).sendBroadcast(intent);
                            }
                        }
                    }
                    if (uploadedIds.size() > 0 || transcodedIds.size() > 0 || (mHandler != null && mHandler.getLooper().getThread().isAlive())) {
                        sendEmptyMessageDelayed(POLL_CHECK, interval);
                    } else {
                        sendEmptyMessageDelayed(POLL_QUIT, 3000);
                    }
                    break;
                case POLL_QUIT:
                    if (!hasMessages(POLL_CHECK)) {
                        getLooper().quit();
                    }
                    break;
            }
        }
    }
}
