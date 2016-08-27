package org.xjy.android.nova.transfer.upload.utils;

import org.xjy.android.nova.transfer.common.ProgressListener;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressNotifyInputStream extends FilterInputStream {
	private final ProgressListener mListener;
	private int mUnNotifiedByteCount;
	private int mNotifyThreshold;
	private boolean mFireCompletedMsg;

	public ProgressNotifyInputStream(InputStream inputStream, ProgressListener progressListener) {
		super(inputStream);
		mListener = progressListener;
	}

	public void setNotifyThreshold(int notifyThreshold) {
		mNotifyThreshold = notifyThreshold;
	}

	public void setFireCompletedMsg(boolean fireCompletedMsg) {
		mFireCompletedMsg = fireCompletedMsg;
	}

	@Override
	public int read() throws IOException {
		int data = super.read();
		if (data == -1) {
			notifyCompleted();
		}
		if (data != -1) {
			notify(1);
		}
		return data;
	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		int count = super.read(buffer, byteOffset, byteCount);
		if (count == -1) {
			notifyCompleted();
		}
		if (count != -1) {
			notify(count);
		}
		return count;
	}

	@Override
	public void close() throws IOException {
		if (mUnNotifiedByteCount > 0) {
			mUnNotifiedByteCount = 0;
		}
		super.close();
	}

	private void notifyCompleted() {
		if (!mFireCompletedMsg) {
			return;
		}
		mUnNotifiedByteCount = 0;
		mListener.onProgressChanged(-1, 0);
	}

	private void notify(int byteCount) {
		mUnNotifiedByteCount += byteCount;
		if (mUnNotifiedByteCount >= mNotifyThreshold) {
			mListener.onProgressChanged(mUnNotifiedByteCount, 0);
			mUnNotifiedByteCount = 0;
		}
	}
}
