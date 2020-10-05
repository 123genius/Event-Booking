package com.example.eventbooking.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventbooking.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddHallActivity extends AppCompatActivity {

    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;
    private String latitude, longitude;
//    private String[] statusList = {"Select Status", "Available", "Booked"};

    private ImageView mSelectImage;
    private static final int GALLERY_REQUEST = 1;
    private EditText mNameText, mCapacityText, mNumberText, mEmailText, mMinimumCapacity, mParkingText, mWashroomsText, mRentText;
    private String name, capacity, number, email, minimumCapacity, parking, washroom, rent;
//    private Spinner mStatusDropdown;
    private ScrollView mainContent;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private Uri imageUri = null;
    private DatabaseReference mDatabase, mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hall);

        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(this);

//        mStatusDropdown = findViewById(R.id.statusDropDown);
        mNameText = findViewById(R.id.txtName);
        mCapacityText = findViewById(R.id.txtCapacity);
        mNumberText = findViewById(R.id.txtPhone);
        mEmailText = findViewById(R.id.txtEmail);
        mMinimumCapacity = findViewById(R.id.txtMinimumCapacity);
        mParkingText = findViewById(R.id.txtParkingCapacity);
        mWashroomsText = findViewById(R.id.txtWashrooms);
        mRentText = findViewById(R.id.txtRent);

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, statusList){
//            @Override
//            public boolean isEnabled(int position){
//                if(position == 0)
//                {
//                    return false;
//                }
//                else
//                {
//                    return true;
//                }
//            }
//            @Override
//            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
//                View view = super.getDropDownView(position, convertView, parent);
//                TextView tv = (TextView) view;
//                if(position == 0){
//                    tv.setTextColor(Color.GRAY);
//                }
//                else {
//                    tv.setTextColor(Color.BLACK);
//                }
//                return view;
//            }
//        };
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mStatusDropdown.setAdapter(adapter);
//        mStatusDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedItemText = (String) parent.getItemAtPosition(position);
//                if(position > 0){
//
//                } else {
//                    status = selectedItemText;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        mSelectImage = findViewById(R.id.hallImage);
        mainContent = findViewById(R.id.mainContent);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Halls");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    public void ButtonClick(View view) {
        if (view.getId() == R.id.btnLocation) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE);
        }
        if (view.getId() == R.id.hallImage) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Toast.makeText(this, "Location Selected Successfully", Toast.LENGTH_LONG).show();
                    latitude = data.getStringExtra("latitude");
                    longitude = data.getStringExtra("longitude");
                }
            }
        }
        if (requestCode == GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    imageUri = data.getData();
                    mSelectImage.setImageURI(imageUri);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btnSubmit) {
            uploadingPicture();
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadingPicture() {
        name = mNameText.getText().toString();
        capacity = mCapacityText.getText().toString();
        number = mNumberText.getText().toString();
        email = mEmailText.getText().toString();
        minimumCapacity = mMinimumCapacity.getText().toString();
        parking = mParkingText.getText().toString();
        washroom = mWashroomsText.getText().toString();
        rent = mRentText.getText().toString();
        if ((TextUtils.isEmpty(latitude) && TextUtils.isEmpty(longitude)) || (latitude.equals("0.0") && longitude.equals("0.0"))) {
            Snackbar.make(mainContent, "Select Your Location First!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
        }else if (imageUri == null) {
            Snackbar.make(mainContent, "Select Image First!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
        } else if (TextUtils.isEmpty(name)) {
            mNameText.setError("Name required");
        } else if (TextUtils.isEmpty(capacity)) {
            mCapacityText.setError("Person's Capacity required");
        } else if (TextUtils.isEmpty(number)) {
            mNumberText.setError("Contact Number required");
        }  else if (TextUtils.isEmpty(rent)) {
            mRentText.setError("Rent required");
        } else if (TextUtils.isEmpty(email)) {
            mEmailText.setError("Email required");
        } else if (TextUtils.isEmpty(minimumCapacity)) {
            mMinimumCapacity.setError("Minimum Capacity required");
        } else if (TextUtils.isEmpty(parking)) {
            mParkingText.setError("Parking Capacity required");
        } else if (TextUtils.isEmpty(washroom)) {
            mWashroomsText.setError("Number of Washrooms required");
        } else {
            mProgress.setMessage("Uploading Your Image");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
            final StorageReference riversRef = mStorage.child("Hall_Images").child(Objects.requireNonNull(imageUri.getLastPathSegment()));
            riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            mUserDatabase.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Map<String, Object> map = new HashMap();
                                    map.put("name", name);
                                    map.put("maximum_capacity", capacity);
                                    map.put("contact_number", number);
                                    map.put("email", email);
                                    map.put("minimum_capacity", minimumCapacity);
                                    map.put("parking", parking);
                                    map.put("number_of_washrooms", washroom);
                                    map.put("status", "Available");
                                    map.put("latitude", latitude);
                                    map.put("longitude", longitude);
                                    map.put("rent", rent);
                                    map.put("rating", "0");
                                    map.put("uid", mAuth.getCurrentUser().getUid());
                                    map.put("user_name", Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());
                                    map.put("imageUrl", uri.toString());
                                    map.put("userImage", Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString());
                                    final DatabaseReference newPost = mDatabase.push();
                                    newPost.setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProgress.dismiss();
                                            AddHallActivity.super.onBackPressed();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mProgress.dismiss();
                                            Snackbar.make(mainContent, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgress.dismiss();
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgress.dismiss();
                    Snackbar.make(mainContent, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_hall_menu, menu);
        return true;
    }
}
