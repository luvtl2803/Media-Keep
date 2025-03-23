package com.anhq.mediakeep.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhq.mediakeep.data.model.MediaItem;
import com.anhq.mediakeep.databinding.ItemMediaBinding;
import com.anhq.mediakeep.view.FullScreenImageActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private final List<MediaItem> mediaList = new ArrayList<>();
    private final List<MediaItem> selectedItems = new ArrayList<>();
    private final String mediaType;
    private Consumer<List<MediaItem>> onSelectionChangeListener;
    private boolean isSelectionMode = false;

    public MediaAdapter(String mediaType) {
        this.mediaType = mediaType;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMediaBinding binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MediaViewHolder(binding, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = mediaList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateMediaList(List<MediaItem> newMediaList) {
        mediaList.clear();
        mediaList.addAll(newMediaList);
        notifyDataSetChanged();
    }

    public List<MediaItem> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void toggleSelectionMode(boolean enable) {
        isSelectionMode = enable;
        selectedItems.clear();
        notifyDataSetChanged();
        notifySelectionChange();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
        notifySelectionChange();
    }

    public void setOnSelectionChangeListener(Consumer<List<MediaItem>> listener) {
        this.onSelectionChangeListener = listener;
    }

    private void notifySelectionChange() {
        if (onSelectionChangeListener != null) {
            onSelectionChangeListener.accept(selectedItems);
        }
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        private final ItemMediaBinding binding;
        private final MediaAdapter adapter;

        MediaViewHolder(@NonNull ItemMediaBinding binding, MediaAdapter adapter) {
            super(binding.getRoot());
            this.binding = binding;
            this.adapter = adapter;
        }

        void bind(MediaItem item) {
            Glide.with(binding.imageView.getContext())
                    .load(item.getUri())
                    .centerCrop()
                    .into(binding.imageView);

            boolean isVideo = "video".equals(adapter.mediaType);
            binding.playIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);

            binding.tvName.setText(item.getName());
            binding.tvSize.setText(item.getFormattedSize());

            binding.getRoot().setOnClickListener(v -> {
                if (adapter.isSelectionMode) {
                    toggleSelection(item);
                } else if (isVideo) {
                    playVideo(item.getUri(), binding.getRoot().getContext());
                } else {
                    viewImage(item, binding.getRoot().getContext());
                }
            });

            binding.checkBox.setVisibility(adapter.isSelectionMode ? View.VISIBLE : View.GONE);
            binding.checkBox.setChecked(adapter.selectedItems.contains(item));
            binding.checkBox.setOnClickListener(v -> toggleSelection(item));
        }

        private void toggleSelection(MediaItem item) {
            if (adapter.selectedItems.contains(item)) {
                adapter.selectedItems.remove(item);
            } else {
                adapter.selectedItems.add(item);
            }
            adapter.notifyItemChanged(getBindingAdapterPosition());
            adapter.notifySelectionChange();
        }

        private void playVideo(Uri videoUri, Context context) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(videoUri, "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        }

        private void viewImage(MediaItem item, Context context) {
            Intent intent = new Intent(context, FullScreenImageActivity.class);
            intent.putExtra("media_item", item);
            context.startActivity(intent);
        }
    }
}