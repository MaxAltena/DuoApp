package com.maxaltena.socialkit;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    //More vars
    public String loggedInUserUid;

    //auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    //User vars
    ArrayList<ArrayList<String>> allSocials = new ArrayList<ArrayList<String>>();

    //References
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference socialRef;

    //View vars
    public TextView mTextViewData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Components
        mFirebaseAuth = FirebaseAuth.getInstance();

        //View vars initialize
        mTextViewData = findViewById(R.id.TextViewData);

        //Reference and listeners
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Get user
                FirebaseUser user = firebaseAuth.getCurrentUser();


                //Check if user is logged in
                if(user!= null){
                    //user is signed in
                    loggedInUserUid = user.getUid();
                    socialRef = db.collection("users").document(loggedInUserUid).collection("socials");
                    //getSocials();
                    getSocials2();

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


    protected void getSocials2() {
        db.collection("users").document(loggedInUserUid).collection("socials").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
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
                    int i = 0;
                    int newIndex = dc.getNewIndex();


                    switch (dc.getType()){
                        case ADDED:
                            mTextViewData.append("\nAdded: " + social.getUsername());
                            break;
                        case MODIFIED:
                            mTextViewData.append("\nModified: " + social.getUsername());
                            break;
                        case REMOVED:
                            mTextViewData.append("\nRemoved: " + social.getUsername());
                            break;

                    }
                }
            }
        });
    }

    //Get socials stuff
    public void getSocials(){
        CollectionReference readDocumentRef = db.collection("users").document(loggedInUserUid).collection("socials");
        readDocumentRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        // Reset array
                        allSocials = new ArrayList<>();

                        for (final QueryDocumentSnapshot doc : value) {
                            if (doc.get("platform") != null && doc.get("username") != null) {
                                final ArrayList<String> social = new ArrayList<String>();

                                final String platformPath = doc.getDocumentReference("platform").getPath();
                                String[] platformPathParts = platformPath.split("/");
                                String platform = platformPathParts[1];
                                String username = doc.getString("username");

                                social.add(username);
                                social.add(platform);

                                // Below could be a method
                                DocumentReference readDocumentRef = db.collection("platforms").document(platform);
                                readDocumentRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                        @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen failed.", e);
                                            return;
                                        }

                                        String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                                                ? "Local" : "Server";

                                        if (snapshot != null && snapshot.exists()) {
                                            Log.d(TAG, source + " PlatformData: " + snapshot.getData());

                                            // This could be outside of method if I knew how to return correctly

                                            String link = snapshot.getString("link");
                                            String image = snapshot.getString("image");

                                            social.add(link);
                                            social.add(image);

                                            allSocials.add(social);

                                            Log.d(TAG, "Platforms: " + allSocials);
                                        } else {
                                            Log.d(TAG, source + " PlatformData: null");
                                        }
                                    }
                                });
                                // End of possible method
                            }
                        }
                    }
                });
    }

    //Get platforms stuff
    public void getPlatforms(String ref){
        String[] parts = ref.split("/");
        String collection = parts[0];
        String document = parts[1];
        DocumentReference readDocumentRef = db.collection(collection).document(document);
        readDocumentRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, source + " dataaaaaaaaaaaaa: " + snapshot.getData());
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });
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
