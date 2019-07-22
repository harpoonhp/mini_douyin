package com.bytedance.camera.demo;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bytedance.camera.demo.bean.Feed;
import com.bytedance.camera.demo.bean.FeedResponse;
import com.bytedance.camera.demo.bean.PostVideoResponse;
import com.bytedance.camera.demo.network.IMiniDouyinService;
import com.bytedance.camera.demo.utils.ResourceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Post_Feed extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final int GRANT_PERMISSION = 3;
    private static final String TAG = "Solution2C2Activity";
    private RecyclerView mRv;
    private List<Feed> mFeeds = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    List<Call> mCallList1 = new ArrayList<>();
    List<Call> mCallList2 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post__feed);
        initRecyclerView();
        initBtns();
        fetchFeed();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageview;
        public TextView usernameTv;
        public TextView studentIdTv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageview = itemView.findViewById(R.id.imageview);
            usernameTv = itemView.findViewById(R.id.username_text);
            studentIdTv = itemView.findViewById(R.id.studentID_text);
        }

        public void bind(final Activity activity, Feed feed) {
            Glide.with(imageview.getContext()).load(feed.getimage_url()).into(imageview);
            usernameTv.setText(feed.getuser_name());
            studentIdTv.setText(feed.getstudent_id());
            imageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaPlayerActivity.launch(activity, feed);
                }
            });
        }
    }

    class MyDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int distance = 5;
            outRect.set(distance, distance, distance, distance);
        }
    }

    private void initBtns() {
        mBtn = findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String s = mBtn.getText().toString();
                if (getString(R.string.select_an_image).equals(s)) {
                        chooseImage();
                } else if (getString(R.string.select_a_video).equals(s)) {
                        chooseVideo();
                } else if (getString(R.string.post_it).equals(s)) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = " + mSelectedVideo + ", mSelectedImage = " + mSelectedImage);
                    }
                } else if ((getString(R.string.success_try_refresh).equals(s))) {
                    mBtn.setText(R.string.select_an_image);
                }
            }
        });
    }


    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mRv.addItemDecoration(new MyDecoration());
        mRv.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {
            @NonNull @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                return new MyViewHolder(
                        LayoutInflater.from(Post_Feed.this)
                                .inflate(R.layout.video_item, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
                final Feed feed = mFeeds.get(i);
                viewHolder.bind(Post_Feed.this, feed);
            }

            @Override public int getItemCount() {
                return mFeeds.size();
            }
        });
    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }


    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                mBtn.setText(R.string.select_a_video);
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                mBtn.setText(R.string.post_it);
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(Post_Feed.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("上传中");
        mBtn.setEnabled(false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Call<PostVideoResponse> postCall = retrofit.create(IMiniDouyinService.class).createVideo("1120173307","harpoon",getMultipartFromUri("cover_image",mSelectedImage),getMultipartFromUri("video",mSelectedVideo));
        mCallList1.add(postCall);
        postCall.enqueue(new Callback<PostVideoResponse>(){
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                if(response.body().getsuccess()){
                    Toast.makeText(getApplicationContext(), "上传成功", Toast.LENGTH_SHORT).show();
                    mBtn.setText(R.string.success_try_refresh);
                    mBtn.setEnabled(true);
                    mCallList1.remove(call);
                    fetchFeed();
                }
                else {
                    Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();
                    mBtn.setText(R.string.select_an_image);
                    mBtn.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                mCallList1.remove(call);
            }
        });
    }

    public void fetchFeed() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test.androidcamp.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Call<FeedResponse> feedCall = retrofit.create(IMiniDouyinService.class).fetchFeed();
        mCallList2.add(feedCall);
        feedCall.enqueue(new Callback<FeedResponse>(){
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                mFeeds = response.body().getfeeds();
                mRv.getAdapter().notifyDataSetChanged();
                mCallList2.remove(call);
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                mCallList2.remove(call);
            }
        });
    }

}

