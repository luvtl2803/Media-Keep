package com.anhq.mediakeep.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anhq.mediakeep.data.network.model.MediaMetadata;
import com.anhq.mediakeep.data.repository.MediaRepository;
import com.anhq.mediakeep.data.repository.MediaRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class BackupMediaViewModel extends AndroidViewModel {
    private final MediaRepository mediaRepository;
    private final MutableLiveData<List<MediaMetadata>> backupMediaList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BackupMediaViewModel(@NonNull Application application) {
        super(application);
        mediaRepository = new MediaRepositoryImpl(application.getContentResolver(), application, "image");
        fetchBackupMedia();
    }

    public LiveData<List<MediaMetadata>> getBackupMediaList() {
        return backupMediaList;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchBackupMedia() {
        mediaRepository.getBackupMediaItems(new MediaRepository.OnGetBackupMediaCallback() {
            @Override
            public void onSuccess(List<MediaMetadata> mediaList) {
                backupMediaList.setValue(mediaList);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public void removeBackupMediaItem(MediaMetadata media, int position) {
        mediaRepository.deleteBackupMediaItem(media, new MediaRepository.OnDeleteCallback() {
            @Override
            public void onSuccess() {
                List<MediaMetadata> currentList = backupMediaList.getValue();
                if (currentList != null) {
                    currentList.remove(position);
                    backupMediaList.setValue(currentList);
                }
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue(error);
            }
        });
    }
}