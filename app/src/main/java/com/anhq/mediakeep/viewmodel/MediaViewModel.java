package com.anhq.mediakeep.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anhq.mediakeep.data.model.MediaItem;
import com.anhq.mediakeep.utils.help.MediaStoreHelper;

import java.util.List;

public class MediaViewModel extends AndroidViewModel {
    private final String TAG = "MediaViewModel";
    private final MutableLiveData<List<MediaItem>> mediaListLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<List<MediaItem>>> duplicateMediaLiveData = new MutableLiveData<>();
    private final MediaStoreHelper mediaStoreHelper;

    public MediaViewModel(Application application) {
        super(application);
        this.mediaStoreHelper = MediaStoreHelper.getInstance(application);
        findDuplicateMedia();
    }

    public LiveData<List<MediaItem>> getMediaList() {
        return mediaListLiveData;
    }

    public LiveData<List<List<MediaItem>>> getDuplicateMediaList() {
        return duplicateMediaLiveData;
    }

    public void loadMediaByType(String mediaType) {
        new Thread(() -> {
            try {
                List<MediaItem> mediaList = mediaStoreHelper.getMediaListByType(mediaType);
                mediaList.sort((item1, item2) -> Long.compare(item2.getDateModified(), item1.getDateModified()));
                mediaListLiveData.postValue(mediaList);
            } catch (Exception e) {
                Log.e(TAG, "Error loading media", e);
                mediaListLiveData.postValue(null);
            }
        }).start();
    }

    public void findDuplicateMedia() {
        new Thread(() -> {
            try {
                List<List<MediaItem>> duplicates = mediaStoreHelper.findDuplicateMedia();
                duplicateMediaLiveData.postValue(duplicates);
            } catch (Exception e) {
                Log.e(TAG, "Error finding duplicates", e);
                duplicateMediaLiveData.postValue(null);
            }
        }).start();
    }
}