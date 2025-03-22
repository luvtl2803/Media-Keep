package com.anhq.mediakeep.view;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhq.mediakeep.R;
import com.anhq.mediakeep.adapter.MediaAdapter;
import com.anhq.mediakeep.databinding.ActivityMediaSelectionBinding;
import com.anhq.mediakeep.viewmodel.MediaViewModel;

public class MediaSelectionActivity extends AppCompatActivity {
    private ActivityMediaSelectionBinding binding;
    private MediaAdapter mediaAdapter;
    private MediaViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String mediaType = getIntent().getStringExtra("media_type");

        viewModel = new ViewModelProvider(this).get(MediaViewModel.class);

        setupRecyclerView();

        viewModel.getMediaList().observe(this, mediaList -> {
            mediaAdapter.updateMediaList(mediaList);
        });

        viewModel.loadMedia(mediaType);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mediaAdapter = new MediaAdapter();
        binding.recyclerView.setAdapter(mediaAdapter);
    }

}