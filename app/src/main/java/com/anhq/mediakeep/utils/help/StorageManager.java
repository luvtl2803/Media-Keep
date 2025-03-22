package com.anhq.mediakeep.utils.help;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class StorageManager {
    /**
     * Tính phần trăm bộ nhớ đã dùng dựa trên internal storage (khớp với Settings)
     */
    public static int getUsedStoragePercentage() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long totalStorage = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
        long availableStorage = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
        long usedStorage = totalStorage - availableStorage;

        int percentage = (int) (((double) usedStorage / totalStorage) * 100);

        Log.d("StorageInfo", "Total: " + StorageUtils.convertBytes(totalStorage));
        Log.d("StorageInfo", "Used: " + StorageUtils.convertBytes(usedStorage));
        Log.d("StorageInfo", "Percentage: " + percentage + "%");

        return percentage;
    }

    /**
     * Lấy dung lượng đã dùng (bytes)
     */
    public static long getUsedStorage() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long totalStorage = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
        long availableStorage = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
        return totalStorage - availableStorage;
    }

    /**
     * Lấy tổng dung lượng (bytes)
     */
    public static long getTotalStorage() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return statFs.getBlockSizeLong() * statFs.getBlockCountLong();
    }

    /**
     * Tính phần trăm bao gồm cả /system (không khuyến khích nếu muốn khớp Settings)
     */
    public static int getTotalSystemUsedPercentage() {
        long totalStorage = 0;
        long usedStorage = 0;

        // /system
        StatFs statFsRoot = new StatFs(Environment.getRootDirectory().getPath());
        long totalRoot = statFsRoot.getBlockSizeLong() * statFsRoot.getBlockCountLong();
        long availableRoot = statFsRoot.getBlockSizeLong() * statFsRoot.getAvailableBlocksLong();
        totalStorage += totalRoot;
        usedStorage += (totalRoot - availableRoot);

        // /storage/emulated/0
        StatFs statFsExternal = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long totalExternal = statFsExternal.getBlockSizeLong() * statFsExternal.getBlockCountLong();
        long availableExternal = statFsExternal.getBlockSizeLong() * statFsExternal.getAvailableBlocksLong();
        totalStorage += totalExternal;
        usedStorage += (totalExternal - availableExternal);

        return (int) (((double) usedStorage / totalStorage) * 100);
    }
}