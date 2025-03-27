package com.anhq.mediakeep.view;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.anhq.mediakeep.adapter.DuplicateMediaAdapter;
import com.anhq.mediakeep.databinding.ActivityDuplicateFinderBinding;
import com.anhq.mediakeep.viewmodel.MediaViewModel;

public class DuplicateFinderActivity extends AppCompatActivity {
    private ActivityDuplicateFinderBinding binding;
    private DuplicateMediaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDuplicateFinderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MediaViewModel viewModel = new ViewModelProvider(this).get(MediaViewModel.class);

        binding.rvDuplicates.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DuplicateMediaAdapter();
        binding.rvDuplicates.setAdapter(adapter);

        viewModel.getDuplicateMediaList().observe(this, duplicateGroups -> {
            if (duplicateGroups != null) {
                adapter.setDuplicateGroups(duplicateGroups);
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
}