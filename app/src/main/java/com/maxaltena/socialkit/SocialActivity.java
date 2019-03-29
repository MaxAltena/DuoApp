package com.maxaltena.socialkit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.InputStream;

public class SocialActivity extends AppCompatActivity {
    private static final String TAG = "SOCIALACTIVITY";
    private String username;
    private String platformLink;
    private String platformName;
    private String platformImage;
    private ImageView mImage;
    private TextView mPlatformLinkTextView;
    private TextView mPlatformnameTextView;
    private TextView mUsernameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_social);
        //Declamre view vars
        mUsernameTextView = (TextView)findViewById(R.id.textView1);
        mPlatformnameTextView  = (TextView)findViewById(R.id.textView2);
        mPlatformLinkTextView  = (TextView)findViewById(R.id.textView3);
        mImage = (ImageView)findViewById(R.id.imageView);


        //Get intent vars
        Intent social = getIntent();
        username = social.getStringExtra("Username");
        platformLink = social.getStringExtra("Platform Link");
        platformName = social.getStringExtra("Platform Name");
        platformImage = social.getStringExtra("Platform Image");


        loadDataToView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void loadDataToView() {
        mUsernameTextView.setText(username);
        mPlatformnameTextView.setText(platformName);
        mPlatformLinkTextView.setText(platformLink);
        Glide.with(this).load(platformImage).into(mImage);
    }

}
