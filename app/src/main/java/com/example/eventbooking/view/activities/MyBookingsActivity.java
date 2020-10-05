package com.example.eventbooking.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventbooking.R;
import com.example.eventbooking.model.BookingsModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyBookingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mBookingDatabase, mDatabase, mHallDatabase;
    private String mCurrentUserId;

    private RecyclerView mBookingsList;

    private String userType;
    private String name, date, review, rating, time, guest, menus, totalBill, hallName, phone, paymetStatus, evetnStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mBookingDatabase = FirebaseDatabase.getInstance().getReference().child("Bookings");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId);
        mHallDatabase = FirebaseDatabase.getInstance().getReference().child("Halls");
        mBookingDatabase.keepSynced(true);
        mDatabase.keepSynced(true);

        mBookingsList = findViewById(R.id.bookingsList);
        mBookingsList.setLayoutManager(new LinearLayoutManager(this));

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userType = (String) dataSnapshot.child("type").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<BookingsModel, BookingViewHolder> adapter = new FirebaseRecyclerAdapter<BookingsModel, BookingViewHolder>(
                BookingsModel.class,
                R.layout.my_booking_single_item,
                BookingViewHolder.class,
                mBookingDatabase
        ) {
            @Override
            protected void populateViewHolder(final BookingViewHolder bookingViewHolder, final BookingsModel bookingsModel, int i) {

                if (bookingsModel.getAdmin_id().equals(mCurrentUserId) || bookingsModel.getUser_id().equals(mCurrentUserId)) {
                    final String postKey = getRef(i).getKey();

                    date = "<b>" + "Booking Date: " + "</b>" + bookingsModel.getEvent_date();
                    time = "<b>" + "Booking Time: " + "</b>" + bookingsModel.getEvent_time();
                    guest = "<b>" + "Total Guests: " + "</b>" + bookingsModel.getTotal_guest();
                    menus = "<b>" + "Menu: " + "</b>" + bookingsModel.getMenus().replace(",", " ");
                    totalBill = "<b>" + "Total Bill: " + "</b>" + bookingsModel.getTotal_bill();
                    hallName = bookingsModel.getHall_name();
                    phone = "<b>" + "Phone No: " + "</b>" + bookingsModel.getCustomer_number();
                    evetnStatus = "<b>" + "Event Status: " + "</b>" + bookingsModel.getEvent_status();
                    paymetStatus = "<b>" + "Payment Status: " + "</b>" + bookingsModel.getPayment_status();
                    review = "<b>" + "Review: " + "</b>" + bookingsModel.getReview();
                    rating = "<b>" + "Rating: " + "</b>" + bookingsModel.getRating();

                    bookingViewHolder.txtName.setText(Html.fromHtml("<b>" + "Name: " + "</b>" + bookingsModel.getCustomer_name()));
                    bookingViewHolder.txtContactNumber.setText(Html.fromHtml(phone));
                    bookingViewHolder.txtHallName.setText(hallName);
                    bookingViewHolder.txtDate.setText(Html.fromHtml(date));
                    bookingViewHolder.txtTime.setText(Html.fromHtml(time));
                    bookingViewHolder.txtGuest.setText(Html.fromHtml(guest));
                    bookingViewHolder.txtMenus.setText(Html.fromHtml(menus));
                    bookingViewHolder.txtBill.setText(Html.fromHtml(totalBill));
                    bookingViewHolder.txtEventStatus.setText(Html.fromHtml(evetnStatus));
                    bookingViewHolder.txtPaymentStatus.setText(Html.fromHtml(paymetStatus));

                    bookingViewHolder.txtReadReview.setText(Html.fromHtml(review));
                    bookingViewHolder.txtRating.setText(Html.fromHtml(rating));

                    if (userType.equals("admin")) {
                        bookingViewHolder.layoutOne.setVisibility(View.VISIBLE);
                    } else {
                        bookingViewHolder.layoutOne.setVisibility(View.GONE);
                    }

                    if (bookingsModel.getEvent_status().equals("Done")){
                        bookingViewHolder.btnEventStatus.setVisibility(View.GONE);
                    }

                    bookingViewHolder.btnEventStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Map<String, Object> data = new HashMap<>();
                            data.put("event_status", "Done");
                            mBookingDatabase.child(Objects.requireNonNull(postKey)).updateChildren(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Map<String, Object> data2 = new HashMap<>();
                                        data2.put("status", "Available");
                                        mHallDatabase.child(bookingsModel.getHall_id()).updateChildren(data2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(MyBookingsActivity.this, "Status Updated Successfully!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MyBookingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    bookingViewHolder.mainLayout.setVisibility(View.GONE);
                }

            }
        };
        mBookingsList.setAdapter(adapter);
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtContactNumber, txtReadReview, txtRating, txtHallName, txtDate, txtTime, txtGuest, txtMenus, txtBill, txtPaymentStatus, txtEventStatus;
        Button btnEventStatus;
        View mView;
        CardView mainLayout;
        LinearLayout layoutOne, reviewSection;
        RatingBar ratingBar;
        EditText txtReview;
        ImageButton btnSubmit;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            txtName = mView.findViewById(R.id.txtUsername);
            txtContactNumber = mView.findViewById(R.id.txtContactNumber);
            txtHallName = mView.findViewById(R.id.txtHallName);
            txtDate = mView.findViewById(R.id.txtEventDate);
            txtTime = mView.findViewById(R.id.txtEventTime);
            txtGuest = mView.findViewById(R.id.txtGuest);
            txtMenus = mView.findViewById(R.id.txtMenus);
            txtBill = mView.findViewById(R.id.txtTotalBill);
            txtPaymentStatus = mView.findViewById(R.id.txtPaymentStatus);
            txtEventStatus = mView.findViewById(R.id.txtEventStatus);
            layoutOne = mView.findViewById(R.id.layoutOne);
            mainLayout = mView.findViewById(R.id.mainLayout);

            txtReadReview = mView.findViewById(R.id.txtReadReview);
            txtRating = mView.findViewById(R.id.txtRating);

            btnEventStatus = mView.findViewById(R.id.btnEventStatus);

        }
    }
}
