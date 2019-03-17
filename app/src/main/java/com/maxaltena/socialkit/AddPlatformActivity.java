package com.maxaltena.socialkit;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class AddPlatformActivity extends AppCompatActivity {
    public static final String SOCIAL_MEDIA_IMAGELINK = "image";
    public static final String SOCIAL_MEDIA_NAME = "name";
    public static final String SOCIAL_MEDIA_LINK = "link";
    public static final String TAG = "Saved";

    //db
    private DocumentReference mWriteDocRef;
    private DocumentReference mReadPlatformsDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_platform);

        //View vars
        TextView allText = (TextView) findViewById(R.id.textViewAll);


        getPlatforms();
    }

    public void getPlatforms(){

        getReadDocRefence().addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    public DocumentReference getReadDocRefence(){
        DocumentReference readDocumentRef = FirebaseFirestore.getInstance().collection("platforms").document("facebook");
        return readDocumentRef;
    }

    public DocumentReference setWriteDocumentReference(String name){
        DocumentReference writeDocumentRef = FirebaseFirestore.getInstance().document("platforms/" + name);
        return writeDocumentRef;
    }


    public void saveNewPlatform(View view){

        EditText socialMediaLinkView = (EditText) findViewById(R.id.socialMediaLink);
        EditText socialMediaNameView = (EditText) findViewById(R.id.socialMediaName);
        EditText socialMediaImagelinkView = (EditText) findViewById(R.id.socialMediaImagelink);
        String socialMediaLinkText = socialMediaLinkView.getText().toString().toLowerCase();
        String socialMediaNameText = socialMediaNameView.getText().toString().toLowerCase();
        String socialMediaImagelinkText = socialMediaImagelinkView.getText().toString().toLowerCase();

        mWriteDocRef = setWriteDocumentReference(socialMediaNameText);

        if(socialMediaLinkText.isEmpty() || socialMediaNameText.isEmpty() || socialMediaImagelinkText.isEmpty()){return;}
        Map<String, Object> dataToSave = new HashMap<String, Object>();
        dataToSave.put(SOCIAL_MEDIA_LINK, socialMediaLinkText);
        dataToSave.put(SOCIAL_MEDIA_NAME, socialMediaNameText);
        dataToSave.put(SOCIAL_MEDIA_IMAGELINK, socialMediaImagelinkText);

        mWriteDocRef.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Document probably saved to " + mWriteDocRef.getPath());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Document saving failed", e);
            }
        });
    }
}
