package org.xjy.android.nova.common.io;

import com.facebook.common.util.ByteConstants;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class IoUtils {
    public static final int BUFFER_SIZE = 8 * ByteConstants.KB;

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable t) {
                //
            }
        }
    }

    public static String readLine(InputStream in) throws IOException {
        StringBuilder result = new StringBuilder();
        for (int c; (c = in.read()) != -1 && c != '\n';) {
            if (c != '\r') {
                result.append((char) c);
            }
        }
        return result.toString();
    }
}
