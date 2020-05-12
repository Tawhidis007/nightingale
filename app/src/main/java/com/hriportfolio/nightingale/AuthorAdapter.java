package com.hriportfolio.nightingale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hriportfolio.nightingale.model.Posts;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.MyViewHolder> {

    static Context context;
    private static MyInterface myinterface;
    public static ArrayList<Posts> postsList;

    public AuthorAdapter(ArrayList<Posts> postsList, Context c,MyInterface myInterface) {
        context = c;
        this.postsList = postsList;
        myinterface = myInterface ;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(context).inflate(R.layout.author_work_card, parent, false);
        MyViewHolder vHolder = new MyViewHolder(v);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(postsList.get(position).post_image).into(holder.auhor_work_image);
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView auhor_work_image;

        public MyViewHolder(View v) { //data to hold is a cardview holder
            super(v);

            auhor_work_image = v.findViewById(R.id.author_work_image);

            auhor_work_image.setOnClickListener(view -> {
                int position = getAdapterPosition();

                myinterface.ImageViewClick(position);

            });

        }
    }
    public interface MyInterface
    {
        void ImageViewClick(int position);
    }


}