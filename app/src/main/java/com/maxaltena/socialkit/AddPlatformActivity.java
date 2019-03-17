package com.maxaltena.socialkit;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddPlatformActivity extends AppCompatActivity {
    public static final String SOCIAL_MEDIA_IMAGELINK = "image";
    public static final String SOCIAL_MEDIA_NAME = "name";
    public static final String SOCIAL_MEDIA_LINK = "link";
    public static final String TAG = "Saved";
    //db
    private DocumentReference mDocRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_platform);
    }
    public DocumentReference setDocumentReference(String name){

        DocumentReference DocumentRef = FirebaseFirestore.getInstance().document("platforms/" + name);
        return DocumentRef;
    }
    public void saveNewEntry(View view){

        EditText socialMediaLinkView = (EditText) findViewById(R.id.socialMediaLink);
        EditText socialMediaNameView = (EditText) findViewById(R.id.socialMediaName);
        EditText socialMediaImagelinkView = (EditText) findViewById(R.id.socialMediaImagelink);
        String socialMediaLinkText = socialMediaLinkView.getText().toString().toLowerCase();
        String socialMediaNameText = socialMediaNameView.getText().toString().toLowerCase();
        String socialMediaImagelinkText = socialMediaImagelinkView.getText().toString().toLowerCase();

        mDocRef = setDocumentReference(socialMediaNameText);

        if(socialMediaLinkText.isEmpty() || socialMediaNameText.isEmpty() || socialMediaImagelinkText.isEmpty()){return;}
        Map<String, Object> dataToSave = new HashMap<String, Object>();
        dataToSave.put(SOCIAL_MEDIA_LINK, socialMediaLinkText);
        dataToSave.put(SOCIAL_MEDIA_NAME, socialMediaNameText);
        dataToSave.put(SOCIAL_MEDIA_IMAGELINK, socialMediaImagelinkText);

        mDocRef.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Document saved?");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Document saving failed", e);
            }
        });
    }
}
