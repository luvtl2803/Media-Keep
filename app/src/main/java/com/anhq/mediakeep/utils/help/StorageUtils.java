package com.anhq.mediakeep.utils.help;

public class StorageUtils {
    // Constants for conversion
    private static final long KB = 1024L;
    private static final long MB = KB * 1024L;
    private static final long GB = MB * 1024L;

    /**
     * Chuyển đổi bytes thành đơn vị phù hợp (KB, MB, GB)
     * @param bytes Số bytes cần chuyển đổi
     * @return Chuỗi định dạng với đơn vị phù hợp
     */
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

    /**
     * Chuyển đổi bytes thành GB
     * @param bytes Số bytes
     * @return Giá trị GB với 2 chữ số thập phân
     */
    public static double toGB(long bytes) {
        return bytes / (double) GB;
    }

    /**
     * Chuyển đổi bytes thành MB
     * @param bytes Số bytes
     * @return Giá trị MB với 2 chữ số thập phân
     */
    public static double toMB(long bytes) {
        return bytes / (double) MB;
    }

    /**
     * Chuyển đổi bytes thành KB
     * @param bytes Số bytes
     * @return Giá trị KB với 2 chữ số thập phân
     */
    public static double toKB(long bytes) {
        return bytes / (double) KB;
    }
}
