package com.anhq.mediakeep.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhq.mediakeep.databinding.ItemMediaBinding;
import com.anhq.mediakeep.data.model.MediaItem;
import com.anhq.mediakeep.view.FullScreenImageActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private List<MediaItem> mediaList = new ArrayList<>();

    public void updateMediaList(List<MediaItem> newList) {
        this.mediaList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMediaBinding binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MediaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem mediaItem = mediaList.get(position);
        Glide.with(holder.binding.imageView.getContext())
                .load(mediaItem.getUri())
                .centerCrop()
                .into(holder.binding.imageView);
        holder.binding.tvName.setText(mediaItem.getName());
        holder.binding.tvSize.setText(mediaItem.getFormattedSize());

        holder.binding.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), FullScreenImageActivity.class);
            intent.putExtra("media_item", mediaItem);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        final ItemMediaBinding binding;

        MediaViewHolder(ItemMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}