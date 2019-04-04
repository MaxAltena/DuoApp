package com.maxaltena.socialkit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AddUsername extends Activity {
    private EditText editTextUsername;
    private String mUsername;
    private String loggedInUserUid;
    final String TAG = "AddUsername";
    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_username);
        editTextUsername = findViewById(R.id.editTextUsername);
        btn = findViewById(R.id.buttonGo);
        Intent intent = getIntent();
        loggedInUserUid = intent.getStringExtra("id");
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                addUsername();
            }
            });

    }

    public void addUsername(){
        Log.d(TAG, loggedInUserUid);
        mUsername = editTextUsername.getText().toString();
        FirebaseFirestore.getInstance().collection("users").document(loggedInUserUid).update("username", mUsername).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               finish();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       //error
                    }
                });
    }

}
