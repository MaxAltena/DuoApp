package com.maxaltena.socialkit;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference accountRef = db.collection("users").document(Global.UID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");

        EditText editTextName = findViewById(R.id.editName);
        EditText editTextUsername = findViewById(R.id.editUsername);
        editTextName.setText(Global.name);
        editTextUsername.setText(Global.username);
    }

    public void updateProfile(View view){
        EditText editTextName = findViewById(R.id.editName);
        EditText editTextUsername = findViewById(R.id.editUsername);
        final String newName = editTextName.getText().toString();
        final String newUsername = editTextUsername.getText().toString();

        if(!newName.equals(Global.name) && !newUsername.equals(Global.username)){
            // Both name and username will update
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Do you really want to update your name and username?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            accountRef.update("name", newName)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Global.name = newName;
                                        }
                                    });
                            accountRef.update("username", newUsername)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Global.username = newUsername;
                                        }
                                    });
                            Toast.makeText(SettingsActivity.this, "Successfully updated name and username", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        } else if(!newName.equals(Global.name)){
            // Name changed
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Do you really want to update your name?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            accountRef.update("name", newName)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Global.name = newName;
                                        }
                                    });
                            Toast.makeText(SettingsActivity.this, "Successfully updated name", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        } else if(!newUsername.equals(Global.username)){
            // Username changed
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Do you really want to update your username?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            accountRef.update("username", newUsername)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Global.username = newUsername;
                                        }
                                    });
                            Toast.makeText(SettingsActivity.this, "Successfully updated username", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        } else {
            // Nothing changed
            Toast.makeText(this, "Updating nothing!", Toast.LENGTH_SHORT).show();
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
