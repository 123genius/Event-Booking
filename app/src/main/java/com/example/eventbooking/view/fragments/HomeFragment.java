package com.example.eventbooking.view.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventbooking.R;
import com.example.eventbooking.model.HallModel;
import com.example.eventbooking.view.activities.AddHallActivity;
import com.example.eventbooking.view.activities.HallViewActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class HomeFragment extends Fragment {

    private RecyclerView mHallListView;
    private DatabaseReference mDatabase, mUserDatabase;
    private String userType = "";
    private String mCurrentUserId;
    FloatingActionButton button;

    public HomeFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Halls");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mHallListView = view.findViewById(R.id.hallListView);
        LinearLayoutManager mManager = new LinearLayoutManager(container.getContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mHallListView.setLayoutManager(mManager);
        mHallListView.setHasFixedSize(true);

        button = view.findViewById(R.id.btnAdd);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).startActivity(new Intent(getContext(), AddHallActivity.class));
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getHallData();
    }

    private void getHallData() {
        mUserDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userType = Objects.requireNonNull(dataSnapshot.child("type").getValue()).toString();
                if (!userType.equals("admin")) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setVisibility(View.VISIBLE);
                }
                final FirebaseRecyclerAdapter<HallModel, HallViewHolder> mRecyclerAdapter = new FirebaseRecyclerAdapter<HallModel, HallViewHolder>(
                        HallModel.class,
                        R.layout.hall_single_item,
                        HallViewHolder.class,
                        mDatabase
                ) {
                    @Override
                    protected void populateViewHolder(final HallViewHolder hallViewHolder, final HallModel hallModel, final int i) {
                        if (userType.equals("admin")) {
                            if (hallModel.getUid().equals(mCurrentUserId)) {
                                hallViewHolder.setHallName(hallModel.getName());
                                hallViewHolder.setOwnerName(hallModel.getUser_name());
                                hallViewHolder.setStatus(hallModel.getStatus());
                                hallViewHolder.setHallPicture(hallModel.getImageUrl(), getContext());
                                hallViewHolder.setProfilePicture(hallModel.getUserImage(), getContext());
                                hallViewHolder.mainLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), HallViewActivity.class);
                                        intent.putExtra("hallData", hallModel);
                                        intent.putExtra("postKey", getRef(i).getKey());
                                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(Objects.requireNonNull(getActivity()), hallViewHolder.imageView, "example_transition");
                                        startActivity(intent, optionsCompat.toBundle());
                                    }
                                });
                            } else {
                                hallViewHolder.mainLayout.setVisibility(View.GONE);
                            }
                        } else {
                            hallViewHolder.setHallName(hallModel.getName());
                            hallViewHolder.setOwnerName(hallModel.getUser_name());
                            hallViewHolder.setStatus(hallModel.getStatus());
                            hallViewHolder.setHallPicture(hallModel.getImageUrl(), getContext());
                            hallViewHolder.setProfilePicture(hallModel.getUserImage(), getContext());
                            hallViewHolder.mainLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext(), HallViewActivity.class);
                                    intent.putExtra("hallData", hallModel);
                                    intent.putExtra("postKey", getRef(i).getKey());
                                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(Objects.requireNonNull(getActivity()), hallViewHolder.imageView, "example_transition");
                                    startActivity(intent, optionsCompat.toBundle());
                                }
                            });
                        }
                    }
                };
                mHallListView.setAdapter(mRecyclerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class HallViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView txtOwnerName, txtHallName, txtStatus;
        ImageView imageView;
        CircleImageView profile_image;
        CardView mainLayout;

        public HallViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mainLayout = mView.findViewById(R.id.mainLayout);
        }


        public void setHallPicture(final String image, final Context ctx) {
            imageView = mView.findViewById(R.id.hallImage);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).into(imageView);
                }
            });
        }

        public void setProfilePicture(final String image, final Context ctx) {
            profile_image = mView.findViewById(R.id.profileImage);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(profile_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).into(profile_image);
                }
            });
        }

        public void setOwnerName(String name) {
            txtOwnerName =  mView.findViewById(R.id.txtOwnerName);
            txtOwnerName.setText(name);
        }

        public void setHallName(String name) {
            txtHallName =  mView.findViewById(R.id.txtHallName);
            txtHallName.setText(name);
        }

        public void setStatus(String name) {
            txtStatus =  mView.findViewById(R.id.txtStatus);
            txtStatus.setText(name);
        }

    }

}
