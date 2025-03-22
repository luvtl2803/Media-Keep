package com.anhq.mediakeep.data.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.anhq.mediakeep.utils.help.StorageUtils;

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
        String uriString = in.readString();
        uri = uriString != null ? Uri.parse(uriString) : null;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uri != null ? uri.toString() : null);
        dest.writeString(name);
        dest.writeLong(size);
        dest.writeLong(dateModified);
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public long getDateModified() {
        return dateModified;
    }

    public String getFormattedSize() {
        return StorageUtils.convertBytes(size);
    }
}