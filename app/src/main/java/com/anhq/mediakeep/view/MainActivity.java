package com.anhq.mediakeep.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.anhq.mediakeep.databinding.ActivityMainBinding;
import com.anhq.mediakeep.utils.help.StorageManager;
import com.anhq.mediakeep.utils.help.StorageUtils;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private ActivityMainBinding binding;
    private final ActivityResultLauncher<Intent> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    handlePermissionResult(Environment.isExternalStorageManager());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkStoragePermission();
        updateStorageUI();
        setupButtonListeners();
    }

    @SuppressLint("SetTextI18n")
    private void updateStorageUI() {
        long totalStorage = StorageManager.getTotalStorage();
        long usedStorage = StorageManager.getUsedStorage();
        int usedPercentage = StorageManager.getUsedStoragePercentage();

        binding.tvUsedMemory.setText(StorageUtils.convertBytes(usedStorage) + " of "+ StorageUtils.convertBytes(totalStorage));
        binding.progressBar.setProgress(usedPercentage);
    }

    private void setupButtonListeners() {
        binding.btnSelectImage.setOnClickListener(v -> startMediaSelectionActivity("image"));
        binding.btnSelectVideo.setOnClickListener(v -> startMediaSelectionActivity("video"));
    }

    private void startMediaSelectionActivity(String mediaType) {
        Intent intent = new Intent(this, MediaSelectionActivity.class);
        intent.putExtra("media_type", mediaType);
        startActivity(intent);
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestManageStoragePermission();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestManageStoragePermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        storagePermissionLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            handlePermissionResult(granted);
        }
    }

    private void handlePermissionResult(boolean granted) {
        if (granted) {
            updateStorageUI();
        } else {
            Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối!", Toast.LENGTH_SHORT).show();
        }
    }
}