package com.maxaltena.socialkit;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.Arrays;

public class SocialActivity extends AppCompatActivity {
    private static final String TAG = "SOCIALACTIVITY";
    private String username;
    private String platformLink;
    private String platformName;
    private String platformImage;
    private String currentUser;
    private String socialId;
    private ImageView mImage;
    private DocumentReference socialRef;
    private TextView mPlatformLinkTextView;
    private TextView mPlatformnameTextView;
    private EditText mUsernameEditText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_social);
        //Declamre view vars
        mUsernameEditText = (EditText)findViewById(R.id.editText);
        mPlatformnameTextView  = (TextView)findViewById(R.id.textView2);
        mPlatformLinkTextView  = (TextView)findViewById(R.id.textView3);
        mImage = (ImageView)findViewById(R.id.imageView);

        //Get intent vars
        Intent social = getIntent();
        username = social.getStringExtra("Username");
        platformLink = social.getStringExtra("Platform Link");
        platformName = social.getStringExtra("Platform Name");
        platformImage = social.getStringExtra("Platform Image");
        socialId = social.getStringExtra("ID");
        currentUser = social.getStringExtra("Current User");

        //Firestore vars
        socialRef = db.collection("users").document(currentUser).collection("socials").document(socialId);


        loadDataToView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void loadDataToView() {
        mUsernameEditText.setText(username);
        mPlatformnameTextView.setText(platformName);
        mPlatformLinkTextView.setText(platformLink);
        Glide.with(this).load(platformImage).into(mImage);
    }
    public void updateUsername(View v){
        String usernameToUpdate = mUsernameEditText.getText().toString();
        socialRef.update("username", usernameToUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
        Toast.makeText(SocialActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
        username = mUsernameEditText.getText().toString();
    }
    public void deleteSocial(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Do you really want to delete " + platformName)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        socialRef
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });
                        Toast.makeText(SocialActivity.this, "Succesfully deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }
    public void openSocial(View v){
        Intent i = new Intent(Intent.ACTION_VIEW);
        Log.v(TAG, "Whatsinhere " + platformLink + username);
        i.setData(Uri.parse(platformLink+username));
        startActivity(i);
    }

}
