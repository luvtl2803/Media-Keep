package com.anhq.mediakeep.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.anhq.mediakeep.adapter.BackupMediaAdapter;
import com.anhq.mediakeep.databinding.ActivityBackupMediaBinding;
import com.anhq.mediakeep.viewmodel.BackupMediaViewModel;

import java.util.ArrayList;

public class BackupMediaActivity extends AppCompatActivity {
    private ActivityBackupMediaBinding binding;
    private BackupMediaAdapter adapter;
    private BackupMediaViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBackupMediaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerViewBackupMedia.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BackupMediaAdapter(this, new ArrayList<>());
        binding.recyclerViewBackupMedia.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(BackupMediaViewModel.class);

        viewModel.getBackupMediaList().observe(this, mediaList -> {
            adapter = new BackupMediaAdapter(this, mediaList);
            binding.recyclerViewBackupMedia.setAdapter(adapter);
            adapter.setOnUnsyncClickListener(viewModel::removeBackupMediaItem);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBack.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}