package com.maxaltena.socialkit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    public static final int RC_SIGN_IN = 1;
    public static final int REQUEST_COMPLETE_CODE = 69;
    public static final String TAG = "Saved";

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public String loggedInUserUid;
    public String loggedInUserDisplayname;

    // List variables
    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mPlatformNames = new ArrayList<>();
    private ArrayList<String> mPlatformLinks = new ArrayList<>();
    private ArrayList<String> mIds = new ArrayList<>();
    private HashMap<String, ArrayList<String>> platformhashmap = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> completeHashmap = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> platformData = new ArrayList<>();

    // References
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    final ArrayList<String> socialArray = new ArrayList<String>();

    // Layout
    private BottomNavigationView mMainNav;
    private FrameLayout mMainFrame;
    private ProfileFragment profileFragment;
    private SearchFragment searchFragment;
    private GroupFragment groupFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("  SocialKit");
        getSupportActionBar().setIcon(getDrawable(R.mipmap.socialkit_logo));
        // Navigation
        mMainNav = findViewById(R.id.navigation);
        mMainFrame = findViewById(R.id.mainFrame);
        profileFragment = new ProfileFragment();
        searchFragment = new SearchFragment();
        groupFragment = new GroupFragment();
        setFragment(profileFragment);
        mMainNav.setOnNavigationItemSelectedListener(navListener);

        // Initialize Firebase Components
        mFirebaseAuth = FirebaseAuth.getInstance();
        // Reference and listeners
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Get user
                FirebaseUser user = firebaseAuth.getCurrentUser();

                // Check if user is logged in
                if(user!= null){
                    // User is signed in
                    loggedInUserDisplayname = user.getDisplayName();
                    loggedInUserUid = user.getUid();


                    db.collection("users").document(loggedInUserUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    initAllPlatforms();
                                    openQR();
                                    getUserInfo();
                                } else {
                                    addUserToDB();
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });



                } else {
                    // TODO: SignIn -> Maak nieuwe firestore document onder Users met zelfde structuur die er nu is.
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

    private void addUserToDB() {
        HashMap<String, Object> input = new HashMap<>();
        input.put("name", loggedInUserDisplayname);
        input.put("username", "Null");

        db.collection("users").document(loggedInUserUid).set(input).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully written!");

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });;
        createUsername();
    }

    private void createUsername() {
        Intent intent = new Intent(this, AddUsername.class);
        intent.putExtra("id", loggedInUserUid);
        intent.putExtra("displayname", loggedInUserDisplayname);
        startActivity(intent);
    }

    private void openQR() {
        ImageView imageViewQR = findViewById(R.id.imageViewQR);
        imageViewQR.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String text2Qr = Global.username;
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE,200,200);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    Intent intent = new Intent(MainActivity.this, QrPopUp.class);
                    intent.putExtra("pic",bitmap);
                    MainActivity.this.startActivity(intent);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void getUserInfo(){
        // Get username and name with query
        Log.d(TAG, "HHHHHHHHHHH" + loggedInUserDisplayname);
        db.collection("users")
                .document(loggedInUserUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.get("username").equals("null")){
                                createUsername();
                            }
                            Log.d(TAG, "qqqqqqqqqqqqqqq" + document.get("username"));
                            Global.username = document.get("username").toString();
                            Global.name = document.get("name").toString();

                            TextView textViewUsername = findViewById(R.id.textViewUsersname);
                            TextView textViewName = findViewById(R.id.textViewName);
                            textViewUsername.setText(Global.username);
                            textViewName.setText(Global.name);
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch(menuItem.getItemId()){
                case R.id.profile_menu:
                    resetLoadedData();
                    setFragment(profileFragment);
                    initAllPlatforms();
                    return true;
                case R.id.search_menu:
                    setFragment(searchFragment);
                    return true;
                case R.id.group_menu:
                    setFragment(groupFragment);
                    return true;
                default:
                    return false;
            }
        }
    };

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();
    }

    private void resetLoadedData() {
        mUsernames.clear();
        mImageUrls.clear();
        mPlatformLinks.clear();
        mIds.clear();
        mPlatformNames.clear();
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
                                if(!document.getId().isEmpty()){platformInfo.add(document.getId()); }
                                if(!document.get("image").toString().isEmpty()){ platformInfo.add(document.get("image").toString()); }
                                if(!document.get("link").toString().isEmpty()){ platformInfo.add(document.get("link").toString()); }
                                if(!document.get("name").toString().isEmpty()){ platformInfo.add(document.get("name").toString()); }
                                Log.d(TAG, "Error getting documents: ", task.getException());
                                MakeHashMap(platformInfo);
                            }
                            getSocials();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    protected void getSocials() {
        db.collection("users")
                .document(loggedInUserUid)
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
                            updateSocial(social.getUsername(), oldIndex);
                            break;
                        case REMOVED:
                            removeSocial(oldIndex);
                            break;
                    }
                }
            }
        });
    }

    private void removeSocial(int oldIndex) {
        mUsernames.remove(oldIndex);
        mImageUrls.remove(oldIndex);
        mPlatformLinks.remove(oldIndex);
        mIds.remove(oldIndex);
        mPlatformNames.remove(oldIndex);
        initRecyclerView();
    }

    private void updateSocial(String newUsername, int oldIndex) {
        mUsernames.set(oldIndex, newUsername);
        initRecyclerView();
    }

    private void MakeHashMap(ArrayList<String> array) {
        platformhashmap.put(array.get(0), array);
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
        if(!socialArray.get(1).isEmpty()){
            mIds.add(socialArray.get(1));
        }else{
            mIds.add("Platform Id add error");
        }
        completeHashmap.put(platformData.get(3), platformData);
        initRecyclerView();
    }
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView called");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mUsernames, mImageUrls, mPlatformNames, mPlatformLinks, mIds);
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
        resetLoadedData();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //Replaces toolbar_menu with top_menu.xml    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
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
            case R.id.camera_menu:
                // Camera clicked
                startActivity(new Intent(this, CameraActivity.class));
                return true;
            case R.id.settings_menu:
                // Settings clicked
                Toast.makeText(this, "Settings was clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.sign_out_menu:
                //Sign out
                resetLoadedData();
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
        startActivityForResult(intent, REQUEST_COMPLETE_CODE);
    }
}
