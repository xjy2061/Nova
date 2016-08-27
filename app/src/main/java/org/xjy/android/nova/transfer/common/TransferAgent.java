package org.xjy.android.nova.transfer.common;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;

import org.xjy.android.nova.NovaApplication;
import org.xjy.android.nova.common.io.IoUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class TransferAgent {
    public static final int JOB_TYPE_IDLE = 0;
    public static final int JOB_TYPE_ADD = 1;
    public static final int JOB_TYPE_DELETE = -1;

    public static final int QUEUE_ADD = 1;
    public static final int QUEUE_REMOVE = -1;
    public static final int QUEUE_PAUSE = -2;
    public static final int QUEUE_AUTO_PAUSE = -3;

    public static final String EXTRA_QUEUE_CHANGE_TYPE = "queue_change_type";
    public static final String EXTRA_QUEUE_CHANGE_IDS = "queue_change_ids";
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_MAX = "max";

    public static class Action {
        public static final int QUIT = 0;
    }

    protected TransferHandler mHandler;
    protected volatile int mDoingJobType;
    protected LinkedBlockingQueue<TransferJob> mTransferQueue = new LinkedBlockingQueue<>();
    protected final HashSet mCurrentJobs = new HashSet();
    protected final HashSet mCurrentFiredJobs = new HashSet();
    protected final HashSet mCurrentSuccessJobs = new HashSet();
    private final Object mLock = new Object();
    private TransferThread mTransferThread;

    public Pair<Integer, Integer> getCurrentProgress() {
        synchronized (mCurrentJobs) {
            return new Pair<>(mCurrentSuccessJobs.size(), mCurrentJobs.size());
        }
    }

    public boolean isTransferring() {
        return mTransferQueue.size() > 0 || isDoingJob();
    }

    public boolean isDoingJob() {
        return  mTransferThread != null && mTransferThread.mCurrentJob != null && mTransferThread.mCurrentJob.mState != TransferJob.STATE_QUIT;
    }

    public Object getHeadJobId() {
        TransferJob job = mTransferQueue.peek();
        if (job != null) {
            return job.getId();
        }
        return null;
    }

    public void shutdown() {
        if (mHandler != null) {
            mHandler.getLooper().quit();
            mHandler = null;
        }
        if (mTransferThread != null) {
            mTransferThread.interrupt();
            mTransferThread = null;
        }
        mTransferQueue.clear();
    }

    protected void initHandler() {
        if (mHandler == null || !mHandler.getLooper().getThread().isAlive()) {
            DispatchThread dispatchThread = new DispatchThread(new ThreadGroup("transfer thread group"));
            dispatchThread.start();
            mHandler = new TransferHandler(dispatchThread.getLooper());
        }
    }

    protected boolean isCurrentJob(TransferJob job) {
        return mTransferThread != null && job.equals(mTransferThread.mCurrentJob) && job.mState != TransferJob.STATE_QUIT;
    }

    protected void quit() {
        mDoingJobType = JOB_TYPE_IDLE;
        synchronized (mLock) {
            mLock.notifyAll();
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(Action.QUIT, 3000);
        }
    }

    protected void notifyQueueChanged(String action, int type, HashSet ids) {
        synchronized (mCurrentJobs) {
            if (type == QUEUE_ADD) {
                mCurrentJobs.addAll(ids);
            } else if (type == QUEUE_REMOVE) {
                mCurrentJobs.removeAll(ids);
                mCurrentFiredJobs.removeAll(ids);
                mCurrentSuccessJobs.removeAll(ids);
            }
            if (mCurrentFiredJobs.size() >= mCurrentJobs.size()) {
                mCurrentJobs.clear();
                mCurrentFiredJobs.clear();
                mCurrentSuccessJobs.clear();
            }
        }
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_QUEUE_CHANGE_TYPE, type);
        intent.putExtra(EXTRA_QUEUE_CHANGE_IDS, ids);
        LocalBroadcastManager.getInstance(NovaApplication.getInstance()).sendBroadcast(intent);
    }

    protected void startTransferThread() {
        if (mTransferThread != null && mTransferThread.isAlive()) {
            mTransferThread.restore();
        } else {
            mTransferThread = new TransferThread();
            mTransferThread.start();
        }
    }

    protected void quitCurrentJob(TransferJob fakeJob) {
        if (mTransferThread != null && fakeJob.equals(mTransferThread.mCurrentJob)) {
            mTransferThread.quitCurrentJob();
        }
    }

    protected Object quitCurrentJob() {
        if (mTransferThread != null) {
            return mTransferThread.quitCurrentJob();
        }
        return null;
    }

    protected Object getCurrentJobId() {
        if (mTransferThread != null && mTransferThread.mCurrentJob != null && mTransferThread.mCurrentJob.getState() != TransferJob.STATE_QUIT) {
            return mTransferThread.mCurrentJob.getId();
        }
        return null;
    }

    protected abstract void handleTransferMessage(Message msg);

    protected abstract boolean hasTransferMessage();

    protected abstract void fireJob(TransferJob job);

    protected void notifyJobStart(TransferJob job) {};

    class DispatchThread extends Thread {
        Looper mLooper;

        DispatchThread(ThreadGroup threadGroup) {
            super(threadGroup, "transfer dispatch thread");
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (this) {
                mLooper = Looper.myLooper();
                notifyAll();
            }
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            Looper.loop();
        }

        public Looper getLooper() {
            if (!isAlive()) {
                return null;
            }

            // If the thread has been started, wait until the looper has been created.
            synchronized (this) {
                while (isAlive() && mLooper == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            return mLooper;
        }
    }

    public class TransferHandler extends Handler {
        TransferHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Action.QUIT) {
                if (!hasTransferMessage() && getLooper().getThread().getThreadGroup().activeCount() <= 1) {
                    getLooper().quit();
                }
            } else {
                handleTransferMessage(msg);
            }
        }
    }

    public static abstract class TransferJob {
        public static final int STATE_SUCCESS = 1;
        public static final int STATE_FAILED = 2;
        public static final int STATE_QUIT = 3;

        private static final int NOTIFY_INTERVAL = 1500;

        protected volatile int mState;
        private long mNotifiedProgress;
        private long mLastNotifyTime;

        public abstract Object getId();

        public abstract void start();

        void quit() {
            mState = STATE_QUIT;
        }

        public int getState() {
            return mState;
        }

        public void notifyProgressChange(String action, Serializable id, long progress, long max) {
            long time = SystemClock.elapsedRealtime();
            if (progress - mNotifiedProgress > IoUtils.BUFFER_SIZE && time - mLastNotifyTime > NOTIFY_INTERVAL) {
                mNotifiedProgress = progress;
                mLastNotifyTime = time;
                Intent intent = new Intent(action);
                intent.putExtra(EXTRA_ID, id);
                intent.putExtra(EXTRA_PROGRESS, progress);
                intent.putExtra(EXTRA_MAX, max);
                LocalBroadcastManager.getInstance(NovaApplication.getInstance()).sendBroadcast(intent);
            }
        }
    }

    class TransferThread extends Thread {
        private volatile boolean mQuit;
        private volatile TransferJob mCurrentJob;

        @Override
        public void run() {
            for (;;) {
                if (mDoingJobType < 0) {
                    synchronized (mLock) {
                        mLock.notifyAll();
                        try {
                            mLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (mQuit) {
                    break;
                }
                TransferJob job = mTransferQueue.poll();
                if (job == null) {
                    break;
                }
                mCurrentJob = job;
                notifyJobStart(job);
                job.start();
                fireJob(job);
                mCurrentJob = null;
            }
            quit();
        }

        public Object quitCurrentJob() {
            Object id = null;
            if (mCurrentJob != null) {
                mCurrentJob.quit();
                id = mCurrentJob.getId();
                mCurrentJob = null;
            }
            return id;
        }

        @Override
        public void interrupt() {
            mQuit = true;
            quitCurrentJob();
            super.interrupt();
        }

        public void restore() {
            mQuit = false;
        }
    }
}
