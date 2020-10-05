package com.example.eventbooking.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventbooking.R;
import com.example.eventbooking.model.HallModel;
import com.example.eventbooking.model.MenuModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class HallViewActivity extends AppCompatActivity {

    private HallModel hallModel;
    private ImageView hallImage;
    private TextView txtContact, txtStatus, txtRating, txtEmail, txtHallName, txtMinimumCapacity, txtPersonsCapacity, txtParking, txtWashrooms, txtOwnerName;
    private Button btnBook, btnEdit;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, mDatabase2;
    private String postKey;

    private RecyclerView mHallListView;
    private LinearLayoutManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_view);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        postKey = getIntent().getStringExtra("postKey");

        hallModel = (HallModel) getIntent().getSerializableExtra("hallData");
        mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Menus").child(postKey);
        mDatabase2.keepSynced(true);
        btnBook = findViewById(R.id.btnBook);
        btnEdit = findViewById(R.id.btnEdit);

        mHallListView = findViewById(R.id.menuList);
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mHallListView.setLayoutManager(mManager);

        mDatabase.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Objects.equals(dataSnapshot.child("type").getValue(), "admin")){
                    btnEdit.setVisibility(View.VISIBLE);
                    btnBook.setVisibility(View.GONE);
                } else {
                    btnEdit.setVisibility(View.GONE);
                    btnBook.setVisibility(View.VISIBLE);
                    if (hallModel.getStatus().equals("Available")) {
                        btnBook.setEnabled(true);
                    } else {
                        btnBook.setText(R.string.alreadyBooked);
                        btnBook.setBackgroundColor(getResources().getColor(R.color.quantum_grey));
                        btnBook.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        hallImage = findViewById(R.id.hallImage);
        Picasso.with(this).load(hallModel.getImageUrl()).networkPolicy(NetworkPolicy.OFFLINE).into(hallImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(HallViewActivity.this).load(hallModel.getImageUrl()).into(hallImage);
            }
        });

        txtContact = findViewById(R.id.txtContactNumber);
        txtEmail = findViewById(R.id.txtEmail);
        txtHallName = findViewById(R.id.txtHallName);
        txtMinimumCapacity = findViewById(R.id.txtMinimumCapacity);
        txtPersonsCapacity = findViewById(R.id.txtCapacity);
        txtParking = findViewById(R.id.txtParkingCapacity);
        txtWashrooms = findViewById(R.id.txtWashrooms);
        txtOwnerName = findViewById(R.id.txtOwnerName);
        txtStatus = findViewById(R.id.txtStatus);
        txtRating = findViewById(R.id.txtRating);

        txtContact.setText(hallModel.getContact_number());
        txtEmail.setText(hallModel.getEmail());
        txtHallName.setText(hallModel.getName());
        txtMinimumCapacity.setText(hallModel.getMaximum_capacity());
        txtPersonsCapacity.setText(hallModel.getMaximum_capacity());
        txtParking.setText(hallModel.getParking());
        txtWashrooms.setText(hallModel.getNumber_of_washrooms());
        txtOwnerName.setText(hallModel.getUser_name());
        txtStatus.setText(hallModel.getStatus());
        txtRating.setText(hallModel.getRating());

    }

    public void ButtonClick(View view) {
        if (view.getId() == R.id.btnLocation) {
            Intent intent = new Intent(HallViewActivity.this, MapViewActivity.class);
            intent.putExtra("lat", hallModel.getLatitude());
            intent.putExtra("longitude", hallModel.getLongitude());
            intent.putExtra("name", hallModel.getName());
            startActivity(intent);
        }
        if (view.getId() == R.id.btnEdit) {
            Intent intent = new Intent(HallViewActivity.this, EditHallInfoActivity.class);
            intent.putExtra("model", hallModel);
            intent.putExtra("postKey", postKey);
            startActivity(intent);
        }
        if (view.getId() == R.id.btnBook) {
            Intent intent = new Intent(HallViewActivity.this, BookingActivity.class);
            intent.putExtra("model", hallModel);
            intent.putExtra("postKey", postKey);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getMenuData();
    }

    private void getMenuData() {
        final FirebaseRecyclerAdapter<MenuModel, MenuViewHolder2> mRecyclerAdapter = new FirebaseRecyclerAdapter<MenuModel, MenuViewHolder2>(
                MenuModel.class,
                R.layout.menu_single_item,
                MenuViewHolder2.class,
                mDatabase2
        ) {
            @Override
            protected void populateViewHolder(final MenuViewHolder2 viewHolder, final MenuModel model, final int i) {
                viewHolder.txtName.setText(model.getMenu_name());
                Picasso.with(HallViewActivity.this).load(model.getMenu_image()).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.menuImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(HallViewActivity.this).load(model.getMenu_image()).into(viewHolder.menuImage);
                    }
                });
            }
        };
        mHallListView.setAdapter(mRecyclerAdapter);
    }

    public static class MenuViewHolder2 extends RecyclerView.ViewHolder {
        View mView;
        TextView txtName;
        CircleImageView menuImage;
        LinearLayout mainLayout;

        public MenuViewHolder2(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            txtName = mView.findViewById(R.id.txtName);
            menuImage = mView.findViewById(R.id.menuImage);
            mainLayout = mView.findViewById(R.id.mainLayout);
        }
    }

}
