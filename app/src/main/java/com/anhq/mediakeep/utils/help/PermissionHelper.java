package com.anhq.mediakeep.utils.help;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class PermissionHelper {
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private final AppCompatActivity activity;
    private final PermissionCallback callback;
    private final ActivityResultLauncher<Intent> storagePermissionLauncher;

    public interface PermissionCallback {
        void onPermissionGranted();

        void onPermissionDenied();
    }

    public PermissionHelper(AppCompatActivity activity, PermissionCallback callback) {
        this.activity = activity;
        this.callback = callback;
        this.storagePermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        handlePermissionResult(Environment.isExternalStorageManager());
                    }
                });
    }

    public void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestManageStoragePermission();
            } else {
                callback.onPermissionGranted();
            }
        } else {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            } else {
                callback.onPermissionGranted();
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void requestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            storagePermissionLauncher.launch(intent);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            handlePermissionResult(granted);
        }
    }

    private void handlePermissionResult(boolean granted) {
        if (granted) {
            callback.onPermissionGranted();
        } else {
            Toast.makeText(activity, "Quyền truy cập bộ nhớ bị từ chối!", Toast.LENGTH_SHORT).show();
            callback.onPermissionDenied();
        }
    }
}