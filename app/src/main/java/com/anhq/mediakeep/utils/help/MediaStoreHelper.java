package com.anhq.mediakeep.utils.help;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.anhq.mediakeep.data.model.MediaItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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

    public List<MediaItem> getMediaList(String mediaType) {
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

    public Uri moveImageToMediaStore(@NonNull File imageFile, String destinationFolder) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.getName());
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + destinationFolder);
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
        } else {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), destinationFolder);
            if (!directory.exists()) directory.mkdirs();
            contentValues.put(MediaStore.Images.Media.DATA, new File(directory, imageFile.getName()).getAbsolutePath());
        }

        Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri != null) {
            try (OutputStream outputStream = contentResolver.openOutputStream(uri);
                 InputStream inputStream = new FileInputStream(imageFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear();
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                contentResolver.update(uri, contentValues, null, null);
            }
        }
        return uri;
    }
}