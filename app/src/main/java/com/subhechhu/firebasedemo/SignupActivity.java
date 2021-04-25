package com.subhechhu.firebasedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    EditText editTextTextPersonName, editTextTextPersonAge, editTextTextPersonEmail, editTextTextPassword;
    Button buttonSignup;
    ProgressBar progressBarSignup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextTextPassword = findViewById(R.id.editTextTextPassword);
        editTextTextPersonAge = findViewById(R.id.editTextTextPersonAge);
        editTextTextPersonEmail = findViewById(R.id.editTextTextPersonEmail);
        editTextTextPersonName = findViewById(R.id.editTextTextPersonName);

        progressBarSignup = findViewById(R.id.progressBarSignup);

        buttonSignup = findViewById(R.id.button_signup);

        mAuth = FirebaseAuth.getInstance();

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = String.valueOf(editTextTextPersonName.getText());
                String age = String.valueOf(editTextTextPersonAge.getText());
                String email = String.valueOf(editTextTextPersonEmail.getText());
                String password = String.valueOf(editTextTextPassword.getText());

                if (name.isEmpty()) {
                    editTextTextPersonName.setError("Name is required");
                    editTextTextPersonName.requestFocus();
                    return;  // do not execute code below if name is empty
                }

                if (age.isEmpty()) {
                    editTextTextPersonAge.setError("Age is required");
                    editTextTextPersonAge.requestFocus();
                    return;  // do not execute code below if age is empty
                }

                if (email.isEmpty()) {
                    editTextTextPersonEmail.setError("Email is required");
                    editTextTextPersonEmail.requestFocus();
                    return;  // do not execute code below if email is empty
                } else {
                    // []@[].[]
                    if (!Patterns.EMAIL_ADDRESS.matcher(editTextTextPersonEmail.getText().toString()).matches()) {
                        editTextTextPersonEmail.setError("Invalid email");
                        editTextTextPersonEmail.requestFocus();
                        return;  // do not execute code below if email is invalid
                    }
                }

                if (password.length() < 6) {
                    editTextTextPassword.setError("Password should be more than 6 characters");
                    editTextTextPassword.requestFocus();
                    return;  // do not execute code below if password is short
                }

                progressBarSignup.setVisibility(View.VISIBLE);  // If input params are fine, show progress bar

                mAuth.createUserWithEmailAndPassword(email, password) // Create a new account with given email and password
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) { // If creating new account is successful, store the account details in firebase DB
                                    User user = new User(); // Create new user object and add fileds;
                                    user.setName(name);
                                    user.setAge(age);
                                    user.setEmail(email);

                                    FirebaseDatabase.getInstance().getReference("ViaEmail") // Create another new table ViaEmail
                                            .child(FirebaseAuth.getInstance().getUid()) // root node for every entry will be UID of the user
                                            .setValue(user) // add the value to the same UID
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    progressBarSignup.setVisibility(View.INVISIBLE); // when stored (or failed) hide the progressbar
                                                    if (task.isSuccessful()) {
                                                        clearFields(); // custom method to clear all the edittext field
                                                        Toast.makeText(SignupActivity.this, "User has been added successfully. Proceed to login", Toast.LENGTH_SHORT).show();
                                                        finish(); // close the current activity
                                                    } else {
                                                        Toast.makeText(SignupActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    clearFields();
                                    progressBarSignup.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SignupActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                            }
                        });
            }

        });
    }

    void clearFields() {
        editTextTextPersonName.setText("");
        editTextTextPassword.setText("");
        editTextTextPersonEmail.setText("");
        editTextTextPersonAge.setText("");
    }
}