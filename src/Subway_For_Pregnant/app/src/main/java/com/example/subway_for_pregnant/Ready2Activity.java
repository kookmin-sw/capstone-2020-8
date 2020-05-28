package com.example.subway_for_pregnant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class Ready2Activity extends AppCompatActivity {

    private static final String TAG = "Ready2Activity";
    String globalStartName;     //출발역
    String globalEndName;       //도착역
    String userID;

    String reservationInfo[];
    String transferInfo[];

    String laneInfo;
    String driveInfo;
    String trainNum;
    String carNum;
    String sectionStartGlobal;
    String sectionEndGlobal;
    String seatNum;
    String sStartName;
    String sEndName;

    String getReservationInfo;
    String getTransferInfo;
    int pastStationCount = 0;
    int transferCount = 0;

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
                    doCancelReserve();
                    doCancelUser(0);
                    if (getTransferInfo.length() == 0) {
                        myStartActivity(MainActivity.class);
                    }
                    else {
                        myStartActivity2(TrainActivity.class);
                    }
                    break;
                case R.id.button_cancel:
                    doCancelReserve();
                    doCancelUser(1);
                    myStartActivity(MainActivity.class);
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

                            getReservationInfo = (String) document1.getData().get("reservation_info");
                            getTransferInfo = (String) document1.getData().get("transfer_info");

                            if (getReservationInfo.length() > 0) {
                                reservationInfo = getReservationInfo.split(";");
                                for (int i = 0; i < reservationInfo.length; i++) {
                                    Log.d(TAG, reservationInfo[i]);
                                }

                                laneInfo = reservationInfo[0];
                                driveInfo = reservationInfo[1];
                                trainNum = reservationInfo[2];
                                carNum = reservationInfo[3];
                                sectionStartGlobal = reservationInfo[4];
                                sectionEndGlobal = reservationInfo[5];
                                seatNum = reservationInfo[6];
                                sStartName = reservationInfo[9];
                                sEndName = reservationInfo[10];
                            }

                            if (getTransferInfo.length() > 0) {
                                transferInfo = getTransferInfo.split(";");
                                for (int i = 0; i < transferInfo.length; i++) {
                                    Log.d(TAG, transferInfo[i]);
                                }

                                pastStationCount = Integer.parseInt(transferInfo[0]);
                                transferCount = Integer.parseInt(transferInfo[1]);
                            }

                            String seatPosition[] = {"왼쪽 자리", "오른쪽 자리"};

                            TextView tv_desti_large = findViewById(R.id.desti_large);
                            switch(laneInfo) {
                                case "line8":
                                    tv_desti_large.setText("8호선");
                                    break;
                                case "line9":
                                    tv_desti_large.setText("9호선");
                                    break;
                                default:
                                    break;
                            }

                            TextView tv_current = findViewById(R.id.textView_current);
                            tv_current.setText("출발역: " + sStartName + "\n도착역: " + sEndName + "\n" + carNum + "번 칸의 " + seatPosition[Integer.parseInt(seatNum) - 1]);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void doCancelReserve() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();

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
    }

    private void doCancelUser(int check) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();

        final String userIDDB = userID;

        data.put("reservation_info", "");
        if (check == 1) {
            data.put("transfer_info", "");
        }

        db.collection("user").document(userIDDB)
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

    private int sectionLower(int sectionStart, int sectionEnd) {
        if (sectionStart < sectionEnd) return sectionStart;
        else return sectionEnd;
    }

    private int sectionHigher(int sectionStart, int sectionEnd) {
        if (sectionStart > sectionEnd) return sectionStart;
        else return sectionEnd;
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void myStartActivity2(Class c) {
        Intent intent = getIntent();
        intent.getExtras();
        Intent intent2 = new Intent(this, c);
        intent2.putExtras(intent);
        intent2.putExtra("pastStationCount", pastStationCount);
        intent2.putExtra("transferCount", transferCount);

        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
    }
}


