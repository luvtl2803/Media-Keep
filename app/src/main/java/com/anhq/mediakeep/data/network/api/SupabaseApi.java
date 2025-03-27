package com.anhq.mediakeep.data.network.api;

import com.anhq.mediakeep.data.network.model.MediaMetadata;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseApi {
    @Multipart
    @POST("/storage/v1/object/media-keep/media/{fileName}")
    Call<Void> uploadFile(
            @Header("authorization") String auth,
            @Header("apikey") String apiKey,
            @Part MultipartBody.Part file,
            @Path("fileName") String fileName
    );

    @POST("/rest/v1/media_items")
    Call<Void> saveMetadata(
            @Header("authorization") String auth,
            @Header("apikey") String apiKey,
            @Header("Content-Type") String contentType,
            @Body MediaMetadata metadata
    );


    @GET("/rest/v1/media_items")
    Call<List<MediaMetadata>> getSyncedMedia(
            @Header("authorization") String auth,
            @Header("apikey") String apiKey,
            @Query("select") String select,
            @Query("media_type") String mediaType
    );

    @DELETE("/storage/v1/object/media-keep/{filePath}")
    Call<Void> deleteFile(
            @Header("authorization") String auth,
            @Header("apikey") String apiKey,
            @Path("filePath") String filePath
    );

    @DELETE("/rest/v1/media_items")
    Call<Void> deleteMetadata(
            @Header("authorization") String auth,
            @Header("apikey") String apiKey,
            @Header("Content-Type") String contentType,
            @Query("file_path") String path
    );
}