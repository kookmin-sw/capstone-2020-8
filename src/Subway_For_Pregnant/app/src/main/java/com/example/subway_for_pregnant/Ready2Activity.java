package com.example.subway_for_pregnant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class Ready2Activity extends AppCompatActivity {

    private static final String TAG = "Ready2Activity";
    String globalStartName;     //출발역
    String globalEndName;       //도착역
    String userID;

    String laneInfo;
    String driveInfo;
    String trainNum;
    String carNum;
    String sectionStartGlobal;
    String sectionEndGlobal;
    String seatNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);
        findViewById(R.id.button_sit);

        Intent intent = getIntent();

        userID = intent.getExtras().getString("user");

        Log.d(TAG, userID);

        globalStartName = intent.getExtras().getString("globalStartName");
        globalEndName = intent.getExtras().getString("globalEndName");

        findCurrent();

        findViewById(R.id.button_sit).setOnClickListener(onClickListener);
        findViewById(R.id.button_cancel).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_sit:
                    doCancel();
                    break;
                case R.id.button_cancel:
                    doCancel();
                    break;
                default:
                    break;
            }
        }
    };

    private void findCurrent() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String userIDDB = userID;

        db.collection("user").document(userIDDB)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final DocumentSnapshot document1 = task.getResult();

                            String getReservation = null;

                            int j = 0;
                            while (getReservation == null) {
                                getReservation = (String) document1.getData().get("reservation_info");
                                Log.d(TAG, "" + j);
                                j += 1;
                            }

                            String reserve[] = getReservation.split(";");
                            for (int i = 0; i < reserve.length; i++) {
                                Log.d(TAG, reserve[i]);
                            }

                            laneInfo = reserve[0];
                            driveInfo = reserve[1];
                            trainNum = reserve[2];
                            carNum = reserve[3];
                            sectionStartGlobal = reserve[4];
                            sectionEndGlobal = reserve[5];
                            seatNum = reserve[6];

                            String seatPosition[] = {"왼쪽 자리", "오른쪽 자리"};

                            TextView tv_current = findViewById(R.id.textView_current);
                            tv_current.setText("출발역: " + globalStartName + "\n도착역: " + globalEndName + "\n" + carNum + "번 칸의 " + seatPosition[Integer.parseInt(seatNum) - 1]);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void doCancel() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> data2 = new HashMap<>();

        final String userIDDB = userID;

        final String laneInfoDB = laneInfo;
        final String driveInfoDB = driveInfo;
        final String trainNumDB = trainNum;
        final String carNumDB = carNum;

        if (Integer.parseInt(seatNum) == 1) {
            data.put("s1_isReservation", false);
            data.put("s1_User", "");
        }
        else {
            data.put("s2_isReservation", false);
            data.put("s2_User", "");
        }
        data2.put("reservation_info", "");

        for (int i = sectionLower(Integer.parseInt(sectionStartGlobal), Integer.parseInt(sectionEndGlobal)); i <= sectionHigher(Integer.parseInt(sectionStartGlobal), Integer.parseInt(sectionEndGlobal)); i++) {
            final int section = i;
            db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB).document(trainNumDB).collection("car").document(carNumDB).collection("section").document(Integer.toString(section))
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }

        db.collection("user").document(userIDDB)
                .set(data2, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private int sectionLower(int sectionStart, int sectionEnd) {
        if (sectionStart < sectionEnd) return sectionStart;
        else return sectionEnd;
    }

    private int sectionHigher(int sectionStart, int sectionEnd) {
        if (sectionStart > sectionEnd) return sectionStart;
        else return sectionEnd;
    }

    private void myStartActivity(Class c) {
        Intent intent = getIntent();
        intent.getExtras();
        Intent intent2 = new Intent(this, c);
        intent2.putExtras(intent);

        startActivity(intent2);
    }
}


