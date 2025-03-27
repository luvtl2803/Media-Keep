package com.anhq.mediakeep.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.anhq.mediakeep.BuildConfig;
import com.anhq.mediakeep.data.model.MediaItem;
import com.anhq.mediakeep.data.network.api.SupabaseApi;
import com.anhq.mediakeep.data.network.model.MediaMetadata;
import com.anhq.mediakeep.utils.help.JwtUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MediaRepositoryImpl implements MediaRepository {
    private static final String SUPABASE_URL = "https://naubvopylgzvanprvkny.supabase.co/";
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_ANON_KEY;
    private final SupabaseApi api;
    private final ContentResolver contentResolver;
    private final Context context;
    private final String mediaType;

    public MediaRepositoryImpl(ContentResolver contentResolver, Context context, String mediaType) {
        this.contentResolver = contentResolver;
        this.context = context;
        this.mediaType = mediaType;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.api = retrofit.create(SupabaseApi.class);
    }

    @Override
    public void backupMediaItems(List<MediaItem> items, OnSyncCallback callback) {
        int[] syncedCount = {0};
        backupNextItem(items, 0, syncedCount, callback);
    }

    private void backupNextItem(List<MediaItem> items, int index, int[] syncedCount, OnSyncCallback callback) {
        if (index >= items.size()) {
            callback.onSuccess(syncedCount[0]);
            return;
        }

        MediaItem item = items.get(index);
        File file = uriToFile(item.getUri());
        if (file == null) {
            backupNextItem(items, index + 1, syncedCount, callback);
            return;
        }

        RequestBody requestBody = RequestBody.create(file, okhttp3.MediaType.parse("image/*"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", item.getName(), requestBody);
        String fileName = item.getName();

        Log.d("MediaRepository", "Uploading file: " + fileName);
        String authHeader = "Bearer " + SUPABASE_KEY;
        if (JwtUtils.isJwtExpired(SUPABASE_KEY)) {
            Log.e("MediaRepository", "JWT has expired. Request blocked.");
            callback.onFailure("JWT has expired");
            return;
        }
        api.uploadFile(authHeader, SUPABASE_KEY, filePart, fileName)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("MediaRepository", "Upload successful for: " + fileName);
                            MediaMetadata metadata = new MediaMetadata(
                                    item.getName(), "media/" + item.getName(), mediaType, item.getSize(), item.getDateModified()
                            );
                            Log.d("MediaRepository", "Saving metadata for: " + fileName);
                            api.saveMetadata("Bearer " + SUPABASE_KEY, SUPABASE_KEY, "application/json", metadata)
                                    .enqueue(new Callback<>() {
                                        @Override
                                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                            if (response.isSuccessful()) {
                                                Log.d("MediaRepository", "Metadata saved for: " + fileName);
                                                syncedCount[0]++;
                                                file.delete();
                                            } else {
                                                Log.e("MediaRepository", "Save metadata failed: " + response.code() + " " + response.message());
                                                try {
                                                    Log.e("MediaRepository", "Error body: " + response.errorBody().string());
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            backupNextItem(items, index + 1, syncedCount, callback);
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                            Log.e("MediaRepository", "Save metadata failed: " + t.getMessage());
                                            callback.onFailure(t.getMessage());
                                        }
                                    });
                        } else {
                            Log.e("MediaRepository", "Upload failed: " + response.code() + " " + response.message());
                            if (response.errorBody() != null) {
                                try {
                                    Log.e("MediaRepository", "Error body: " + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e("MediaRepository", "No error body, possible network issue.");
                            }
                            callback.onFailure("Upload failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e("MediaRepository", "Upload failed: " + t.getMessage());
                        callback.onFailure(t.getMessage());
                    }
                });
    }

    @Override
    public void getBackupMediaItems(OnGetBackupMediaCallback callback) {
        api.getSyncedMedia("Bearer " + SUPABASE_KEY, SUPABASE_KEY, "*", "eq." + mediaType)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<MediaMetadata>> call, @NonNull Response<List<MediaMetadata>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onFailure("Failed to fetch synced media: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<MediaMetadata>> call, @NonNull Throwable t) {
                        callback.onFailure(t.getMessage());
                    }
                });
    }

    @Override
    public void deleteBackupMediaItem(MediaMetadata media, OnDeleteCallback callback) {
        api.deleteFile("Bearer " + SUPABASE_KEY, SUPABASE_KEY, media.getFile_path())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful() || response.code() == 404) {
                            // Xóa metadata từ bảng media_items
                            api.deleteMetadata("Bearer " + SUPABASE_KEY, SUPABASE_KEY, "application/json", "eq." + media.getFile_path())
                                    .enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                            if (response.isSuccessful()) {
                                                callback.onSuccess();
                                            } else {
                                                try {
                                                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                                    callback.onFailure("Failed to delete metadata: " + response.code() + " - " + errorBody);
                                                } catch (IOException e) {
                                                    callback.onFailure(e.getMessage());
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                            callback.onFailure(t.getMessage());
                                        }
                                    });
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                callback.onFailure("Failed to delete file: " + response.code() + " - " + errorBody);
                            } catch (IOException e) {
                                callback.onFailure(e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        callback.onFailure(t.getMessage());
                    }
                });
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            File file = new File(context.getCacheDir(), "temp_" + System.currentTimeMillis() + uri.getLastPathSegment());
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}