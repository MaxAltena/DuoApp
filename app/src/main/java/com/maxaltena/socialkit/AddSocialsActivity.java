package com.maxaltena.socialkit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

public class AddSocialsActivity extends AppCompatActivity {

    //Final vars
    public static final String SOCIAL_MEDIA_USERNAME = "username";
    public static final String SOCIAL_MEDIA_PLATFORM = "platform";
    public static final String TAG = "Saved";


    //db
    private DocumentReference mDocRef;

    String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_socials);

        Intent intent = getIntent();
        UID = intent.getStringExtra("userUID");
    }


    public DocumentReference setDocumentReference(){
        DocumentReference DocumentRef = FirebaseFirestore.getInstance().collection("users").document(UID).collection("socials").document();
        return DocumentRef;
    }
    public void saveNewSocial(View view){

        EditText socialMediaPlatformView = (EditText) findViewById(R.id.editTextPlatform);
        EditText socialMediaUsernameView = (EditText) findViewById(R.id.editTextUsername);
        String socialMediaPlatformText = socialMediaPlatformView.getText().toString();
        String socialMediaUsernameText = socialMediaUsernameView.getText().toString();
        DocumentReference platform = FirebaseFirestore.getInstance().collection("platforms").document(socialMediaPlatformText.toLowerCase());

        mDocRef = setDocumentReference();

        if(socialMediaPlatformText.isEmpty() || socialMediaUsernameText.isEmpty()){
            return;
        }else{
            Map<String, Object> dataToSave = new HashMap<String, Object>();
            dataToSave.put(SOCIAL_MEDIA_PLATFORM, platform);
            dataToSave.put(SOCIAL_MEDIA_USERNAME, socialMediaUsernameText);

            mDocRef.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Document probably saved to " + mDocRef.getPath());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Document saving failed", e);
                }
            });
        }

    }
}
