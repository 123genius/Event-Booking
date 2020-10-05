package com.example.eventbooking.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventbooking.R;
import com.example.eventbooking.model.RequestModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestViewActivity extends AppCompatActivity {

    private RequestModel requestModel;
    private String userId, image, date, time, guest, menus, totalBill, userType, hallName, phone;
    private TextView txtName, txtContactNumber, txtHallName, txtDate, txtTime, txtGuest, txtMenus, txtBill;
    private CircleImageView profileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, mDeleteDatabase, mConfirmDatabase, mHallDatabse;
    private String mCurrentUserId;

    private Button btnDelete, btnResponse;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_view);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        requestModel = (RequestModel) getIntent().getSerializableExtra("requestModel");
        userId = getIntent().getStringExtra("userId");
        image = getIntent().getStringExtra("image");

        builder = new AlertDialog.Builder(this);
        btnResponse = findViewById(R.id.btnRespond);
        btnDelete = findViewById(R.id.btnDelete);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId);
        mDeleteDatabase = FirebaseDatabase.getInstance().getReference();
        mConfirmDatabase = FirebaseDatabase.getInstance().getReference().child("Bookings");
        mHallDatabse = FirebaseDatabase.getInstance().getReference().child("Halls").child(requestModel.getHall_id());
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userType = (String) dataSnapshot.child("type").getValue();
                if (Objects.requireNonNull(userType).equals("admin")) {
                    btnResponse.setVisibility(View.VISIBLE);
                    btnDelete.setVisibility(View.GONE);
                } else {
                    btnResponse.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileImage = findViewById(R.id.profileImage);
        txtName = findViewById(R.id.txtName);
        txtContactNumber = findViewById(R.id.txtContactNumber);
        txtHallName = findViewById(R.id.txtHallName);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        txtGuest = findViewById(R.id.txtGuest);
        txtMenus = findViewById(R.id.txtMenus);
        txtBill = findViewById(R.id.txtBill);

        Picasso.with(this).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(profileImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(RequestViewActivity.this).load(image).into(profileImage);
            }
        });

        date = "<b>" + "Booking Date: " + "</b>" + requestModel.getDate();
        time = "<b>" + "Booking Time: " + "</b>" + requestModel.getTime();
        guest = "<b>" + "Total Guests: " + "</b>" + requestModel.getGuest();
        menus = "<b>" + "Menu: " + "</b>" + requestModel.getMenus().replace(",", " ");
        totalBill = "<b>" + "Total Bill: " + "</b>" + requestModel.getTotal_bill();
        hallName = "<b>" + "Hall Name: " + "</b>" + requestModel.getHall_name();
        phone = "<b>" + "Phone No: " + "</b>" + requestModel.getCustomer_number();

        txtName.setText(requestModel.getCustomer_name());
        txtContactNumber.setText(Html.fromHtml(phone));
        txtHallName.setText(Html.fromHtml(hallName));
        txtDate.setText(Html.fromHtml(date));
        txtTime.setText(Html.fromHtml(time));
        txtGuest.setText(Html.fromHtml(guest));
        txtMenus.setText(Html.fromHtml(menus));
        txtBill.setText(Html.fromHtml(totalBill));

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequest();
            }
        });

        btnResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.setMessage("Would You Like To Confirm This Request?").setCancelable(true).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        acceptRequest();
                    }
                }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRequest();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setTitle("Recieved Request");
                alert.show();
            }
        });
    }

    private void deleteRequest() {
        mDeleteDatabase.child("booking_request").child(mCurrentUserId).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mDeleteDatabase.child("booking_request").child(userId).child(mCurrentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RequestViewActivity.this, "Request Deleted!", Toast.LENGTH_SHORT).show();
                                RequestViewActivity.super.onBackPressed();
                            } else {
                                Toast.makeText(RequestViewActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RequestViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(RequestViewActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RequestViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptRequest() {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("customer_name", requestModel.getCustomer_name());
        data.put("customer_number", requestModel.getCustomer_number());
        data.put("event_date", requestModel.getDate());
        data.put("event_time", requestModel.getTime());
        data.put("total_guest", requestModel.getGuest());
        data.put("hall_id", requestModel.getHall_id());
        data.put("hall_name", requestModel.getHall_name());
        data.put("menus", requestModel.getMenus());
        data.put("total_bill", requestModel.getTotal_bill());
        data.put("payment_status", "paid");
        data.put("user_id", userId);
        data.put("rating", "0");
        data.put("review", "no reivew given by user");
        data.put("admin_id", mCurrentUserId);
        data.put("event_status", "Happening");

        mConfirmDatabase.push().setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> data2 = new HashMap<>();
                    data2.put("status", "Booked");
                    mHallDatabse.updateChildren(data2).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(RequestViewActivity.this, "You have successfully given " + requestModel.getHall_name() + " on one day rent!", Toast.LENGTH_LONG).show();
                            deleteRequest();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RequestViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RequestViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void ButtonClick(View view) {
        super.onBackPressed();
    }
}
