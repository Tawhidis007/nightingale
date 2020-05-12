package com.hriportfolio.nightingale.model;

import com.google.firebase.database.PropertyName;

public class Posts {
    @PropertyName("author_id")
    public String author_id;

    @PropertyName("post_title")
    public String post_title;

    @PropertyName("author_name")
    public String author_name;

    @PropertyName("post_date_time")
    public String post_date_time;

    @PropertyName("post_description")
    public String post_description;

    @PropertyName("post_image")
    public String post_image;

    @PropertyName("post_id")
    public String post_id;

    public Posts(String author_id, String post_title, String author_name, String post_date_time,
                 String post_description, String post_image,String post_id) {
        this.author_id = author_id;
        this.post_title = post_title;
        this.author_name = author_name;
        this.post_date_time = post_date_time;
        this.post_description = post_description;
        this.post_image = post_image;
        this.post_id = post_id;
    }

    //essential empty constructor for firebase
    public Posts(){

    }

//    public String getAuthor_id() {
//        return author_id;
//    }
//
//    public void setAuthor_id(String author_id) {
//        this.author_id = author_id;
//    }
//
//    public String getPost_title() {
//        return post_title;
//    }
//
//    public void setPost_title(String post_title) {
//        this.post_title = post_title;
//    }
//
//    public String getAuthor_name() {
//        return author_name;
//    }
//
//    public void setAuthor_name(String author_name) {
//        this.author_name = author_name;
//    }
//
//    public String getPostDate() {
//        return post_date_time;
//    }
//
//    public void setPostDate(String post_date_time) {
//        this.post_date_time = post_date_time;
//    }
//
//    public String getPost_description() {
//        return post_description;
//    }
//
//    public void setPost_description(String post_description) {
//        this.post_description = post_description;
//    }
//
//    public String getPost_image() {
//        return post_image;
//    }
//
//    public void setPost_image(String post_image) {
//        this.post_image = post_image;
//    }
}
