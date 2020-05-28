package com.example.subway_for_pregnant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
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

    private void pregnant_register() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final String email = user.getEmail();
        final String inputName = ((EditText) findViewById(R.id.pregnant_name)).getText().toString();
        final String inputCardNum = ((EditText) findViewById(R.id.pregnant_cardNum)).getText().toString();

        final Map<String, Object> updateUser = new HashMap<>();
        updateUser.put("name", inputName);
        updateUser.put("cardNum", inputCardNum);
        updateUser.put("isPregnant", true);
        /*
        updateUser.put("reserve_laneInfo", "");  //예약된 열차 노선
        updateUser.put("reserve_driveInfo", "");  //예약된 열차 방향
        updateUser.put("reserve_trainNum", "");  //예약된 열차 번호
        updateUser.put("reserve_carNum", "");  //예약된 열차 칸
        updateUser.put("reserve_start", "");  //예약된 출발역
        updateUser.put("reserve_end", "");  //예약된 도착역
        updateUser.put("reserve_seatNum", "");  //예약된 자리 번호
         */
        updateUser.put("reservation_info", "");
        updateUser.put("transfer_info", "");

        synchronized (this) {
            db.collection("pregnant_init").whereEqualTo("name", inputName).whereEqualTo("cardNum", inputCardNum)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d(TAG, String.valueOf(task.isSuccessful()));
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, String.valueOf(updateUser));
                                    Log.d(TAG, String.valueOf(email));
                                    db.collection("user").document(email).update(updateUser);
                                    Log.d(TAG, document.getData().get("name") + " + " + inputName);
                                    Log.d(TAG, document.getData().get("cardNum") + " + " + inputCardNum);
                                    myStartActivity(MainActivity.class);
                                    return;
                                }
                                startToast("이름 혹은 카드번호를 확인해주세요.");
                                myStartActivity(PregnantInitActivity.class);
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }

                        }
                    });
        }
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
