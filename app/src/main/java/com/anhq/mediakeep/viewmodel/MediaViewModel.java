package com.anhq.mediakeep.viewmodel;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anhq.mediakeep.data.model.MediaItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MediaViewModel extends AndroidViewModel {
    private final MutableLiveData<List<MediaItem>> mediaListLiveData = new MutableLiveData<>();
    private final ContentResolver contentResolver;

    public MediaViewModel(Application application) {
        super(application);
        this.contentResolver = application.getContentResolver();
    }

    public LiveData<List<MediaItem>> getMediaList() {
        return mediaListLiveData;
    }

    public void loadMedia(String mediaType) {
        new Thread(() -> {
            List<MediaItem> mediaList = new ArrayList<>();
            Uri contentUri;
            String[] projection = {
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE,
                    MediaStore.MediaColumns.DATE_MODIFIED
            };

            if ("image".equals(mediaType)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(mediaType)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else {
                mediaListLiveData.postValue(mediaList);
                return;
            }

            try (Cursor cursor = contentResolver.query(contentUri, projection, null, null, null)) {
                if (cursor != null) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                    int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
                    int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED);
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idColumn);
                        String name = cursor.getString(nameColumn);
                        long size = cursor.getLong(sizeColumn);
                        long dateModified = cursor.getLong(dateColumn);
                        Uri mediaUri = Uri.withAppendedPath(contentUri, String.valueOf(id));
                        mediaList.add(new MediaItem(mediaUri, name, size, dateModified));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Collections.sort(mediaList, (item1, item2) -> Long.compare(item2.getDateModified(), item1.getDateModified()));

            mediaListLiveData.postValue(mediaList);
        }).start();
    }
}