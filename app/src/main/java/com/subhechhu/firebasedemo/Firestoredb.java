package com.subhechhu.firebasedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Firestoredb extends AppCompatActivity {

    FirebaseFirestore db;
    TextInputEditText textInputEditText_name, textInputEditText_phone, textinputEditText_address;

    ProgressBar progressBar_db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestoredb);

        progressBar_db = findViewById(R.id.progressBar_db);

        textInputEditText_name = findViewById(R.id.textinput_name);
        textInputEditText_phone = findViewById(R.id.textinput_phone);
        textinputEditText_address = findViewById(R.id.textinput_address);

        db = FirebaseFirestore.getInstance();


    }

    public void saveData(View view) {
        String name = String.valueOf(textInputEditText_name.getText());
        String number = String.valueOf(textInputEditText_phone.getText());
        String address = String.valueOf(textinputEditText_address.getText());

        if (name.isEmpty() && number.isEmpty() && address.isEmpty()) { // check if fields are empty
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
        } else { // If fields are not empty

            progressBar_db.setVisibility(View.VISIBLE);

            User user = new User();
            user.setName(name);
            user.setAge(number);
            user.setEmail(address);

            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(Firestoredb.this, name + " added to DB.\nId: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                            // Clear all the fields after storing

                            progressBar_db.setVisibility(View.INVISIBLE);

                            textInputEditText_phone.setText("");
                            textInputEditText_name.setText("");
                            textinputEditText_address.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressBar_db.setVisibility(View.INVISIBLE);

                            Log.w("TAGGED", "Error adding document", e);
                        }
                    });
        }
    }

    // View the stored Data
    public void showData(View v) {
        progressBar_db.setVisibility(View.VISIBLE);
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            progressBar_db.setVisibility(View.INVISIBLE);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAGGED", document.getId() + " => " + document.getData());
                            }
                        } else {
                            progressBar_db.setVisibility(View.INVISIBLE);
                            Log.w("TAGGED", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}