package org.xjy.android.nova.transfer.upload.utils;

import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.InputStreamEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RepeatableInputStreamEntity extends BasicHttpEntity {
	private boolean mFirstAttempt = true;
	private InputStreamEntity mInputStreamEntity;
	private InputStream mContent;

	public RepeatableInputStreamEntity(InputStream inputStream, long contentLength) {
		setChunked(false);
		mInputStreamEntity = new InputStreamEntity(inputStream, contentLength);
		mContent = inputStream;
		setContent(mContent);
		setContentLength(contentLength);
	}

	@Override
	public boolean isChunked() {
		return false;
	}

	@Override
	public boolean isRepeatable() {
		return mContent.markSupported() || mInputStreamEntity.isRepeatable();
	}

	@Override
	public void writeTo(OutputStream paramOutputStream) throws IOException {
		if (!mFirstAttempt && isRepeatable()) {
			mContent.reset();
		}
		mFirstAttempt = false;
		mInputStreamEntity.writeTo(paramOutputStream);
	}
}
