package com.hriportfolio.nightingale;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hriportfolio.nightingale.model.FavouriteModel;
import com.hriportfolio.nightingale.model.Posts;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    static Context context;
    public static ArrayList<Posts> postsList;
    public static DeleteInterface deleteInterface;
    static int action;
    //public boolean result = false;

    public RecyclerViewAdapter(ArrayList<Posts> postsList, Context c, int action, DeleteInterface deleteInterface) {
        context = c;
        this.postsList = postsList;
        this.action = action;
        this.deleteInterface = deleteInterface;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {


        private TextView post_title;
        private TextView post_author;
        private TextView post_date;
        private TextView post_desc;
        Button post_save_button;
        private ImageView post_image;


        public MyViewHolder(View v) { //data to hold is a cardview holder
            super(v);
            post_title = v.findViewById(R.id.post_title);
            post_author = v.findViewById(R.id.post_author);
            post_date = v.findViewById(R.id.post_date);
            post_desc = v.findViewById(R.id.post_desc);
            post_save_button = v.findViewById(R.id.post_save_button);
            post_image = v.findViewById(R.id.post_image);
            if (action == 2) {
                post_image.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    Intent i = new Intent(context, AuthorPage.class);
                    i.putExtra("author_name", postsList.get(position).author_name);
                    i.putExtra("post_title", postsList.get(position).post_title);
                    i.putExtra("post_image", postsList.get(position).post_image);
                    i.putExtra("post_timestamp", postsList.get(position).post_date_time);
                    i.putExtra("author_id", postsList.get(position).author_id);
                    i.putExtra("post_id", postsList.get(position).post_id);
                    i.putExtra("post_details", postsList.get(position).post_description);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                });
                post_save_button.setText("Remove");
                post_save_button.setOnClickListener(view
                        -> removeFromFav(postsList.get(getAdapterPosition()).post_id));

            }

            if (action == 0) {
                post_save_button.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    Intent i = new Intent(context, AuthorPage.class);
                    i.putExtra("author_name", postsList.get(position).author_name);
                    i.putExtra("post_title", postsList.get(position).post_title);
                    i.putExtra("post_image", postsList.get(position).post_image);
                    i.putExtra("post_timestamp", postsList.get(position).post_date_time);
                    i.putExtra("author_id", postsList.get(position).author_id);
                    i.putExtra("post_id", postsList.get(position).post_id);
                    i.putExtra("post_details", postsList.get(position).post_description);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                });
            }
            if (action == 1) {

                post_save_button.setText("Delete");
                post_save_button.setOnClickListener(view ->
                        removeMyData(postsList.get(getAdapterPosition()).post_id));
            }
        }

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(context).inflate(R.layout.photo_card, parent, false);
        MyViewHolder vHolder = new MyViewHolder(v);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.post_author.setText(postsList.get(position).author_name);
        holder.post_date.setText(postsList.get(position).post_date_time);
        holder.post_desc.setText(postsList.get(position).post_description);
        Picasso.get().load(postsList.get(position).post_image).into(holder.post_image);
        holder.post_title.setText(postsList.get(position).post_title);
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public static void saveToFavs(String post_id) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("post_id", post_id);
        databaseRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Favourites").push()
                .setValue(postMap);
        Toast.makeText((context), "Added to Favourites!", Toast.LENGTH_SHORT).show();
    }

    public static void removeFromFav(String post_id) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        databaseRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Favourites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                FavouriteModel fav = ds.getValue(FavouriteModel.class);
                                if (post_id.equals(fav.post_id)) {
                                    Log.d("firebase_removal", ds.getRef().toString());
                                    ds.getRef().removeValue();

                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public static void removeMyData(String post_id) {
        deleteInterface.deletePost(post_id);
    }

    public interface DeleteInterface {
        void deletePost(String post_id);
    }
}
