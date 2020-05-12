package com.hriportfolio.nightingale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hriportfolio.nightingale.Utilities.KeyString;
import com.hriportfolio.nightingale.Utilities.OnSwipeTouchListener;
import com.hriportfolio.nightingale.Utilities.SharedPreferenceManager;
import com.hriportfolio.nightingale.Utilities.Utils;
import com.hriportfolio.nightingale.model.FavouriteModel;
import com.hriportfolio.nightingale.model.Posts;

import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class Home extends AppCompatActivity implements RecyclerViewAdapter.DeleteInterface {

    SharedPreferenceManager preferenceManager;
    private String userName;
    private String propic_url;
    private String aboutUser;
    RecyclerView recyclerView;
    Context mContext;

    int action = 0;
    ArrayList<Posts> postsList;
    ArrayList<Posts> myPostsList;
    ArrayList<Posts> favouritePostsList;
    ArrayList<String> favouritePostsIds;

    //views
    @BindView(R.id.home_pro_pic)
    ImageView home_pro_pic;
    @BindView(R.id.more_options)
    ImageView more_options_iv;
    @BindView(R.id.relativeLayout)
    RelativeLayout relativeLayoutSwipable;
    @BindView(R.id.rel_layout_below)
    RelativeLayout rel_layout_below;
    @BindView(R.id.code_layout)
    ConstraintLayout code_layout;
    @BindView(R.id.image_extractor)
    ImageView image_extractor;
    @BindView(R.id.home_userName)
    TextView home_user_name;
    @BindView(R.id.menu_placeholder_text)
    TextView menu_placeholder_text;

    //create post views
    @BindView(R.id.create_post_title)
    EditText create_post_title_editText;

    @BindView(R.id.create_post_caption)
    EditText create_post_caption_editText;

    @BindView(R.id.create_post_details)
    EditText create_post_details_editText;

    @BindView(R.id.create_post_iv)
    ImageView create_post_iv;

    private String post_title;
    private String post_desc;
    private String post_caption;
    private String post_url;

    private boolean isExtracted = false;

    private ProgressDialog pd;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private StorageTask uploadTask;
    Uri imageUri;

    //for unique file name generation for firestore
    private String uniqueString = "";

    RecyclerViewAdapter adapter;
    ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference().child("Posts");
        databaseRef = FirebaseDatabase.getInstance().getReference();

        recyclerView = findViewById(R.id.recycler_view);
        mContext = getApplicationContext();
        initPref();

        initializeCards();
        setUpInitialUiWithBottomSheetHidden();
        makeRelativeLayoutSwipable();

    }

    private void initializeCards() {
        pd = Utils.createProgressDialog(this);
        if (action == 0) {
            valueEventListener = databaseRef.child("Posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    postsList = new ArrayList<>();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Posts post = snapshot.getValue(Posts.class);
                            postsList.add(post);
                            Log.d("post_data", post.post_title);

                        }
                        setUpRecyclerView(postsList, action);
                        pd.dismiss();
                    } else {
                        postsList.clear();
                        setUpRecyclerView(postsList, action);
                        Toast.makeText(Home.this, "No data available!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else if (action == 1) {
            valueEventListener = databaseRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Posts")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            myPostsList = new ArrayList<>();
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Posts post = snapshot.getValue(Posts.class);
                                    myPostsList.add(post);
                                    Log.d("my_post_data", post.post_title);

                                }
                                if (action == 1) {
                                    setUpRecyclerView(myPostsList, action);
                                    pd.dismiss();
                                }

                            } else {
                                myPostsList.clear();
                                setUpRecyclerView(myPostsList, action);
                                Toast.makeText(Home.this, "You don't have any post yet!", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {
            valueEventListener = databaseRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Favourites")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            favouritePostsList = new ArrayList<>();
                            favouritePostsIds = new ArrayList<>();
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    FavouriteModel fav = snapshot.getValue(FavouriteModel.class);
                                    favouritePostsIds.add(fav.post_id);
                                }
                                databaseRef.child("Posts").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                Posts post = snapshot.getValue(Posts.class);
                                                for (int i = 0; i < favouritePostsIds.size(); i++) {
                                                    if (post.post_id.equals(favouritePostsIds.get(i))) {
                                                        favouritePostsList.add(post);
                                                    }
                                                }
                                            }
                                            Log.d("firebase_action", String.valueOf(action));
                                            if (action == 2) {
                                                setUpRecyclerView(favouritePostsList, action);
                                                pd.dismiss();
                                            }

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                favouritePostsList.clear();
                                setUpRecyclerView(favouritePostsList, action);
                                Toast.makeText(Home.this, "No Favourites!", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

    }

    private void setUpRecyclerView(ArrayList<Posts> posts, int action) {
        adapter = new RecyclerViewAdapter(posts, mContext, action,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }

    private void makeRelativeLayoutSwipable() {
        relativeLayoutSwipable.setOnTouchListener(new OnSwipeTouchListener(Home.this) {
            public void onSwipeTop() {
                showBottomSheet();
            }

            public void onSwipeBottom() {
                hideBottomSheet();
            }
        });
    }

    private void initPref() {
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        userName = preferenceManager.getValue(KeyString.USER_NAME, "");
        propic_url = preferenceManager.getValue(KeyString.PROFILE_PICTURE_URL, "");
        aboutUser = preferenceManager.getValue(KeyString.ABOUT_USER, "");

        if (userName.equals("") || aboutUser.equals("")) {
            setUpAccFromDB();
        } else {
            setUpAccFromSharedPref();
        }
    }

    private void setUpAccFromSharedPref() {
        if (!propic_url.equals("")) {
            Picasso.get().load(propic_url).into(home_pro_pic);
        }
        home_user_name.setText(userName);
    }

    private void setUpAccFromDB() {
        databaseRef.child("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if(ds.getKey().equals("name")){
                            userName= ds.getValue().toString();
                        }
                        if(ds.getKey().equals("image")){
                            propic_url= ds.getValue().toString();
                        }
                        if(ds.getKey().equals("about")){
                            aboutUser= ds.getValue().toString();
                        }
                    }
                    if (!propic_url.equals("")) {
                        Picasso.get().load(propic_url).into(home_pro_pic);
                    }
                    home_user_name.setText(userName);

                    //set sharedpref
                    preferenceManager.setValue(KeyString.USER_NAME, userName);
                    preferenceManager.setValue(KeyString.PROFILE_PICTURE_URL, propic_url);
                    preferenceManager.setValue(KeyString.ABOUT_USER, aboutUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @OnClick(R.id.create_post_button)
    void createPost() {
        post_title = create_post_title_editText.getText().toString();
        post_desc = create_post_details_editText.getText().toString();
        post_caption = create_post_caption_editText.getText().toString();
        savePostToDatabase(post_title, post_desc, post_caption);
    }

    @OnClick(R.id.create_post_iv)
    void selectImage() {
        ImagePicker.Companion.with(this)
                .compress(1024)   //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    @OnClick(R.id.image_extractor)
    void extractor_clicked() {
        imageExtractorClicked(isExtracted);
    }

    private void imageExtractorClicked(boolean isExtracted) {
        if (isExtracted) {
            hideBottomSheet();
        } else {
            showBottomSheet();
        }
    }

    private void showBottomSheet() {
        if (!isExtracted) {
            float density = this.getResources().getDisplayMetrics().density;
            rel_layout_below.getLayoutParams().height = (int) (460 * density);
            code_layout.setVisibility(View.VISIBLE);
            isExtracted = true;
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.creation_slide_up);
            rel_layout_below.startAnimation(slideUp);

            image_extractor.animate().setDuration(600).rotation(0).start();
        }
    }

    private void hideBottomSheet() {
        if (isExtracted) {
            float density = this.getResources().getDisplayMetrics().density;
            rel_layout_below.getLayoutParams().height = (int) (120 * density);
            code_layout.setVisibility(View.GONE);
            isExtracted = false;
            Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.creation_slide_down);
            rel_layout_below.startAnimation(slideDown);
            image_extractor.animate().setDuration(600).rotation(180).start();
        }
    }

    private void setUpInitialUiWithBottomSheetHidden() {
        float density = this.getResources().getDisplayMetrics().density;
        rel_layout_below.getLayoutParams().height = (int) (120 * density);
        code_layout.setVisibility(View.GONE);
        image_extractor.animate().setDuration(600).rotation(180).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Home.RESULT_OK) {
            imageUri = data.getData();
            create_post_iv.setImageURI(imageUri);
        }
    }

    private void savePostToDatabase(String title, String desc, String caption) {
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(Home.this,
                    "Please provide post_title", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(desc)) {
            Toast.makeText(Home.this,
                    "Please provide description", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(caption)) {
            Toast.makeText(Home.this,
                    "Please provide caption", Toast.LENGTH_SHORT).show();
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Saving to Database");
            progressDialog.setMessage("Please wait a while");
            progressDialog.show();

            if (imageUri != null) {
                uniqueString = UUID.randomUUID().toString();
                final StorageReference fileRef = storageRef.child(uniqueString + ".jpg");
                uploadTask = fileRef.putFile(imageUri);
                uploadTask.continueWithTask((Continuation) task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        post_url = downloadUrl.toString();

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        String currentDateandTime = sdf.format(new Date());

                        HashMap<String, Object> postMap = new HashMap<>();
                        postMap.put("author_id", mAuth.getCurrentUser().getUid());
                        postMap.put("post_title", title);
                        postMap.put("post_description", desc);
                        postMap.put("post_image", post_url);
                        postMap.put("author_name", userName);
                        postMap.put("post_date_time", currentDateandTime);

                        String key = databaseRef.child("Posts").push().getKey();
                        postMap.put("post_id", key);


                        //for adding post data to user
                        databaseRef.child("Posts").child(key).setValue(postMap, (databaseError, databaseReference) -> {
                            //   String post_id = databaseReference.getKey();

                            //storing post reference in user db
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("post_id", key);
                            userMap.put("post_title", title);
                            userMap.put("post_description", desc);
                            userMap.put("post_image", post_url);
                            userMap.put("author_name", userName);
                            userMap.put("post_date_time", currentDateandTime);
                            databaseRef.child("Users").child(mAuth.getCurrentUser()
                                    .getUid()).child("Posts").push().setValue(userMap);
                        });
                        uniqueString = "";
                        progressDialog.dismiss();
                        hideBottomSheet();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(Home.this, "Database update error", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(Home.this, "Image is not selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.more_options)
    void more_options() {
        PopupMenu popup = new PopupMenu(this, more_options_iv);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.more_options, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_my_posts) {
                action = 1;
                menu_placeholder_text.setText("My Posts");
                initializeCards();
            }
            if (item.getItemId() == R.id.menu_favourites) {
                action = 2;
                menu_placeholder_text.setText("Favourites");
                initializeCards();
            }
            if (item.getItemId() == R.id.menu_home) {
                menu_placeholder_text.setText("Recent Feed");
                action = 0;
                initializeCards();
            }
            return true;
        });
    }

    @OnClick(R.id.home_pro_pic)
    void logout_options(){
        PopupMenu popup = new PopupMenu(this, home_pro_pic);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.logout_menu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                new AlertDialog.Builder(this)
                        .setTitle("Logging Out")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            mAuth.signOut();
                            databaseRef.removeEventListener(valueEventListener);
                            preferenceManager.clear();
                            finish();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
            return true;
        });

//        String target = "Sat Dec 21 2019 00:00:00 GMT+0000";
//        DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy kk:mm:ss z", Locale.ENGLISH);
//
//        String target = "Thu Nov 19 00:00:00 GMT+06:00 2020";
//        String target2 = "Thu Dec 19 00:00:00 GMT+06:00 2020";
//        DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
//        Date result = null;
//        Date result2 = null;
//        try {
//            result = df.parse(target);
//            result2 = df.parse(target2);
//            Log.d("JODATIME_",result.compareTo(result2)+" and month : "+result.getMonth());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseRef.removeEventListener(valueEventListener);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void deletePost(String post_id) {
        new AlertDialog.Builder(this)
                .setTitle("Deleting Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                    databaseRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Posts")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                                            Posts fav = ds.getValue(Posts.class);
                                            if(post_id.equals(fav.post_id)){
                                                Log.d("firebase_removal",ds.getRef().toString());
                                                ds.getRef().removeValue();

                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                    databaseRef.child("Posts").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    Posts feed = ds.getValue(Posts.class);
                                    if(post_id.equals(feed.post_id)){
                                        ds.getRef().removeValue();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
