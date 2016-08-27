package org.xjy.android.nova.transfer.upload.utils;

import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class UploadInputStreamEntity extends AbstractHttpEntity {
	private InputStream content;
	private long length;
	private boolean firstAttempt;
	private boolean consumed = false;
	private byte[] prefix;
	private byte[] suffix;

	public UploadInputStreamEntity(InputStream inputStream, long len, String boundary) throws UnsupportedEncodingException {
		this.content = inputStream;
		StringBuilder sb = new StringBuilder();
		sb.append("--" + boundary + "\r\n");
		sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"program.mp3\"").append("\r\n");
		sb.append("Content-Type: ").append("audio/mpeg").append("\r\n");
		sb.append("Content-Transfer-Encoding: binary").append("\r\n").append("\r\n");
		prefix = sb.toString().getBytes("utf-8");
		suffix = ("\r\n--" + boundary + "--\r\n").getBytes("utf-8");
		this.length = len;
	}

	public boolean isRepeatable() {
		return content.markSupported();
	}

	public void writeTo(OutputStream outstream) throws IOException {
		if ((!firstAttempt) && (isRepeatable())) {
			this.content.reset();
		}
		firstAttempt = false;
		
		if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream instream = this.content;
        byte[] buffer = new byte[2048];
        int l;
        if (this.length < 0) {
            // consume until EOF
            while ((l = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, l);
            }
        } else {
        	outstream.write(prefix);
            // consume no more than length
        	long remaining = this.length;
            while (remaining > 0) {
                l = instream.read(buffer, 0, (int)Math.min(2048, remaining));
                if (l == -1) {
                    break;
                }
                outstream.write(buffer, 0, l);
                remaining -= l;
            }
            outstream.write(suffix);
        }
        this.consumed = true;
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return this.content;
	}

	@Override
	public long getContentLength() {
		return this.length + prefix.length + suffix.length;
	}

	// non-javadoc, see interface HttpEntity
    public boolean isStreaming() {
        return !this.consumed;
    }

    // non-javadoc, see interface HttpEntity
    public void consumeContent() throws IOException {
        this.consumed = true;
        // If the input stream is from a connection, closing it will read to
        // the end of the content. Otherwise, we don't care what it does.
        this.content.close();
    }
}
