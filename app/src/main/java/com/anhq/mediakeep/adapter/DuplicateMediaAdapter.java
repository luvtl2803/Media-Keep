package com.anhq.mediakeep.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhq.mediakeep.data.model.MediaItem;
import com.anhq.mediakeep.databinding.ItemDuplicateGroupBinding;
import com.anhq.mediakeep.databinding.ItemDuplicateImageBinding;
import com.anhq.mediakeep.utils.help.MediaStoreHelper;
import com.anhq.mediakeep.view.FullScreenImageActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class DuplicateMediaAdapter extends RecyclerView.Adapter<DuplicateMediaAdapter.ViewHolder> {
    private List<List<MediaItem>> duplicateGroups = new ArrayList<>();
    private Context context;
    private MediaStoreHelper mediaStoreHelper;

    @SuppressLint("NotifyDataSetChanged")
    public void setDuplicateGroups(List<List<MediaItem>> groups) {
        this.duplicateGroups = groups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        mediaStoreHelper = MediaStoreHelper.getInstance(context);
        ItemDuplicateGroupBinding binding = ItemDuplicateGroupBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<MediaItem> group = duplicateGroups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return duplicateGroups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDuplicateGroupBinding binding;
        private InnerAdapter innerAdapter;

        public ViewHolder(@NonNull ItemDuplicateGroupBinding binding, DuplicateMediaAdapter outerAdapter) {
            super(binding.getRoot());
            this.binding = binding;
            binding.rvImages.setLayoutManager(new LinearLayoutManager(
                    binding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
            innerAdapter = new InnerAdapter(outerAdapter);
            binding.rvImages.setAdapter(innerAdapter);
        }

        @SuppressLint("DefaultLocale")
        public void bind(List<MediaItem> group) {
            updateHeader(group);
            innerAdapter.setItems(group);
        }

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        private void updateHeader(List<MediaItem> group) {
            if (group.isEmpty()) {
                binding.tvCountDuplicate.setText("No similar images");
            } else {
                long totalSize = 0;
                for (MediaItem item : group) {
                    totalSize += item.getSize();
                }
                String sizeText = formatSize(totalSize);
                binding.tvCountDuplicate.setText(String.format("%d Similar Images", group.size()));
                binding.tvFreeUp.setText(String.format("Free Up %s", sizeText));
            }
        }

        @SuppressLint("DefaultLocale")
        private String formatSize(long bytes) {
            if (bytes < 1024) return bytes + "B";
            if (bytes < 1024 * 1024) return String.format("%.1fKB", bytes / 1024.0);
            return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
        }

        public void updateUI() {
            updateHeader(innerAdapter.getItems());
        }
    }

    private class InnerAdapter extends RecyclerView.Adapter<InnerAdapter.InnerViewHolder> {
        private List<MediaItem> items = new ArrayList<>();
        private final DuplicateMediaAdapter outerAdapter;

        public InnerAdapter(DuplicateMediaAdapter outerAdapter) {
            this.outerAdapter = outerAdapter;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setItems(List<MediaItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        public List<MediaItem> getItems() {
            return items;
        }

        @NonNull
        @Override
        public InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDuplicateImageBinding binding = ItemDuplicateImageBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new InnerViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull InnerViewHolder holder, int position) {
            MediaItem item = items.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class InnerViewHolder extends RecyclerView.ViewHolder {
            private final ItemDuplicateImageBinding binding;

            public InnerViewHolder(@NonNull ItemDuplicateImageBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(MediaItem item) {
                Glide.with(binding.getRoot().getContext())
                        .load(item.getUri())
                        .centerCrop()
                        .into(binding.ivImage);

                binding.ivImage.setOnClickListener(v -> {
                    Intent intent = new Intent(binding.getRoot().getContext(), FullScreenImageActivity.class);
                    intent.putExtra("media_item", item);
                    binding.getRoot().getContext().startActivity(intent);
                });

                binding.btnKeep.setOnClickListener(v -> {
                    int innerPosition = getBindingAdapterPosition();
                    if (innerPosition != RecyclerView.NO_POSITION) {
                        items.remove(innerPosition);
                        notifyItemRemoved(innerPosition);
                        Toast.makeText(binding.getRoot().getContext(),
                                "Kept: " + item.getName(), Toast.LENGTH_SHORT).show();

                        updateOuterUI();
                    }
                });

                binding.btnDelete.setOnClickListener(v -> {
                    try {
                        context.getContentResolver().delete(item.getUri(), null, null);
                        int innerPosition = getBindingAdapterPosition();
                        if (innerPosition != RecyclerView.NO_POSITION) {
                            items.remove(innerPosition);
                            notifyItemRemoved(innerPosition);
                            Toast.makeText(binding.getRoot().getContext(),
                                    "Deleted successfully!", Toast.LENGTH_SHORT).show();

                            updateOuterUI();
                        }
                    } catch (Exception e) {
                        Toast.makeText(binding.getRoot().getContext(),
                                "Error deleting file", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }

            private void updateOuterUI() {
                RecyclerView innerRecyclerView = (RecyclerView) binding.getRoot().getParent();

                ViewParent groupLayout = innerRecyclerView.getParent();
                if (!(groupLayout instanceof LinearLayout)) {
                    return;
                }

                RecyclerView outerRecyclerView = (RecyclerView) groupLayout.getParent();

                RecyclerView.ViewHolder outerHolder = outerRecyclerView.findContainingViewHolder(innerRecyclerView);
                if (outerHolder instanceof ViewHolder) {
                    ((ViewHolder) outerHolder).updateUI();

                    if (items.size() == 1) {
                        int groupPosition = outerHolder.getBindingAdapterPosition();
                        if (groupPosition != RecyclerView.NO_POSITION) {
                            outerAdapter.duplicateGroups.remove(groupPosition);
                            outerAdapter.notifyItemRemoved(groupPosition);
                        }
                    }
                }
            }
        }
    }
}