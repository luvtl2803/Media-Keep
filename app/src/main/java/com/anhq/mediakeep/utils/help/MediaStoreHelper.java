package com.anhq.mediakeep.utils.help;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.anhq.mediakeep.data.model.MediaItem;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaStoreHelper {
    private Context context;
    private ContentResolver contentResolver;
    private static MediaStoreHelper instance;

    public static MediaStoreHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MediaStoreHelper(context);
        }
        return instance;
    }

    private MediaStoreHelper(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    public List<MediaItem> getMediaListByType(String mediaType) {
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
            return mediaList;
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
        return mediaList;
    }

    public List<MediaItem> getAllMedia() {
        List<MediaItem> mediaList = new ArrayList<>();
        mediaList.addAll(getMediaList(MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        mediaList.addAll(getMediaList(MediaStore.Video.Media.EXTERNAL_CONTENT_URI));
        return mediaList;
    }

    private List<MediaItem> getMediaList(Uri contentUri) {
        List<MediaItem> mediaList = new ArrayList<>();
        String[] projection = {
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED
        };

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
        return mediaList;
    }

    public String getImagePathFromUri(@NonNull Uri imageUri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = contentResolver.query(imageUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (path == null) {
            path = getPathFromMediaStore(imageUri);
        }
        return path != null ? path : imageUri.toString();
    }

    private String getPathFromMediaStore(@NonNull Uri imageUri) {
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.RELATIVE_PATH};
        try (Cursor cursor = contentResolver.query(imageUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH));
                return Environment.getExternalStorageDirectory() + "/" + relativePath + displayName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<List<MediaItem>> findDuplicateMedia() {
        List<MediaItem> allMedia = getAllMedia();
        Map<String, List<MediaItem>> hashMap = new HashMap<>();
        List<List<MediaItem>> duplicates = new ArrayList<>();

        for (MediaItem item : allMedia) {
            try {
                String hash = calculateMediaHash(item.getUri());
                hashMap.computeIfAbsent(hash, k -> new ArrayList<>()).add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (List<MediaItem> group : hashMap.values()) {
            if (group.size() > 1) {
                duplicates.add(group);
            }
        }

        return duplicates;
    }

    private String calculateMediaHash(Uri mediaUri) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = contentResolver.openInputStream(mediaUri)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}