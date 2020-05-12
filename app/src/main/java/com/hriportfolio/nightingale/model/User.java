package com.hriportfolio.nightingale.model;

import com.google.firebase.database.PropertyName;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;

public class User {
    @PropertyName("about")
    public String about;

    @PropertyName("image")
    public String image;

    @PropertyName("name")
    public String name;

    @PropertyName("uid")
    public String uid;

    @Nullable
    @PropertyName("Posts")
    public ArrayList<Posts> posts;

}
