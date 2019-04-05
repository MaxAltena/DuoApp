package com.maxaltena.socialkit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddSocialsActivity extends AppCompatActivity {
    //Final vars
    public static final String SOCIAL_MEDIA_USERNAME = "username";
    public static final String SOCIAL_MEDIA_PLATFORM = "platform";
    public static final String TAG = "Saved";

    //db
    private DocumentReference mDocRef;

    String UID;

    // TODO: Laad platforms in vanuit database in een dropdown

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_socials);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add new social");

        Intent intent = getIntent();
        UID = intent.getStringExtra("userUID");
    }

    public DocumentReference setDocumentReference(){
        return FirebaseFirestore.getInstance().collection("users").document(UID).collection("socials").document();
    }

    public void saveNewSocial(View view){
        EditText socialMediaPlatformView = findViewById(R.id.editTextPlatform);
        EditText socialMediaUsernameView = findViewById(R.id.editTextUsername);
        String socialMediaPlatformText = socialMediaPlatformView.getText().toString();
        String socialMediaUsernameText = socialMediaUsernameView.getText().toString();
        DocumentReference platform = FirebaseFirestore.getInstance().collection("platforms").document(socialMediaPlatformText.toLowerCase());

        mDocRef = setDocumentReference();

        if(socialMediaPlatformText.isEmpty() || socialMediaUsernameText.isEmpty()){
            return;
        } else {
            Map<String, Object> dataToSave = new HashMap<String, Object>();
            dataToSave.put(SOCIAL_MEDIA_PLATFORM, platform);
            dataToSave.put(SOCIAL_MEDIA_USERNAME, socialMediaUsernameText);

            mDocRef.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Document probably saved to " + mDocRef.getPath());
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Document saving failed", e);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.camera_menu:
                // Camera
                startActivity(new Intent(this, CameraActivity.class));
                return true;
            case R.id.settings_menu:
                // Settings
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.sign_out_menu:
                // Sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                // Back
                startActivity(new Intent(this, MainActivity.class));
                return super.onOptionsItemSelected(item);
        }
    }
}