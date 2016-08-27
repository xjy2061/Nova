package org.xjy.android.nova.transfer.upload.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class RepeatableFileInputStream extends InputStream {
	private File mFile = null;
	private FileInputStream mFileInputStream = null;
	private long mBytesReadPastMarkPoint = 0;
	private long mMarkPoint = 0;

	public RepeatableFileInputStream(File file) throws FileNotFoundException {
		if (file == null) {
			throw new IllegalArgumentException("File cannot be null");
		}
		mFileInputStream = new FileInputStream(file);
		mFile = file;
	}

	@Override
	public void reset() throws IOException {
		mFileInputStream.close();
		mFileInputStream = new FileInputStream(mFile);
		long skipped = 0;
		for (long toSkip = mMarkPoint; toSkip > 0; toSkip -= skipped) {
			skipped = mFileInputStream.skip(toSkip);
		}
		mBytesReadPastMarkPoint = 0;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readlimit) {
		mMarkPoint += mBytesReadPastMarkPoint;
		mBytesReadPastMarkPoint = 0;
	}

	@Override
	public int available() throws IOException {
		return mFileInputStream.available();
	}

	@Override
	public void close() throws IOException {
		mFileInputStream.close();
	}

	@Override
	public int read() throws IOException {
		int bytesRead = mFileInputStream.read();
		if (bytesRead != -1) {
			mBytesReadPastMarkPoint += 1;
			return bytesRead;
		}
		return -1;
	}

	@Override
	public long skip(long byteCount) throws IOException {
		long skipped = mFileInputStream.skip(byteCount);
		mBytesReadPastMarkPoint += skipped;
		return skipped;
	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		int count = mFileInputStream.read(buffer, byteOffset, byteCount);
		mBytesReadPastMarkPoint += count;
		return count;
	}

	public InputStream getWrappedInputStream() {
		return mFileInputStream;
	}
}
