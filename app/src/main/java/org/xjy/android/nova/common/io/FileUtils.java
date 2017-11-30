package org.xjy.android.nova.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String replaceSpecialCharInFileName(String fileName) {
        StringBuilder sb = new StringBuilder();
        for (char c : fileName.toCharArray()) {
            switch (c) {
                case '?':
                    sb.append('？');
                    break;
                case '"':
                    sb.append('”');
                    break;
                case ':':
                    sb.append('：');
                    break;
                case '+':
                    sb.append('＋');
                    break;
                case '<':
                    sb.append('＜');
                    break;
                case '>':
                    sb.append('＞');
                    break;
                case '[':
                    sb.append('［');
                    break;
                case ']':
                    sb.append('］');
                    break;
                case '\\':
                case '/':
                case '*':
                case '|':
                case '\t':
                    sb.append(' ');
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isFilenameContainsSpecialChar(String filename) {
        Pattern pattern = Pattern.compile("[\\?\":\\+<>\\[\\]\\\\/\\*\\|]+");
        Matcher matcher = pattern.matcher(filename);
        return matcher.find();
    }

    public static void ensureDirectoryExist(File file, boolean isDirectory) {
        boolean directoryExist = true;
        File parent = file.getParentFile();
        File directory = isDirectory ? file : parent;
        while (!directory.exists()) {
            directory = directory.getParentFile();
            directoryExist = false;
        }
        if (!directory.isDirectory()) {
            directory.delete();
        }
        if (!directoryExist) {
            if (isDirectory) {
                file.mkdirs();
            } else {
                parent.mkdirs();
            }
        }
    }
}
