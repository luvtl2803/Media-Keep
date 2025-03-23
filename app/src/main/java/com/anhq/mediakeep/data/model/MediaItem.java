package com.anhq.mediakeep.data.model;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class MediaItem implements Parcelable {
    private Uri uri;
    private String name;
    private long size;
    private long dateModified;

    public MediaItem(Uri uri, String name, long size, long dateModified) {
        this.uri = uri;
        this.name = name;
        this.size = size;
        this.dateModified = dateModified;
    }

    protected MediaItem(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        name = in.readString();
        size = in.readLong();
        dateModified = in.readLong();
    }

    public static final Creator<MediaItem> CREATOR = new Creator<>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public long getDateModified() {
        return dateModified;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedSize() {
        if (size < 1024) return size + " B";
        else if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        else if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        else return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeString(name);
        dest.writeLong(size);
        dest.writeLong(dateModified);
    }
}