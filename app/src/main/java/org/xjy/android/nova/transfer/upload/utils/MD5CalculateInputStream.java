package org.xjy.android.nova.transfer.upload.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5CalculateInputStream extends FilterInputStream {
	private MessageDigest mDigest;

	public MD5CalculateInputStream(InputStream inputStream) throws NoSuchAlgorithmException {
		super(inputStream);
		mDigest = MessageDigest.getInstance("MD5");
	}

	public byte[] getMd5Digest() {
		return mDigest.digest();
	}

	@Override
	public synchronized void reset() throws IOException {
		try {
			mDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}
		in.reset();
	}

	@Override
	public int read() throws IOException {
		int data = in.read();
		if (data != -1) {
			mDigest.update((byte) data);
		}
		return data;
	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		int count = in.read(buffer, byteOffset, byteCount);
		if (count != -1) {
			mDigest.update(buffer, byteOffset, count);
		}
		return count;
	}
}
