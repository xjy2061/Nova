package org.xjy.android.nova.transfer.upload.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SubInputStream extends FilterInputStream {
	private final long mRequestedOffset;
	private final long mRequestedLength;
	private final boolean mCloseSourceStream;
	private long mCurrentPosition;
	private long mMarkedPosition;

	public SubInputStream(InputStream inputStream, long offset, long length, boolean closeSourceStream) {
		super(inputStream);
		mRequestedOffset = offset;
		mRequestedLength = length;
		mCloseSourceStream = closeSourceStream;
	}

	@Override
	public int read() throws IOException {
		byte[] buffer = new byte[1];
		int count = read(buffer, 0, 1);
		if (count == -1) {
			return count;
		}
		return buffer[0];
	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		for (long skipped = 0; mCurrentPosition < mRequestedOffset; mCurrentPosition += skipped) {
			skipped = super.skip(mRequestedOffset - mCurrentPosition);
		}
		long remain = mRequestedLength + mRequestedOffset - mCurrentPosition;
		if (remain <= 0) {
			return -1;
		}
		byteCount = (int) Math.min(byteCount, remain);
		int count = super.read(buffer, byteOffset, byteCount);
		mCurrentPosition += count;
		return count;
	}

	@Override
	public synchronized void mark(int readLimit) {
		mMarkedPosition = mCurrentPosition;
		super.mark(readLimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		mCurrentPosition = mMarkedPosition;
		super.reset();
	}

	@Override
	public void close() throws IOException {
		if (mCloseSourceStream) {
			super.close();
		}
	}

	@Override
	public int available() throws IOException {
		long remain;
		if (mCurrentPosition < mRequestedOffset) {
			remain = mRequestedLength;
		} else {
			remain = mRequestedLength + mRequestedOffset - mCurrentPosition;
		}
		return (int) Math.min(remain, super.available());
	}
}
