package com.bytedance.camera.demo.network;

import com.bytedance.camera.demo.bean.FeedResponse;
import com.bytedance.camera.demo.bean.PostVideoResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface IMiniDouyinService {

    @Multipart
    @POST("/mini_douyin/invoke/video")
    Call<PostVideoResponse> createVideo(
            @Query("student_id") String student_id,
            @Query("user_name") String user_name,
            @Part MultipartBody.Part image, @Part MultipartBody.Part video);


    @GET("/mini_douyin/invoke/video") Call<FeedResponse> fetchFeed();
}

