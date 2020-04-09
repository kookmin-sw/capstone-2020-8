package com.example.subway_for_pregnant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class PregnantInitActivity extends AppCompatActivity {
    private static final String TAG = "myTag";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregnant_init);

        findViewById(R.id.pregnantRegisterButton).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pregnantRegisterButton:
                    pregnant_register();
                    break;
            }
        }
    };

    private void pregnant_register(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final String email = user.getEmail();
        final String name = ((EditText)findViewById(R.id.pregnant_name)).getText().toString();
        final String cardNum = ((EditText)findViewById(R.id.pregnant_cardNum)).getText().toString();

        final Map<String, Object> updateUser = new HashMap<>();
        updateUser.put("name", name);
        updateUser.put("cardNum", cardNum);
        updateUser.put("isPregnant", true);

        CollectionReference initRef = db.collection("pregnant_init");

        Query query = initRef.whereEqualTo("name", name).whereEqualTo("cardNum", cardNum);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            db.collection("user").document(email)
                                    .update(updateUser);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        // setContentView(R.layout.activity_main2);
        myStartActivity(MainActivity.class);
    }

    private void myStartActivity(Class c){
        Intent intent=new Intent(this,c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
