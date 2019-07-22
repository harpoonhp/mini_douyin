package com.bytedance.camera.demo;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.camera.demo.bean.Feed;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.io.IOException;

public class MediaPlayerActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SurfaceHolder holder;
    private SeekBar seekBar;
    private Thread thread;
    private TextView showtime;
    private boolean isChanging = false;
    private Feed mfeed;

    public static void launch(Activity activity, Feed feed) {
        Intent intent = new Intent(activity, MediaPlayerActivity.class);
        intent.putExtra("feed", feed);
        activity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MediaPlayer");
        setContentView(R.layout.activity_media_player);
        surfaceView = findViewById(R.id.surfaceView);
        player = new MediaPlayer();
        seekBar = findViewById(R.id.seekBar);
        showtime = findViewById(R.id.show_time);
        mfeed = (Feed) getIntent().getSerializableExtra("feed");
        String url = mfeed.getvideo_url();
        try {
            player.setDataSource(url);
            holder = surfaceView.getHolder();
            holder.addCallback(new PlayerCallBack());
            player.prepare();
            seekBar.setMax(player.getDuration());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //显示时间
                    showtime.setText(String.format("%02d:%02d",progress/60000%60,progress/1000%60));
                    //时间随进度条移动
                    final float scale = MediaPlayerActivity.this.getResources().getDisplayMetrics().density;
                    float textwidth = showtime.getWidth();
                    float left = seekBar.getLeft();
                    float max = Math.abs(seekBar.getMax());
                    float thumb = 15 * scale +0.5f;
                    float average = (((float) seekBar.getWidth())-2*thumb)/max;
                    float pox = left - textwidth/2 +thumb + average * progress;
                    showtime.setX(pox);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isChanging = true;

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    player.seekTo(seekBar.getProgress());
                    isChanging = false;
                    thread = new Thread(new SeekBarThread());
                    thread.start();
                }
            });
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    setVideoParams(player, getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
                    player.start();
                    player.setLooping(true);
                    thread = new Thread(new SeekBarThread());
                    thread.start();
                }
            });
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    System.out.println(percent);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        surfaceView.setOnClickListener(v ->  {
                if(player.isPlaying()){
                    player.pause();
                }
                else if(!player.isPlaying()){
                    player.start();
                }
                thread = new Thread(new SeekBarThread());
                thread.start();
        });
        LikeButton likeButton = findViewById(R.id.likeBtn);
        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked() {
                Toast.makeText(getApplicationContext(), "已点赞", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void unLiked() {
                Toast.makeText(getApplicationContext(), "取消点赞", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void setVideoParams(MediaPlayer mediaPlayer, boolean isLand) {
        View RelativeLayout = findViewById(R.id.r1);
        View surfaceView = findViewById(R.id.surfaceView);
        //获取surfaceView父布局的参数
        ViewGroup.LayoutParams rl_paramters = RelativeLayout.getLayoutParams();
        //设置宽高比为16/9
        float screen_widthPixels = getResources().getDisplayMetrics().widthPixels;
        float screen_heightPixels = getResources().getDisplayMetrics().widthPixels * 16f / 9f;
        //取消全屏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (isLand) {
            screen_heightPixels = getResources().getDisplayMetrics().heightPixels;
            //设置全屏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        rl_paramters.width = (int) screen_widthPixels;
        rl_paramters.height = (int) screen_heightPixels;
        RelativeLayout.setLayoutParams(rl_paramters);
        surfaceView.setLayoutParams(rl_paramters);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setVideoParams(player, true);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setVideoParams(player, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.pause();
            player.stop();
            isChanging = true;
            player.release();
            player = null;
            startActivity(new Intent(this, Post_Feed.class));
        }
    }

    private class PlayerCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (player != null) {
                player.pause();
                player.stop();
                isChanging = true;
                player.release();
                player = null;
            }
        }
    }
    class SeekBarThread implements Runnable{
        public void run(){
            while (!isChanging && player.isPlaying()){
                seekBar.setProgress(player.getCurrentPosition());
                try {
                    // 每100毫秒更新一次位置
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

