package com.anhq.mediakeep.data.network.model;

public class MediaMetadata {
    public String file_name;
    public String file_path;
    public String media_type;
    public long size;
    public long date_modified;

    public MediaMetadata(String fileName, String filePath, String mediaType, long size, long dateModified) {
        this.file_name = fileName;
        this.file_path = filePath;
        this.media_type = mediaType;
        this.size = size;
        this.date_modified = dateModified;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate_modified() {
        return date_modified;
    }

    public void setDate_modified(long date_modified) {
        this.date_modified = date_modified;
    }
}