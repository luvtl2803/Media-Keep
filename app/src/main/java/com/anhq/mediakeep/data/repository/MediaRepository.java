package com.anhq.mediakeep.data.repository;

import com.anhq.mediakeep.data.model.MediaItem;
import com.anhq.mediakeep.data.network.model.MediaMetadata;

import java.util.List;

public interface MediaRepository {
    void backupMediaItems(List<MediaItem> items, OnSyncCallback callback);
    void getBackupMediaItems(OnGetBackupMediaCallback callback);
    void deleteBackupMediaItem(MediaMetadata media, OnDeleteCallback callback);

    interface OnSyncCallback {
        void onSuccess(int syncedCount);
        void onFailure(String error);
    }

    interface OnGetBackupMediaCallback {
        void onSuccess(List<MediaMetadata> mediaList);
        void onFailure(String error);
    }

    interface OnDeleteCallback {
        void onSuccess();
        void onFailure(String error);
    }
}