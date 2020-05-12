package com.hriportfolio.nightingale;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FullScreenImage extends AppCompatActivity {

    private ImageView fullScreenImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_full_screen_image);
        fullScreenImageView = findViewById(R.id.fullScreenImageView);

        if(getIntent()!=null){
            String img = getIntent().getStringExtra("image");
            Picasso.get().load(img).into(fullScreenImageView);
        }
    }
}
