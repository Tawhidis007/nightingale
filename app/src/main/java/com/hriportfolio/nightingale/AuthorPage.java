package com.hriportfolio.nightingale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
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
import com.hriportfolio.nightingale.Utilities.Utils;
import com.hriportfolio.nightingale.model.FavouriteModel;
import com.hriportfolio.nightingale.model.Posts;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AuthorPage extends AppCompatActivity implements AuthorAdapter.MyInterface {

    //Views
    @BindView(R.id.author_image_holder)
    ImageView author_image_holder;

    @BindView(R.id.author_name)
    TextView author_name;

    @BindView(R.id.author_post_details)
    TextView author_post_details;

    @BindView(R.id.author_post_title)
    TextView author_post_title;

    @BindView(R.id.author_post_date)
    TextView author_post_date;

    @BindView(R.id.author_user_image)
    CircleImageView author_user_image;

    @BindView(R.id.backFromAuthorPage)
    ImageView backFromAuthorPage;

    Button fav_btn;

    //data
    private String name;
    private String title;
    private String desc;
    private String postImage;
    private String postDate;
    private String authorId;
    private String postId;
    private String authorImage="";
    ArrayList<Posts> authorPostList;
    ArrayList<String> favList;

    private RecyclerView author_recyclerView;
    AuthorAdapter author_adapter;
    Context c;

    private DatabaseReference databaseRef;

    ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_author_page);
        ButterKnife.bind(this);
        author_recyclerView = findViewById(R.id.author_recyclerView);
        databaseRef = FirebaseDatabase.getInstance().getReference();
        c = this;
        fav_btn = findViewById(R.id.author_save_button);

        if(getIntent()!=null){
            Intent intent = getIntent();
            name = intent.getStringExtra("author_name");
            title = intent.getStringExtra("post_title");
            desc = intent.getStringExtra("post_details");
            postImage = intent.getStringExtra("post_image");
            postDate = intent.getStringExtra("post_timestamp");
            authorId = intent.getStringExtra("author_id");
            postId = intent.getStringExtra("post_id");
        }

        if(!name.equals("")){
            initializePage();
        }


        initializeBottomWork();
        initializeFavs();

        backFromAuthorPage.setOnClickListener(view -> onBackPressed());

    }

    private void initializePage() {
        author_post_title.setText(title);
        author_name.setText(name);
        author_post_details.setText(desc);
        author_post_date.setText(postDate);
        if (!postImage.equals("")) {
            Picasso.get().load(postImage).into(author_image_holder);
        }
        getAuthorPicture();
    }

    @OnClick(R.id.author_save_button)
    void save_button_pressed(){
        if(fav_btn.getText().equals("Save")){
            saveToFavs(postId);
        }else{
            Toast.makeText(AuthorPage.this,
                    "You've already added this as favourite!",Toast.LENGTH_SHORT).show();
        }
    }

    void initializeBottomWork(){
        valueEventListener = databaseRef.child("Users").child(authorId).child("Posts")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 authorPostList = new ArrayList<>();
                if(dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Posts post = snapshot.getValue(Posts.class);
                        authorPostList.add(post);
                        Log.d("fire_fire","date  :"+post.post_date_time);
                    }
                    setUpWorks(authorPostList);
                }
                else{
                    authorPostList.clear();
                    setUpWorks(authorPostList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void getAuthorPicture(){
        databaseRef.child("Users").child(authorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        if(ds.getKey().equals("image")){
                            authorImage = ds.getValue().toString();
                            Picasso.get().load(authorImage).fit().into(author_user_image);
                            break;
                        }
                       Log.d("firebase_author","key : "+ ds.getKey()+" value : "+ds.getValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setUpWorks(ArrayList<Posts> authorPostList) {
        author_adapter = new AuthorAdapter(authorPostList, c,this);
        author_recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        author_recyclerView.setAdapter(author_adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseRef.removeEventListener(valueEventListener);
    }

    @Override
    public void ImageViewClick(int position) {
        name = authorPostList.get(position).author_name;
        title = authorPostList.get(position).post_title;
        desc = authorPostList.get(position).post_description;
        postImage = authorPostList.get(position).post_image;
        postDate = authorPostList.get(position).post_date_time;
        authorId = authorPostList.get(position).author_id;
        postId = authorPostList.get(position).post_id;


        ProgressDialog pd = Utils.createProgressDialog(this);
        pd.show();
        //can implement a logic here to check for saved already
        Log.d("final_task","List size : "+favList.size());
        if(!postId.equals("") && favList.size()>0){
            for(int i=0;i<favList.size();i++){
                Log.d("final_task","postID :"+postId+" and favList item :"+favList.get(i));
                if(postId.equals(favList.get(i))){
                    fav_btn.setText("Saved");
                  //  fav_btn.setEnabled(false);
                }else{
                    fav_btn.setText("Save");
                  //  fav_btn.setEnabled(true);
                }
            }
        }
        author_post_title.setText(title);
        author_post_details.setText(desc);
        author_post_date.setText(postDate);
        if (!postImage.equals("")) {
            Picasso.get().load(postImage).into(author_image_holder);
        }
        pd.dismiss();
    }

    @OnClick(R.id.author_image_holder)
    void authorImageClicked(){
        if(!postImage.equals("")){
            Intent i = new Intent(AuthorPage.this,FullScreenImage.class);
            i.putExtra("image",postImage);
            startActivity(i);
        }
    }
    public void saveToFavs(String post_id){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("post_id",post_id);
        databaseRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Favourites").push()
                .setValue(postMap);
        Toast.makeText(AuthorPage.this,"Added to Favourites!",Toast.LENGTH_SHORT).show();
        fav_btn.setText("Saved");
        //fav_btn.setEnabled(false);
    }



    private void initializeFavs(){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        databaseRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Favourites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        favList = new ArrayList<>();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                FavouriteModel favouriteModel = snapshot.getValue(FavouriteModel.class);
                                favList.add(favouriteModel.post_id);
                                Log.d("final_task",favouriteModel.post_id);
                            }
                            for(int i=0;i<favList.size();i++){
                                if(postId.equals(favList.get(i))){
                                    fav_btn.setText("Saved");
                                 //   fav_btn.setEnabled(false);
                                }
                            }
                        }else{
                           // fav_btn.setEnabled(true);
                            fav_btn.setText("Save");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
