package com.anhq.mediakeep.utils.help;

import android.annotation.SuppressLint;

public class ConvertBytesToBigger {
    private static final long KB = 1024L;
    private static final long MB = KB * 1024L;
    private static final long GB = MB * 1024L;
    @SuppressLint("DefaultLocale")
    public static String convertBytes(long bytes) {
        double kb = bytes / (double) KB;
        double mb = bytes / (double) MB;
        double gb = bytes / (double) GB;

        if (gb >= 1) {
            return String.format("%.2f GB", gb);
        } else if (mb >= 1) {
            return String.format("%.2f MB", mb);
        } else if (kb >= 1) {
            return String.format("%.2f KB", kb);
        } else {
            return bytes + " bytes";
        }
    }
    public static double toGB(long bytes) {
        return bytes / (double) GB;
    }
    public static double toMB(long bytes) {
        return bytes / (double) MB;
    }
    public static double toKB(long bytes) {
        return bytes / (double) KB;
    }
}
