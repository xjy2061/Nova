package org.xjy.android.nova.common.io;

import java.io.Closeable;

public class IoUtils {

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable t) {
                //
            }
        }
    }
}
