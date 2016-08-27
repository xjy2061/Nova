package org.xjy.android.nova.transfer.upload.utils;

import org.xjy.android.nova.common.io.IoUtils;
import org.xjy.android.nova.transfer.common.QuitGuard;
import org.xjy.android.nova.transfer.common.TransferException;
import org.xjy.android.nova.utils.EncryptUtils;

import java.io.File;
import java.io.FileInputStream;

public class UploadUtils {
    public static String calcMD5(File file, QuitGuard quitGuard) throws Throwable {
        MD5CalculateInputStream inputStream = new MD5CalculateInputStream(new FileInputStream(file));
        try {
            byte[] buffer = new byte[IoUtils.BUFFER_SIZE];
            while (inputStream.read(buffer) != -1) {
                if (quitGuard != null && quitGuard.isQuit()) {
                    throw new TransferException(TransferException.TYPE_QUIT);
                }
            }
            return EncryptUtils.bytesToHex(inputStream.getMd5Digest());
        } finally {
            IoUtils.closeSilently(inputStream);
        }
    }
}
