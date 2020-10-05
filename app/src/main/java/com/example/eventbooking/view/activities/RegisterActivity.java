package com.example.eventbooking.view.activities;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventbooking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText txtEmail, txtPassword, txtConfirm, txtName;
    private FrameLayout mainContent;
    private String email, name, password, confirm;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabase;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        type = getIntent().getStringExtra("type");
        mProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        txtEmail = findViewById(R.id.txtEmail);
        txtName = findViewById(R.id.txtName);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirm = findViewById(R.id.txtConfirmPassword);
        mainContent = findViewById(R.id.mainContent);

    }

    public void ButtonClick(View view) {

        email = txtEmail.getText().toString();
        password = txtPassword.getText().toString();
        confirm = txtConfirm.getText().toString();
        name = txtName.getText().toString();

        if (TextUtils.isEmpty(name)) {
            txtName.setError("Name can't be empty");
        } else if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Enter valid email");
        } else if (TextUtils.isEmpty(password)) {
            txtPassword.setError("Enter Password");
        } else if (TextUtils.isEmpty(confirm)) {
            txtConfirm.setError("Enter Password");
        } else if(!password.equals(confirm)) {
            txtConfirm.setError("Passwords are not matching");
            txtConfirm.setError("Passwords are not matching");
        } else {
            registerUser(name, email, password, confirm);
        }
    }

    private void registerUser(final String name, final String email, String password, String confirm) {
        mProgress.setMessage("Registering Your Account");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                setData(user, name, email, type);
                            }
                        } else {
                            mProgress.dismiss();
                            Snackbar.make(mainContent, "Authentication Failed!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
                        }
                    }
                });
    }

    private void setData(FirebaseUser user, String name, String email, String type) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("name", name);
        data.put("email", email);
        data.put("type", type);
        mDatabase.child(user.getUid()).setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                RegisterActivity.this.finish();
                mProgress.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgress.dismiss();
                Snackbar.make(mainContent, "Authentication Failed!", Snackbar.LENGTH_LONG).setActionTextColor(getResources().getColor(android.R.color.holo_red_dark )).show();
            }
        });
    }
}
