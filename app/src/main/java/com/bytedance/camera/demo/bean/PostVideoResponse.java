package com.bytedance.camera.demo.bean;

import com.google.gson.annotations.SerializedName;


public class PostVideoResponse {

    @SerializedName("item") private Feed item;
    @SerializedName("success") private boolean success;

    public Feed getitem() {
        return item;
    }
    public void setitem(Feed item) {
        this.item = item;
    }

    public boolean getsuccess() {
        return success;
    }
    public void setsuccess(boolean success) {
        this.success = success;
    }

    @Override public String toString() {
        return "Post{" +
                "item='" + item + '\'' +
                ", success=" + success +
                '}';
    }
}

