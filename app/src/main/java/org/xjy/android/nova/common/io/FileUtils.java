package org.xjy.android.nova.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static boolean isFileStrictExist(String path) {
        return new File(path).length() > 0;
    }

    public static boolean copy(String src, String dest, boolean enableEmptySrc) {
        FileInputStream in = null;
        FileOutputStream out = null;
        File destFile = null;
        try {
            File srcFile = new File(src);
            if ((!enableEmptySrc) && srcFile.length() == 0) {
                return false;
            }
            destFile = new File(dest);
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[IoUtils.BUFFER_SIZE];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            out.flush();
            if (srcFile.length() != destFile.length()) {
                throw new IOException("Failed to copy full contents from '" + src + "' to '" + dest + "'");
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            destFile.delete();
            return false;
        } finally {
            IoUtils.closeSilently(in);
            IoUtils.closeSilently(out);
        }
    }
}
