package com.example.eventbooking.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventbooking.R;
import com.example.eventbooking.model.HallModel;
import com.example.eventbooking.model.MenuModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditHallInfoActivity extends AppCompatActivity {

    private HallModel hallModel;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;
    public static final int PICK_IMAGE = 1;
    private Uri imageUri;
    private RecyclerView mHallListView;
    private LinearLayoutManager mManager;
    private EditText txtName, txtPrice;
    private ScrollView mainContent;
    private String postKey, latitude, longitude;
    private String name, capacity, number, minimumCapacity, parking, washroom;

    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabase, mDatabase2, mUpdateDatabase;

    private EditText txtContact, txtHallName, txtMinimumCapacity, txtPersonsCapacity, txtParking, txtWashrooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_hall_info);
        hallModel = (HallModel) getIntent().getSerializableExtra("model");
        postKey = getIntent().getStringExtra("postKey");

        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Menus");
        mUpdateDatabase = FirebaseDatabase.getInstance().getReference().child("Halls").child(postKey);
        mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Menus").child(postKey);
        mDatabase2.keepSynced(true);

        mHallListView = findViewById(R.id.menuList);
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mHallListView.setLayoutManager(mManager);

        mainContent = findViewById(R.id.mainContent);
        txtName = findViewById(R.id.txtName);
        txtPrice = findViewById(R.id.txtPrice);

        txtContact = findViewById(R.id.txtContactNumber);
        txtHallName = findViewById(R.id.txtHallName);
        txtMinimumCapacity = findViewById(R.id.txtMinimumCapacity);
        txtPersonsCapacity = findViewById(R.id.txtCapacity);
        txtParking = findViewById(R.id.txtParkingCapacity);
        txtWashrooms = findViewById(R.id.txtWashrooms);

        txtContact.setText(hallModel.getContact_number());
        txtHallName.setText(hallModel.getName());
        txtMinimumCapacity.setText(hallModel.getMaximum_capacity());
        txtPersonsCapacity.setText(hallModel.getMaximum_capacity());
        txtParking.setText(hallModel.getParking());
        txtWashrooms.setText(hallModel.getNumber_of_washrooms());
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
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
        } else {
            imageUri = null;
        }
    }

    public void ButtonClick(View view) {
        if (view.getId() == R.id.btnAddImage) {
            selectImageFromGallery();
        }
        if (view.getId() == R.id.btnAddMenu) {
            checkMenuStatus();
        }
        if (view.getId() == R.id.btnLocation) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE);
        }
    }

    private void checkMenuStatus() {
        String name = txtName.getText().toString();
        String price = txtPrice.getText().toString();
        if (imageUri == null) {
            Snackbar.make(mainContent, "Select Image First!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
        } else if (TextUtils.isEmpty(name)) {
            txtName.setError("Menu Name Required");
        } else if (TextUtils.isEmpty(price)) {
            txtName.setError("Menu Price Required");
        } else {
            addToFirebase(imageUri, name, price);
        }
    }

    private void addToFirebase(final Uri imageUri, final String name, final String price) {
        mProgress.setMessage("Adding Your Menu");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();
        final StorageReference riversRef = mStorage.child("Menu_Images").child(Objects.requireNonNull(imageUri.getLastPathSegment()));
        riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map<String, String> map = new HashMap();
                        map.put("menu_name", name);
                        map.put("menu_price", price);
                        map.put("menu_image", uri.toString());
                        final DatabaseReference newPost = mDatabase.child(postKey).push();
                        newPost.setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                txtName.setText("");
                                txtPrice.setText("");
                                mProgress.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mProgress.dismiss();
                                Snackbar.make(mainContent, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
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

    private void selectImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getMenuData();
    }

    private void getMenuData() {
        final FirebaseRecyclerAdapter<MenuModel, MenuViewHolder> mRecyclerAdapter = new FirebaseRecyclerAdapter<MenuModel, MenuViewHolder>(
                MenuModel.class,
                R.layout.menu_single_item,
                MenuViewHolder.class,
                mDatabase2
        ) {
            @Override
            protected void populateViewHolder(final MenuViewHolder viewHolder, final MenuModel model, final int i) {
                viewHolder.txtName.setText(model.getMenu_name());
                Picasso.with(EditHallInfoActivity.this).load(model.getMenu_image()).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.menuImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(EditHallInfoActivity.this).load(model.getMenu_image()).into(viewHolder.menuImage);
                    }
                });
                viewHolder.mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        CharSequence options[] = new CharSequence[] {"Delete", "Cancel"};
                        final AlertDialog.Builder builder = new AlertDialog.Builder(EditHallInfoActivity.this);
                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    mDatabase2.child(Objects.requireNonNull(getRef(i).getKey())).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(EditHallInfoActivity.this, "Menu Deleted!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                if (which == 1) {
                                    dialog.dismiss();
                                }
                            }
                        });
                        builder.show();
                        return true;
                    }
                });
            }
        };
        mHallListView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btnSubmit){
            updateData();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateData() {
        name = txtHallName.getText().toString();
        capacity = txtPersonsCapacity.getText().toString();
        number = txtContact.getText().toString();
        minimumCapacity = txtMinimumCapacity.getText().toString();
        parking = txtParking.getText().toString();
        washroom = txtWashrooms.getText().toString();
        if ((TextUtils.isEmpty(latitude) && TextUtils.isEmpty(longitude)) || (latitude.equals("0.0") && longitude.equals("0.0"))) {
            Snackbar.make(mainContent, "Select Your Location First!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
        } else if (TextUtils.isEmpty(name)) {
            txtHallName.setError("Name required");
        } else if (TextUtils.isEmpty(capacity)) {
            txtPersonsCapacity.setError("Person's Capacity required");
        } else if (TextUtils.isEmpty(number)) {
            txtContact.setError("Contact Number required");
        } else if (TextUtils.isEmpty(minimumCapacity)) {
            txtMinimumCapacity.setError("Minimum Capacity required");
        } else if (TextUtils.isEmpty(parking)) {
            txtParking.setError("Parking Capacity required");
        } else if (TextUtils.isEmpty(washroom)) {
            txtWashrooms.setError("Number of Washrooms required");
        } else {
            mProgress.setMessage("Uploading Your Image");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("maximum_capacity", capacity);
            map.put("contact_number", number);
            map.put("minimum_capacity", minimumCapacity);
            map.put("parking", parking);
            map.put("number_of_washrooms", washroom);
            map.put("manager_name", name);
            map.put("latitude", latitude);
            map.put("longitude", longitude);
            mUpdateDatabase.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mProgress.dismiss();
                    Snackbar.make(mainContent, "Information Updated!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(mainContent, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
                    mProgress.dismiss();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_hall_menu, menu);
        return true;
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView txtName;
        CircleImageView menuImage;
        LinearLayout mainLayout;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            txtName = mView.findViewById(R.id.txtName);
            menuImage = mView.findViewById(R.id.menuImage);
            mainLayout = mView.findViewById(R.id.mainLayout);
        }
    }
}
