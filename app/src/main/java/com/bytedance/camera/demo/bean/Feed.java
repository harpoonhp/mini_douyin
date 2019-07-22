package com.bytedance.camera.demo.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class Feed implements Serializable {

    @SerializedName("student_id") private String student_id;
    @SerializedName("user_name") private String user_name;
    @SerializedName("image_url") private String image_url;
    @SerializedName("video_url") private String video_url;
    @SerializedName("_id") private String _id;
    @SerializedName("createdAt") private String createdAt;
    @SerializedName("updatedAt") private String updatedAt;
    @SerializedName("image_w") private int image_w;
    @SerializedName("image_h") private int image_h;
    @SerializedName("__v") private int __v;


    public String getstudent_id() {
        return student_id;
    }
    public void setstudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getuser_name() {
        return user_name;
    }
    public void setuser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getimage_url() {
        return image_url;
    }
    public void setimage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getvideo_url() {
        return video_url;
    }
    public void setvideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String get_id() {
        return _id;
    }
    public void set_id(String _id) {
        this._id = _id;
    }

    public String getcreatedAt() {
        return createdAt;
    }
    public void setcreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getupdatedAt() {
        return updatedAt;
    }
    public void setupdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getimage_w() {
        return image_w;
    }
    public void setimage_w(int image_w) {
        this.image_w = image_w;
    }

    public int getimage_h() {
        return image_h;
    }
    public void setimage_h(int image_h) {
        this.image_h = image_h;
    }

    public int get__v() {
        return __v;
    }
    public void set__v(int __v) {
        this.__v = __v;
    }


    @Override public String toString() {
        return "Feed{" +
                "student_id='" + student_id + '\'' +
                ", user_name=" + user_name + '\'' +
                ", image_url=" + image_url + '\'' +
                ", video_url=" + video_url + '\'' +
                ", _id=" + _id + '\'' +
                ", image_w=" + image_w +
                ", image_h=" + image_h +
                ", createdAt=" + createdAt + '\'' +
                ", updatedAt=" + updatedAt + '\'' +
                ", __v=" + __v +
                '}';
    }
}

