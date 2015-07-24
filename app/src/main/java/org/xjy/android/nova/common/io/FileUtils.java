package org.xjy.android.nova.common.io;

import java.io.File;

public class FileUtils {

    public static boolean isFileStrictExist(String path) {
        return new File(path).length() > 0;
    }
}
