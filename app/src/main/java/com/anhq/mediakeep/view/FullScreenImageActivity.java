package com.anhq.mediakeep.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.anhq.mediakeep.data.model.MediaItem;
import com.anhq.mediakeep.databinding.ActivityFullScreenImageBinding;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FullScreenImageActivity extends AppCompatActivity {
    private ActivityFullScreenImageBinding binding;
    private MediaItem mediaItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullScreenImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaItem = intent.getParcelableExtra("media_item", MediaItem.class);
        } else {
            @SuppressWarnings("deprecation")
            MediaItem deprecatedItem = intent.getParcelableExtra("media_item");
            mediaItem = deprecatedItem;
        }

        if (mediaItem == null || mediaItem.getUri() == null) {
            finish();
            return;
        }

        Glide.with(this)
                .load(mediaItem.getUri())
                .into(binding.fullScreenImageView);

        binding.btnShare.setOnClickListener(v -> shareImage());
        binding.btnSetBackground.setOnClickListener(v -> setAsBackground());
        binding.btnInfo.setOnClickListener(v -> showInfo());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void shareImage() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, mediaItem.getUri());
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    private void setAsBackground() {
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(mediaItem.getUri(), "image/*");
        intent.putExtra("mimeType", "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Set as Background"));
    }

    private void showInfo() {
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(mediaItem.getDateModified() * 1000));
        String info = "Name: " + mediaItem.getName() + "\n" +
                "Size: " + mediaItem.getFormattedSize() + "\n" +
                "Date Modified: " + date;

        new AlertDialog.Builder(this)
                .setTitle("Media Info")
                .setMessage(info)
                .setPositiveButton("OK", null)
                .show();
    }
}