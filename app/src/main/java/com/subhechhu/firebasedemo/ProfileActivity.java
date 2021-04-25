package com.subhechhu.firebasedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}