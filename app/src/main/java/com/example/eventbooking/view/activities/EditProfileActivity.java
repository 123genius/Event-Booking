package com.example.eventbooking.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventbooking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    private CircleImageView profileImage;
    private Uri imageUri;
    private DatabaseReference mDatabase;
    private EditText txtProfileName;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private ProgressDialog mProgress;
    private FrameLayout mainContent;
    private ProgressBar mProgressBar;
    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mProgress = new ProgressDialog(this);
        profileImage = findViewById(R.id.profileImage);
        mainContent = findViewById(R.id.mainContent);
        mainLayout = findViewById(R.id.mainLayout);
        txtProfileName = findViewById(R.id.txtName);
        mProgressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        txtProfileName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!v.getText().toString().equals("")) {
                        mDatabase.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("name").setValue(v.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        txtProfileName.clearFocus();
                                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                        if (imm != null) {
                                            imm.hideSoftInputFromWindow(txtProfileName.getWindowToken(),
                                                    InputMethodManager.RESULT_UNCHANGED_SHOWN);
                                        }
                                        Snackbar.make(mainContent, "Name Updated Successfully!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                txtProfileName.clearFocus();
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(txtProfileName.getWindowToken(),
                                            InputMethodManager.RESULT_UNCHANGED_SHOWN);
                                }
                                Snackbar.make(mainContent, "Operation Failed! Try Again", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
                            }
                        });
                    }
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getData();
    }

    private void getData() {
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabase.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
                txtProfileName.setText(Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());
                if (dataSnapshot.child("image").getValue() != null) {
                    Picasso.with(EditProfileActivity.this).load(Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString()).into(profileImage);
                } else {
                    profileImage.setImageResource(R.drawable.account_icon);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void ButtonClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogout:
                logout();
                break;
            case R.id.profileImage:
                selectImageFromGallery();
                break;
            case R.id.btnBack:
                super.onBackPressed();
        }
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(EditProfileActivity.this, RootActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        EditProfileActivity.this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            uploadImage(profileImage);
        } else {
            imageUri = null;
        }
    }

    private void uploadImage(final CircleImageView profileImage) {
        mProgress.setMessage("Uploading Your Image");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();
        final String uId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        final StorageReference riversRef = mStorageRef.child("profileImages").child(uId + ".jpg");

        riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                Map map = new HashMap();
                                map.put("image", uri.toString());
                                mDatabase.child(uId).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        mProgress.dismiss();
                                        Picasso.with(EditProfileActivity.this).load(uri.toString()).into(profileImage);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mProgress.dismiss();

                                        Snackbar.make(mainContent, "Uploading Failed!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
                                    }
                                });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        mProgress.dismiss();
                        Snackbar.make(mainContent, "Uploading Failed!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
                    }
                });
    }
}
