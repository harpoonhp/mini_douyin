package com.bytedance.camera.demo.bean;


import com.google.gson.annotations.SerializedName;

import java.util.List;


public class FeedResponse {

    @SerializedName("feeds") private List<Feed> feeds;
    @SerializedName("success") private boolean success;

    public List<Feed> getfeeds() {
        return feeds;
    }
    public void setfeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public boolean getsuccess() {
        return success;
    }
    public void setsuccess(boolean success) {
        this.success = success;
    }

    @Override public String toString() {
        return "Feeds{" +
                "feeds='" + feeds + '\'' +
                ", success=" + success +
                '}';
    }
}

