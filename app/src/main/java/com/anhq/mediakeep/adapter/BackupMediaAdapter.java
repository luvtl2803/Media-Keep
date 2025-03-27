package com.anhq.mediakeep.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhq.mediakeep.R;
import com.anhq.mediakeep.data.network.model.MediaMetadata;
import com.anhq.mediakeep.databinding.ItemBackupMediaBinding;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupMediaAdapter extends RecyclerView.Adapter<BackupMediaAdapter.ViewHolder> {
    private final Context context;
    private final List<MediaMetadata> mediaList;
    private static final String BASE_URL = "https://naubvopylgzvanprvkny.supabase.co/storage/v1/object/public/media-keep/";
    private OnUnsyncClickListener unsyncClickListener;

    public BackupMediaAdapter(Context context, List<MediaMetadata> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    public void setOnUnsyncClickListener(OnUnsyncClickListener listener) {
        this.unsyncClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBackupMediaBinding binding = ItemBackupMediaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaMetadata media = mediaList.get(position);
        holder.binding.textViewFileName.setText(media.getFile_name());

        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(media.getDate_modified() * 1000));
        holder.binding.textViewDateModified.setText("Modified: " + dateStr);
        holder.binding.textViewPath.setText("Remote Path: " + media.getFile_path());

        String imageUrl = BASE_URL + media.getFile_path();
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_background)
                .into(holder.binding.imageViewSynced);

        holder.binding.buttonUnsync.setOnClickListener(v -> {
            if (unsyncClickListener != null) {
                unsyncClickListener.onUnsyncClick(media, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemBackupMediaBinding binding;

        ViewHolder(@NonNull ItemBackupMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnUnsyncClickListener {
        void onUnsyncClick(MediaMetadata media, int position);
    }
}