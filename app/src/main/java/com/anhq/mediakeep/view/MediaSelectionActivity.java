package com.anhq.mediakeep.view;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.anhq.mediakeep.R;
import com.anhq.mediakeep.adapter.MediaAdapter;
import com.anhq.mediakeep.data.model.MediaItem;
import com.anhq.mediakeep.databinding.ActivityMediaSelectionBinding;
import com.anhq.mediakeep.viewmodel.MediaViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MediaSelectionActivity extends AppCompatActivity {
    private ActivityMediaSelectionBinding binding;
    private MediaAdapter mediaAdapter;
    private MediaViewModel viewModel;
    private String mediaType;
    private int currentSortMode = 0; // 0: Date Desc, 1: Date Asc, 2: Size Asc, 3: Size Desc
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mediaType = getIntent().getStringExtra("media_type");

        viewModel = new ViewModelProvider(this).get(MediaViewModel.class);

        setupRecyclerView();

        viewModel.getMediaList().observe(this, mediaList -> {
            mediaAdapter.updateMediaList(sortMediaList(mediaList));
        });

        viewModel.loadMedia(mediaType);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_dropdown_item_1line);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        binding.spinnerSort.setAdapter(adapter);
        binding.spinnerSort.setText(adapter.getItem(0), false);
        currentSortMode = 0;

        binding.spinnerSort.setOnItemClickListener((parent, view, position, id) -> {
            currentSortMode = position;
            viewModel.loadMedia(mediaType);
        });

        mediaAdapter.setOnSelectionChangeListener(selectedItems -> {
            binding.bottomToolbar.setVisibility(selectedItems.isEmpty() ? View.GONE : View.VISIBLE);
            binding.btnRename.setVisibility(selectedItems.size() == 1 ? View.VISIBLE : View.GONE);
        });

        handleButton();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSelectionMode) {
                    toggleSelectionMode(false);
                } else {
                    finish();
                }
            }
        });
    }

    private void handleButton() {
        binding.btnBack.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelectionMode(false);
            } else {
                finish();
            }
        });

        binding.btnSelectMode.setOnClickListener(v -> {
            isSelectionMode = !isSelectionMode;
            toggleSelectionMode(isSelectionMode);
        });

        binding.btnDelete.setOnClickListener(v -> {
            List<MediaItem> selectedItems = mediaAdapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete " + selectedItems.size() + " item(s)? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        ContentResolver resolver = getContentResolver();
                        for (MediaItem item : selectedItems) {
                            try {
                                resolver.delete(item.getUri(), null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        mediaAdapter.clearSelections();
                        viewModel.loadMedia(mediaType);
                        Toast.makeText(this, "Deleted " + selectedItems.size() + " items", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
        });

        binding.btnShare.setOnClickListener(v -> {
            List<MediaItem> selectedItems = mediaAdapter.getSelectedItems();
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("video".equals(mediaType) ? "video/*" : "image/*");
            ArrayList<Uri> uris = new ArrayList<>();
            for (MediaItem item : selectedItems) {
                uris.add(item.getUri());
            }
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            startActivity(Intent.createChooser(shareIntent, "Share " + mediaType + "s"));
        });

        binding.btnRename.setOnClickListener(v -> {
            List<MediaItem> selectedItems = mediaAdapter.getSelectedItems();
            if (selectedItems.size() == 1) {
                MediaItem item = selectedItems.get(0);
                showRenameDialog(item);
            }
        });
    }

    private void toggleSelectionMode(boolean enable) {
        isSelectionMode = enable;
        mediaAdapter.toggleSelectionMode(enable);
        binding.btnSelectMode.setChecked(enable);
        binding.btnSelectMode.setText(enable ? "Cancel" : "Select");
    }

    private List<MediaItem> sortMediaList(List<MediaItem> mediaList) {
        List<MediaItem> sortedList = new ArrayList<>(mediaList);
        switch (currentSortMode) {
            case 0: // Date Descending
                sortedList.sort((item1, item2) -> Long.compare(item2.getDateModified(), item1.getDateModified()));
                break;
            case 1: // Date Ascending
                sortedList.sort(Comparator.comparingLong(MediaItem::getDateModified));
                break;
            case 2: // Size Ascending
                sortedList.sort(Comparator.comparingLong(MediaItem::getSize));
                break;
            case 3: // Size Descending
                sortedList.sort((item1, item2) -> Long.compare(item2.getSize(), item1.getSize()));
                break;
        }
        return sortedList;
    }

    private void showRenameDialog(MediaItem item) {
        EditText editText = new EditText(this);
        editText.setText(item.getName());

        new AlertDialog.Builder(this)
                .setTitle("Rename")
                .setView(editText)
                .setPositiveButton("OK", (dialog, which) -> {
                    String newName = editText.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(item.getName())) {
                        renameMediaItem(item, newName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void renameMediaItem(MediaItem item, String newName) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();

        String extension = item.getName().substring(item.getName().lastIndexOf("."));
        if (!newName.endsWith(extension)) {
            newName += extension;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, newName);
                int updated = resolver.update(item.getUri(), values, null, null);

                if (updated > 0) {
                    item.setName(newName);
                    mediaAdapter.clearSelections();
                    Toast.makeText(this, "Renamed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to rename", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Rename not supported on this Android version", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error renaming: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            viewModel.loadMedia(mediaType);
        }
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mediaAdapter = new MediaAdapter(mediaType);
        binding.recyclerView.setAdapter(mediaAdapter);
    }
}