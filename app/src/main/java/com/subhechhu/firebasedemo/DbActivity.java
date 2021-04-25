package com.subhechhu.firebasedemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DbActivity extends AppCompatActivity {

    /*
     * Objective of this Activity is to store information of different users
     * Activity consist of text fields.
     * When user press the save button, current details will be saved on firebase DB
     *
     * User's name will be the root.
     * Other details will be the child object of the name
     *
     * Name
     *   Phone
     *   Address
     * */

    TextInputEditText textInputEditText_name, textInputEditText_phone, textinputEditText_address;

    private DatabaseReference mFirebaseDatabase;
    FirebaseDatabase mFirebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        textInputEditText_name = findViewById(R.id.textinput_name);
        textInputEditText_phone = findViewById(R.id.textinput_phone);
        textinputEditText_address = findViewById(R.id.textinput_address);

        mFirebaseInstance = FirebaseDatabase.getInstance(); // instance of the Firebase
        mFirebaseDatabase = mFirebaseInstance.getReference("ViaForm"); // Acts like Table
    }

    public void saveData(View view) {
        String name = String.valueOf(textInputEditText_name.getText());
        String number = String.valueOf(textInputEditText_phone.getText());
        String address = String.valueOf(textinputEditText_address.getText());

        if (name.isEmpty() && number.isEmpty() && address.isEmpty()) { // check if fields are empty
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
        } else { // If fields are not empty

            // Create root node inside the "ViaForm" section as "name" given by the user
            DatabaseReference myRef = mFirebaseDatabase.child(name);

            // Create a child "Phone" under the "name" of user & set the value provided
            DatabaseReference myRefPhone = myRef.child("Phone");
            myRefPhone.setValue(number);

            // Create a child "Address" under the "name" of user & set the value provided
            DatabaseReference myRefAddress = myRef.child("Address");
            myRefAddress.setValue(address);


            // Clear all the fields after storing
            textInputEditText_phone.setText("");
            textInputEditText_name.setText("");
            textinputEditText_address.setText("");
        }
    }


    // View the stored Data
    public void showData(View v) {
        //Get the table "ViaForm" from the firebase
        DatabaseReference ref = mFirebaseInstance.getReference().child("ViaForm");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ss : snapshot.getChildren()) { // loops on getchildren() and get one single object
                    Log.e("TAGGED", "key: " + ss.getKey()); // gives the name
                    Log.e("TAGGED", "child count: " + ss.getChildrenCount()); // name will have 2 objects as children [Address & phone]
                    for (DataSnapshot css : ss.getChildren()) { // Loop on the object to get the value of the children of the name root
                        Log.e("TAGGED", "child key: " + css.getKey()); // first loop will give first key [either phone or address]
                        Log.e("TAGGED", "value: " + css.getValue().toString()); // get the respective value
                    }
                    Log.e("TAGGED", "---------end of object---------");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}