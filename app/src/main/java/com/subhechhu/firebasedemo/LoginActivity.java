package com.subhechhu.firebasedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    ProgressBar progressBarLogin;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailauth);

        editTextEmail = findViewById(R.id.editTextTextPersonEmail);
        editTextPassword = findViewById(R.id.editTextTextPassword);

        progressBarLogin = findViewById(R.id.progressBarLogin);

        firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());

                if (email.isEmpty()) {
                    editTextEmail.setError("Email cannot be empty");
                    editTextEmail.requestFocus();
                    return; // do not execute code below if email is empty
                } else {
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // []@[].[]
                        editTextEmail.setError("Invalid email");
                        editTextEmail.requestFocus();
                        return; // do not execute code below if email is invalid
                    }
                }

                if (password.length() < 6) {
                    editTextPassword.setError("Password should be more than 6 characters");
                    editTextPassword.requestFocus();
                    return; // do not execute code below if password is short
                }

                progressBarLogin.setVisibility(View.VISIBLE); // If input params are fine, show progress bar

                firebaseAuth.signInWithEmailAndPassword(email, password) // login to firebase with provided email and password
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBarLogin.setVisibility(View.INVISIBLE); // hide progressbar on complete
                                if (task.isSuccessful()) {
                                    DatabaseReference ref = FirebaseDatabase.getInstance()
                                            .getReference()
                                            .child(FirebaseAuth.getInstance().getUid()); // get the saved data of the user from the FirebaseDB based on the stored UID
                                    Log.e("TAGGED","reference: "+ref.getKey());

                                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class)); // go to another activity
                                    finish(); // kill the class for back button handling.
                                } else {
                                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBarLogin.setVisibility(View.INVISIBLE);
                                e.printStackTrace();
                            }
                        })
                ;
            }
        });

    }

    public void signup(View view) {
        startActivity(new Intent(this, SignupActivity.class));
    }
}