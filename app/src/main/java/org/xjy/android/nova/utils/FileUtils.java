package org.xjy.android.nova.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {

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
}
