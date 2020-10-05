package com.example.eventbooking.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.eventbooking.R;

import java.util.Objects;

public class AuthenticationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

    }

    public void ButtonClick(View view) {
        switch (view.getId()) {
            case R.id.btnUser:
                Intent intent1 = new Intent(AuthenticationActivity.this, LoginActivity.class);
                intent1.putExtra("type", "user");
                startActivity(intent1);
                break;
            case R.id.btnAdmin:
                Intent intent2 = new Intent(AuthenticationActivity.this, LoginActivity.class);
                intent2.putExtra("type", "admin");
                startActivity(intent2);
                break;
                default:
                    break;
        }
    }
}
