package com.example.eventbooking.view.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventbooking.R;
import com.example.eventbooking.model.RequestModel;
import com.example.eventbooking.view.activities.BookingActivity;
import com.example.eventbooking.view.activities.RequestViewActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

public class RequestFragment extends Fragment {

    private View mView;
    private RecyclerView requestList;
    private FirebaseAuth mAuth;
    private DatabaseReference mRequestDatabase, mUserDatabase, mUserListDatabase;
    private String mCurrentUserId;
    private String userType = "";
    private String postTitle, image = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_request, container, false);

        requestList = mView.findViewById(R.id.request_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("booking_request").child(mCurrentUserId);
        mUserListDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestDatabase.keepSynced(true);
        mUserListDatabase.keepSynced(true);
        mUserDatabase.keepSynced(true);

        requestList.setHasFixedSize(true);
        requestList.setLayoutManager(new LinearLayoutManager(container.getContext()));

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getRequestData();
    }

    private void getRequestData() {
        FirebaseRecyclerAdapter<RequestModel, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<RequestModel, RequestViewHolder>(
                RequestModel.class,
                R.layout.request_single_item,
                RequestViewHolder.class,
                mRequestDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder requestViewHolder, final RequestModel requestModel, int i) {
                final String postKey = getRef(i).getKey();

                String dateTime = requestModel.getDate() + " at " + requestModel.getTime();
                requestViewHolder.txtDateTime.setText(dateTime);

                mUserDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userType = (String) dataSnapshot.child("type").getValue();
                        mUserDatabase.child(Objects.requireNonNull(postKey)).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String name = (String) dataSnapshot.child("name").getValue();
                                if (Objects.requireNonNull(userType).equals("admin")){
                                    postTitle = "<b>" + requestModel.getCustomer_name() + "</b>" + " sent you a request for booking of " + "<b>" + requestModel.getHall_name() + "</b>" + " on";
                                } else {
                                    postTitle = "You sent a request to " + "<b>" + name + "</b>" + " for booking of " + "<b>" + requestModel.getHall_name() + "</b>" + " on";
                                }
                                requestViewHolder.txtName.setText(Html.fromHtml(postTitle));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mUserListDatabase.child(Objects.requireNonNull(postKey)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        image = (String) dataSnapshot.child("image").getValue();
                        Picasso.with(getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(requestViewHolder.profileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(getContext()).load(image).into(requestViewHolder.profileImage);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                requestViewHolder.mainContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), RequestViewActivity.class);
                        intent.putExtra("requestModel", requestModel);
                        intent.putExtra("userId", postKey);
                        intent.putExtra("image", image);
                        startActivity(intent);
                    }
                });

            }
        };
        requestList.setAdapter(adapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircleImageView profileImage;
        TextView txtName, txtDateTime;
        CardView mainContent;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            profileImage = mView.findViewById(R.id.userImage);
            txtName = mView.findViewById(R.id.txtTitle);
            txtDateTime = mView.findViewById(R.id.txtDateTime);
            mainContent = mView.findViewById(R.id.mainContent);
        }
    }
}
