package com.example.eventbooking.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.eventbooking.R;
import com.example.eventbooking.model.HallModel;
import com.example.eventbooking.model.MenuModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class BookingActivity extends AppCompatActivity {

    private HallModel hallModel;
    private String postKey, time;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabase, mDatabase2, mRequestDatabase, mMenuDatabase;

    private CircleImageView hallImage;
    private TextView txtHallName, txtDate, txtTime, txtTotalBill;
    private EditText txtName, txtContact;

    private DatePicker datePicker;
    private Calendar calendar;
    private int year, month, day;
    String date;
    private List<String> numberOfGuests;
    private int totalCapacity;

    private RecyclerView mHallListView;
    private LinearLayoutManager mManager;
    private List<Double> menuPriceList;
    private int totalGuest;
    private LinearLayout mainContent;
    private StringBuilder menuNames;
    private double sumOfMenuPrice = 0;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        menuPriceList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        menuNames = new StringBuilder(100);
        builder = new AlertDialog.Builder(this);

        hallModel = (HallModel) getIntent().getSerializableExtra("model");
        postKey = getIntent().getStringExtra("postKey");

        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);

        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("booking_request");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Menus");
        mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Halls").child(postKey);

        mMenuDatabase = FirebaseDatabase.getInstance().getReference().child("Menus").child(postKey);
        mMenuDatabase.keepSynced(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mainContent = findViewById(R.id.mainContent);

        txtContact = findViewById(R.id.txtContactNumber);
        txtName = findViewById(R.id.txtCustomerName);

        hallImage = findViewById(R.id.hallImage);
        txtHallName = findViewById(R.id.txtHallName);
        txtDate = findViewById(R.id.txtBookingDate);
        txtTime = findViewById(R.id.txtBookingTime);
        txtTotalBill = findViewById(R.id.txtTotalBill);

        txtHallName.setText(hallModel.getName());
        Picasso.with(this).load(hallModel.getImageUrl()).networkPolicy(NetworkPolicy.OFFLINE).into(hallImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(BookingActivity.this).load(hallModel.getImageUrl()).into(hallImage);
            }
        });

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        date = day + "/" + month + "/" + year;
        txtDate.setText(date);

        totalCapacity = Integer.parseInt(hallModel.getMaximum_capacity());

        numberOfGuests = new ArrayList<>();
        for (int i=0; i<=totalCapacity; i++) {
            if (i==0) {
                numberOfGuests.add(getResources().getString(R.string.number_of_guest));
            } else {
                numberOfGuests.add(String.valueOf(i));
            }
        }

        Spinner guestNumberList = findViewById(R.id.guestNumberList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, numberOfGuests){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        guestNumberList.setAdapter(adapter);

        guestNumberList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                if(position > 0){
                    if (menuPriceList.size() > 0) {
                        totalGuest = Integer.parseInt(selectedItemText);
                        calculateTotalBill(totalGuest);
                    } else {
                        Toast.makeText(BookingActivity.this, "Please Select Menu First!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mHallListView = findViewById(R.id.menuList);
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mHallListView.setLayoutManager(mManager);

    }

    private void calculateTotalBill(int totalGuest) {
        double totalBill = 0;
        for (int i=0; i<menuPriceList.size(); i++) {
            sumOfMenuPrice = sumOfMenuPrice + menuPriceList.get(i);
        }
        totalBill = (sumOfMenuPrice * totalGuest)  + Integer.parseInt(hallModel.getRent()) ;
        txtTotalBill.setText(String.valueOf(totalBill));
    }

    public void ButtonClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                super.onBackPressed();
                break;
            case R.id.btnSubmit:
                submitRequest();
                break;
            case R.id.btnDate:
                selectDate();
                break;
            case R.id.btnTime:
                selectTime();
                break;
                default:
                    break;
        }
    }

    private void submitRequest() {
        String name = txtName.getText().toString();
        String contact = txtContact.getText().toString();

        if (TextUtils.isEmpty(name)) {
            txtName.setError("Name required");
        } else if (TextUtils.isEmpty(contact)) {
            txtContact.setError("Contact Number required");
        } else if (TextUtils.isEmpty(date)) {
            Snackbar.make(mainContent, "Select Date First!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
        } else if (TextUtils.isEmpty(time)) {
            Snackbar.make(mainContent, "Select Time First!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
        } else if (menuPriceList.size() == 0) {
            Snackbar.make(mainContent, "Select Menu First!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
        } else if (totalGuest == 0) {
            Snackbar.make(mainContent, "Select Number of Guest First!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
        } else {
            mProgress.setMessage("Your request is being processed!");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
            final Map<String, java.io.Serializable> data = new HashMap<String, java.io.Serializable>();
            data.put("customer_name", name);
            data.put("customer_number", contact);
            data.put("date", date);
            data.put("time", time);
            data.put("hall_name", hallModel.getName());
            data.put("guest", String.valueOf(totalGuest));
            data.put("total_bill", txtTotalBill.getText().toString());
            data.put("hall_id", postKey);
            data.put("menus", String.valueOf(menuNames));
            data.put("menu_bill", String.valueOf(sumOfMenuPrice));

            mRequestDatabase.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child(hallModel.getUid()).setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mRequestDatabase.child(hallModel.getUid()).child(mAuth.getCurrentUser().getUid()).setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mProgress.dismiss();
                            builder.setMessage("Congratulations!\nYour request has been submitted to the Manager. The management will contact you in the working days.\nThanks For Choosing us\nThe management will accept your request once you pay the bill in advance.\nPayment method is ByHand.").setCancelable(false).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    Intent intent = new Intent(BookingActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    BookingActivity.super.onBackPressed();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.setTitle("Request Sent");
                            alert.show();
                        }
                    });
                }
            });
        }

    }

    private void selectTime() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @SuppressLint("SimpleDateFormat")
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Time tme = new Time(hourOfDay, minute,0);//seconds by default set to zero
                    Format formatter;
                    formatter = new SimpleDateFormat("h:mm a");
                    time = formatter.format(tme);
                    txtTime.setText(time);
                }
            }, 0, 0, false);
            dialog.show();
        }
    }

    private void selectDate() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            DatePickerDialog dialog = new DatePickerDialog(this);
            dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    date = dayOfMonth + "/" + month + "/" + year;
                    txtDate.setText(date);
                }
            });
            dialog.show();
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
                R.layout.add_menu_single_item,
                MenuViewHolder2.class,
                mMenuDatabase
        ) {
            @Override
            protected void populateViewHolder(final MenuViewHolder2 viewHolder, final MenuModel model, final int i) {
                viewHolder.txtName.setText(model.getMenu_name());
                viewHolder.itemCheckbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final boolean isChecked = viewHolder.itemCheckbox.isChecked();
                        if (isChecked) {
                            menuNames.append(model.getMenu_name()).append(",");
                            addToList(model.getMenu_price());
                        } else {
                            removeFromList(model.getMenu_price());
                        }
                    }
                });
                Picasso.with(BookingActivity.this).load(model.getMenu_image()).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.menuImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(BookingActivity.this).load(model.getMenu_image()).into(viewHolder.menuImage);
                    }
                });
            }
        };
        mHallListView.setAdapter(mRecyclerAdapter);
    }

    private void removeFromList(String menu_price) {
        double price = Double.parseDouble(menu_price);
        menuPriceList.remove(price);
    }

    private void addToList(String menu_price) {
        double price = Double.parseDouble(menu_price);
        menuPriceList.add(price);
    }

    public static class MenuViewHolder2 extends RecyclerView.ViewHolder {
        View mView;
        TextView txtName;
        CircleImageView menuImage;
        FrameLayout mainLayout;
        CheckBox itemCheckbox;

        public MenuViewHolder2(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            txtName = mView.findViewById(R.id.txtName);
            menuImage = mView.findViewById(R.id.menuImage);
            mainLayout = mView.findViewById(R.id.mainLayout);
            itemCheckbox = mView.findViewById(R.id.itemCheckBox);
        }
    }
}
