package com.anhq.mediakeep.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.anhq.mediakeep.databinding.ActivityMainBinding;
import com.anhq.mediakeep.utils.help.ConvertBytesToBigger;
import com.anhq.mediakeep.utils.help.PermissionHelper;
import com.anhq.mediakeep.utils.help.StorageManager;

public class MainActivity extends AppCompatActivity implements PermissionHelper.PermissionCallback {
    private ActivityMainBinding binding;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        permissionHelper = new PermissionHelper(this, this);
        permissionHelper.checkStoragePermission();

        updateStorageUI();
        setupButtonListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateStorageUI() {
        long totalStorage = StorageManager.getTotalStorage();
        long usedStorage = StorageManager.getUsedStorage();
        int usedPercentage = StorageManager.getUsedStoragePercentage();

        binding.tvUsedStorage.setText(ConvertBytesToBigger.convertBytes(usedStorage) + " of " + ConvertBytesToBigger.convertBytes(totalStorage));
        binding.progressBar.setProgress(usedPercentage);
    }

    private void setupButtonListeners() {
        binding.btnSelectImage.setOnClickListener(v -> startMediaSelectionActivity("image"));
        binding.btnSelectVideo.setOnClickListener(v -> startMediaSelectionActivity("video"));
        binding.btnFindDuplicates.setOnClickListener(v -> startFindDuplicatesActivity());
        binding.btnBackup.setOnClickListener(v -> startBackupActivity());
    }

    private void startBackupActivity() {
        Intent intent = new Intent(this, BackupMediaActivity.class);
        startActivity(intent);
    }

    private void startMediaSelectionActivity(String mediaType) {
        Intent intent = new Intent(this, MediaSelectionActivity.class);
        intent.putExtra("media_type", mediaType);
        startActivity(intent);
    }

    private void startFindDuplicatesActivity() {
        Intent intent = new Intent(this, DuplicateFinderActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionGranted() {
        updateStorageUI();
    }

    @Override
    public void onPermissionDenied() {

    }
}