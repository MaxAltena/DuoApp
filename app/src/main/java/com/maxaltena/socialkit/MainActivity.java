package com.maxaltena.socialkit;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // final vars
    public static final int RC_SIGN_IN = 1;
    public static final String TAG = "Saved";

    public String loggedInUserUid;

    //auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    //list vars
    private ArrayList<ArrayList> allPlatforms = new ArrayList<ArrayList>();
    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mPlatformNames = new ArrayList<>();
    private ArrayList<String> mPlatformLinks = new ArrayList<>();
    private HashMap<String, ArrayList<String>> platformhashmap = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> completeHashmap = new HashMap<String, ArrayList<String>>();

    //User vars
    ArrayList<ArrayList<String>> allSocials = new ArrayList<ArrayList<String>>();

    //References
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference socialRef;
    final ArrayList<String> socialArray = new ArrayList<String>();
    //View vars
    public TextView mTextViewData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Components
        mFirebaseAuth = FirebaseAuth.getInstance();


        //Reference and listeners
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Get user
                FirebaseUser user = firebaseAuth.getCurrentUser();


                //Check if user is logged in
                if(user!= null){
                    //user is signed in
                    initAllPlatforms();

                    loggedInUserUid = user.getUid();
                    socialRef = db.collection("users").document(loggedInUserUid).collection("socials");
                    //getSocials();


                } else {
                    //user is not signed in
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

    }
    private void initAllPlatforms(){

        db.collection("platforms")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ArrayList<String> platformInfo = new ArrayList<>();
                                if(!document.getId().isEmpty()){
                                    platformInfo.add(document.getId());
                                    //0
                                }
                                if(!document.get("image").toString().isEmpty()){
                                    platformInfo.add(document.get("image").toString());
                                    //1
                                }
                                if(!document.get("link").toString().isEmpty()){
                                    platformInfo.add(document.get("link").toString());
                                    //2
                                }
                                if(!document.get("name").toString().isEmpty()){
                                    platformInfo.add(document.get("name").toString());
                                    //3
                                }
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


    protected void getSocials() {
        db.collection("users").document(loggedInUserUid).collection("socials").orderBy("platform").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                //Check if somthing went wrong
                if (e !=  null){
                    Log.d(TAG, e.toString());
                    return;
                }
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                    DocumentSnapshot documentSnapshot = dc.getDocument();
                    Log.d(TAG, documentSnapshot.getData().toString());
                    Map<String, Object> data = documentSnapshot.getData();
                    Social social = documentSnapshot.toObject(Social.class);
                    int oldIndex = dc.getOldIndex();
                    int newIndex = dc.getNewIndex();
                    final String platformPath = documentSnapshot.getDocumentReference("platform").getPath();
                    String[] platformPathParts = platformPath.split("/");
                    String platform = platformPathParts[1];
                    Log.d(TAG, "here" + platform);
                    socialArray.clear();
                    switch (dc.getType()){
                        case ADDED:
                            socialArray.add(social.getUsername());
                            initImageBitmaps(socialArray, platform);
                            break;
                        case MODIFIED:
                            updateSocial(platform, social.getUsername(), oldIndex, newIndex);
                            break;
                        case REMOVED:
                            break;

                    }


                    Log.d(TAG, "dataaa  " + social.getUsername() + " " + oldIndex + " " + newIndex);
                    //mTextViewData.setText(socialArray.toString());
                }
            }
        });
    }

    private void updateSocial(String platform, String newUsername, int oldIndex, int newIndex) {
        String oldUsername = completeHashmap.get(platform).get(4);
        mUsernames.set(oldIndex, newUsername);
        initRecyclerView();
    }

    
    private void MakeHashMap(ArrayList<String> array) {
        platformhashmap.put(array.get(0), array);
    }

    private void initImageBitmaps(ArrayList<String> socialArray, String platform){
        Log.d(TAG, "initImageBitmaps called");
        Log.d(TAG, "HEEEY" + platform);


            ArrayList<String> platformData = new ArrayList<>();
            platformData = platformhashmap.get(platform);
            Log.d(TAG, "HAHAHA" + platformData);
            mUsernames.add(socialArray.get(0));
            mImageUrls.add(platformData.get(1));
            mPlatformLinks.add(platformData.get(2));
            mPlatformNames.add(platformData.get(3));
            platformData.add(socialArray.get(0));
            completeHashmap.put(platformData.get(3), platformData);
            Log.d(TAG, "Stoer" + completeHashmap.toString());
        initRecyclerView();
    }
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView called");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mUsernames, mImageUrls, mPlatformNames, mPlatformLinks);
        Log.d(TAG, "mImageUrls" + mImageUrls);
        Log.d(TAG, "mUsernames" + mUsernames);
        Log.d(TAG, "mPlatformLinks" + mPlatformLinks);
        Log.d(TAG, "mPlatformLinks" + mPlatformNames);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    //Auth stuff
    @Override
    protected void onPause(){
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
    @Override
    protected void onResume(){
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    //Replaces menu with main_menu.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    //Checks user activity in user sign in
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(MainActivity.this, "Welcome, to SocialKit!", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(MainActivity.this, "Bye :(", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    //logout thing
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sign_out_menu:
                //Sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //Change view
    public void StartAddSocialActivity(View view) {
        Intent intent = new Intent(this, AddSocialsActivity.class);
        intent.putExtra("userUID", loggedInUserUid);
        startActivity(intent);
    }
    //Change view
    public void StartAddPlatformActivity(View view) {
        Intent intent = new Intent(this, AddPlatformActivity.class);
        startActivity(intent);
    }
}
