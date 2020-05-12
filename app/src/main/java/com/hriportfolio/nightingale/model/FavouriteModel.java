package com.hriportfolio.nightingale.model;

import com.google.firebase.database.PropertyName;

public class FavouriteModel {
    @PropertyName("post_id")
    public String post_id;

    public FavouriteModel(String post_id) {
        this.post_id = post_id;
    }

    public FavouriteModel(){

    }
}
