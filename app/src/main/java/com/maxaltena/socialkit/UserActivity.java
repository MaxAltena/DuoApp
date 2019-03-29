package com.maxaltena.socialkit;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "User Activity ";

    private HashMap<String, ArrayList<String>> platformhashmap = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> platformData = new ArrayList<>();
    final ArrayList<String> socialArray = new ArrayList<String>();
    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mPlatformNames = new ArrayList<>();
    private ArrayList<String> mPlatformLinks = new ArrayList<>();
    private String lookedUpUser;
    // References
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        //TODO Make dynamic
        lookedUpUser = "mudX2lxxQkXZCaObeqJQPIRgBWm1";
        initAllPlatforms();
    }

    private void initAllPlatforms() {
        db.collection("platforms")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ArrayList<String> platformInfo = new ArrayList<>();
                                if(!document.getId().isEmpty()){platformInfo.add(document.getId()); }
                                if(!document.get("image").toString().isEmpty()){ platformInfo.add(document.get("image").toString()); }
                                if(!document.get("link").toString().isEmpty()){ platformInfo.add(document.get("link").toString()); }
                                if(!document.get("name").toString().isEmpty()){ platformInfo.add(document.get("name").toString()); }
                                Log.d(TAG, "Error getting documents: ", task.getException());
                                MakeHashMap(platformInfo);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        getSocials();
                    }
                });
    }
    private void MakeHashMap(ArrayList<String> array) {
        platformhashmap.put(array.get(0), array);
    }
    protected void getSocials() {
        db.collection("users")
                .document(lookedUpUser)
                .collection("socials")
                .orderBy("platform")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        //Check if something went wrong
                        if (e !=  null){
                            Log.d(TAG, e.toString());
                            return;
                        }
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                            DocumentSnapshot documentSnapshot = dc.getDocument();
                            Social social = documentSnapshot.toObject(Social.class);
                            int oldIndex = dc.getOldIndex();
                            final String platformPath = documentSnapshot.getDocumentReference("platform").getPath();
                            String[] platformPathParts = platformPath.split("/");
                            String platform = platformPathParts[1];
                            socialArray.clear();
                            switch (dc.getType()){
                                case ADDED:
                                    socialArray.add(social.getUsername());
                                    socialArray.add(documentSnapshot.getId());
                                    initImageBitmaps(socialArray, platform);
                                    break;
                                case MODIFIED:
                                    break;
                                case REMOVED:
                                    break;

                            }
                        }
                    }
                });
    }
    private void initImageBitmaps(ArrayList<String> socialArray, String platform){
        Log.d(TAG, "initImageBitmaps called");
        platformData.clear();
        if(platformhashmap.containsKey(platform)){
            platformData = platformhashmap.get(platform);
        }else{
            return;
        }

        if(!socialArray.get(0).isEmpty()){
            mUsernames.add(socialArray.get(0));
        }else{
            mUsernames.add("Username add error");
        }
        if(!platformData.get(1).isEmpty()){
            mImageUrls.add(platformData.get(1));
        }else{
            mImageUrls.add("Image add error");
        }
        if(!platformData.get(2).isEmpty()){
            mPlatformLinks.add(platformData.get(2));
        }else{
            mPlatformLinks.add("Platform link add error");
        }
        if(!platformData.get(3).isEmpty()){
            mPlatformNames.add(platformData.get(3));
        }else{
            mPlatformNames.add("Platform name add error");
        }
        if(!socialArray.get(0).isEmpty()){
            platformData.add(socialArray.get(0));
        }else{
            platformData.add("Platform name add error");
        }
        initRecyclerView();
        Log.d(TAG, "YAAAA" + platformhashmap.toString());
    }
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView called");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapterUser adapter = new RecyclerViewAdapterUser(this, mUsernames, mImageUrls, mPlatformNames, mPlatformLinks);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

}
