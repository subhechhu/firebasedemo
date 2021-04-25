package com.subhechhu.firebasedemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    // https://firebase.google.com/docs/cloud-messaging/android/client ==> Set up cloud messaging from scratch
    // https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages ==> Firebase Cloud Messaging structure of notification payload

    // https://firebase.google.com/docs/database/android/start ==> Firebase DB

    // https://firebase.google.com/docs/auth/android/google-signin ==> Every code of Firebase Authentication with Google
    // https://developers.google.com/android/guides/client-auth ==> Enable SHA1

    // https://firebase.google.com/docs/firestore/quickstart ==> Firestore DB

    GoogleSignInClient googleSignInClient; // A client for interacting with the Google Sign In API
    private FirebaseAuth mAuth;
    SignInButton signInButton;

    ProgressBar progressBar_mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton = findViewById(R.id.sign_in_button); // google signin button
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        progressBar_mainActivity = findViewById(R.id.progressBar_mainActivity); // progress bar to show loading status


        mAuth = FirebaseAuth.getInstance(); // gives instance of firebase authentication.

        // FIREBASE CLOUD MESSAGING I.E. FOR NOTIFICATION
        // when application is on background, we get the title and body in the notification via intent
        if (getIntent() != null && getIntent().getExtras() != null) {

            // we need to pass "title" & "body" as keys in Custom data section
            // (#5) Additional options (optional) in  Compose notification
            // in firebase console

            String customString = String.valueOf(getIntent().getExtras().get("title"));
            String customInteger = String.valueOf(getIntent().getExtras().get("body"));

            Log.e("TAGGED", "custom string: " + customString);
            Log.e("TAGGED", "custom int: " + customInteger);
        }


        // Google Sign in process beings from following line
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar_mainActivity.setVisibility(View.VISIBLE); // progress bar is visible when you click signin with google button
                authGmail(); // custom method
            }
        });

        GoogleSigninRequest(); // custom method created for google sign in configuration

    }

    private void GoogleSigninRequest() {

        //GoogleSignIn contains options used to configure the Api entry point for Google Sign In.
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) // Change to DEFAULT_GAMES_SIGN_IN for google games account
                .requestIdToken(getString(R.string.default_web_client_id)) // Get the value from "google-services.json"
                .requestEmail() // Specifies that email info is requested by your application.
                .build(); // Builds the GoogleSignInOptions object.

        // googleSignInClient is a client for interacting with the Google Sign In API
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions); //GoogleSignIn is an entry point for the Google Sign In API.   //getClient creates a new instance of GoogleSignInClient
    }

    // Custom method
    public void firebasedb(View view) {
        startActivity(new Intent(this, DbActivity.class));
    }

    // Custom method
    public void authEmail(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    // Custom method
    public void authGmail() {
        Intent intent = googleSignInClient.getSignInIntent(); // Gets an Intent to start the Google Sign In flow. Returns the Intent used for start the sign-in flow.
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 1) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class); // Get google account
            if (account != null) {
                Log.e("TAGGED", "email: " + account.getEmail()); // get email from logged in account
                Log.e("TAGGED", "name: " + account.getDisplayName());  // get name from logged in account

                Toast.makeText(this, "logged in user: " + account.getEmail(), Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account.getIdToken()); // proceed for firebase authentication with the credentials

            } else {
                progressBar_mainActivity.setVisibility(View.INVISIBLE);
            }
        } catch (ApiException e) {
            progressBar_mainActivity.setVisibility(View.INVISIBLE);
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAGGED", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null); // get credentials with the ID obtained from google sign in
        mAuth.signInWithCredential(credential) // sign in with credentials
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser(); // getting user from current firebase authentication
                            if (user != null) {
                                String name = user.getDisplayName(); // name from google's current logged account
                                String email = user.getEmail();// email from google's current logged in account

                                User fbUser = new User(); // create local User object to save the login credentials on Firebase DB
                                fbUser.setEmail(email);
                                fbUser.setName(name);
                                fbUser.setAge("");

                                FirebaseDatabase.getInstance().getReference("ViaGmail") // create a new reference "ViaGmail". Acts like a independent folder
                                        .child(mAuth.getUid()) // Firebase ID will be the root for a user.
                                        .setValue(fbUser) // Add the local user object created in #157
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressBar_mainActivity.setVisibility(View.INVISIBLE);
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(MainActivity.this, "Google Login Successful", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(MainActivity.this, ProfileActivity.class)); // Go to profile activity if firebase db operation was successful
                                                    finish();
                                                } else {
                                                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            progressBar_mainActivity.setVisibility(View.INVISIBLE);
                            Log.w("TAGGED", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }


    public void firestoreDB(View view) {
        startActivity(new Intent(this, Firestoredb.class));
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // check if the user is logged in to Firebase.
        if (currentUser != null) { // if yes, current user cannot be null. hence, go to profile screen directly.
            Toast.makeText(this, "logged in user: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            finish(); // close the current activity while going to Profile activity so that back press from Profile Activity will close the app rather coming to MainActivity.
        }
    }
}