package com.example.subway_for_pregnant;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ViewSeatsActivity extends AppCompatActivity {
    private static final String TAG = "VS";
    int[] seat1_State, seat2_State;  //지금은 임시로 지정해둠. 0 = 예약가능, 1 = 일반인 사용중, 2 = 예약 불가.
    int carNum;
    int now = 0;

    int stationsLength;         //역 개수. 즉 stations 라고 앞에 붙은 데이터들의 Length.
    String[] stationsStartName; //구간마다 현재역 이름
    int[] stationsStartID;      //구간마다 현재역 코드
    String[] stationsEndName;   //구간마다 다음역 이름
    int[] stationsEndSID;       //구간마다 다음역 코드

    int driveInfoLength;        //노선 개수. 환승 없으면 1, 1번 환승은 2. 이런식으로.
    int[] driveInfoWayCode;     //방면 코드 (1:상행, 2:하행)
    String[] driveInfoLaneName;  //노선 이름. 예: "8호선", "분당선".

    boolean s1_isSit = false;
    boolean s1_isPregnant = false;
    boolean s1_isReservation = false;
    boolean s2_isSit = false;
    boolean s2_isPregnant = false;
    boolean s2_isReservation = false;

    Button[] bt_State;
    TextView[] tv_State;

    //final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_seats);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        //인텐트 호출
        stationsLength = intent.getExtras().getInt("stationsLength");   //역 개수. 즉 stations 라고 앞에 붙은 데이터들의 Length.
        stationsStartName = new String[stationsLength];    //구간마다 현재역 이름
        stationsStartID = new int[stationsLength];            //구간마다 현재역 코드
        stationsEndName = new String[stationsLength];      //구간마다 다음역 이름
        stationsEndSID = new int[stationsLength];             //구간마다 다음역 코드

        driveInfoLength = intent.getExtras().getInt("driveInfoLength");     //노선 개수. 환승 없으면 1, 1번 환승은 2. 이런식으로.
        driveInfoWayCode = new int[driveInfoLength];      //방면 코드 (1:상행, 2:하행)
        driveInfoLaneName = new String[driveInfoLength];

        for (int i = 0; i < stationsLength; i++) {
            stationsStartName[i] = intent.getExtras().getString("stationsStartName" + i);
            stationsStartID[i] = intent.getExtras().getInt("stationsStartID" + i);
            stationsEndName[i] = intent.getExtras().getString("stationsEndName" + i);
            stationsEndSID[i] = intent.getExtras().getInt("stationsEndSID" + i);
            //stationsTravelTime[i] = intent.getExtras().getInt("stationsTravelTime" + i);
        }

        for (int i = 0; i < driveInfoLength; i++) {
            driveInfoLaneName[i] = intent.getExtras().getString("driveInfoLaneName" + i);
            driveInfoWayCode[i] = intent.getExtras().getInt("driveInfoWayCode" + i);
        }

        String driveInfo = "1";
        String laneInfo = "1";

        int trainLength = 6;
        seat1_State = new int[trainLength];
        seat2_State = new int[trainLength];

        for (int i = 0; i < trainLength; i++) {
            seat1_State[i] = -1;
            seat2_State[i] = -1;
        }

        bt_State = new Button[trainLength];
        bt_State[0] = findViewById(R.id.button_StateRight);
        bt_State[1] = findViewById(R.id.button_State2);
        bt_State[2] = findViewById(R.id.button_State3);
        bt_State[3] = findViewById(R.id.button_State4);
        bt_State[4] = findViewById(R.id.button_State5);
        bt_State[5] = findViewById(R.id.button_StateLeft);

        tv_State = new TextView[trainLength];
        tv_State[0] = findViewById(R.id.textView_StateRight);
        tv_State[1] = findViewById(R.id.textView_State2);
        tv_State[2] = findViewById(R.id.textView_State3);
        tv_State[3] = findViewById(R.id.textView_State4);
        tv_State[4] = findViewById(R.id.textView_State5);
        tv_State[5] = findViewById(R.id.textView_StateLeft);

        if (driveInfoWayCode[0] == 1) driveInfo = "Up";
        else driveInfo = "Down";

        switch (driveInfoLaneName[0]) {
            case "2호선":
                laneInfo = "line2";
                break;
            case "8호선":
                laneInfo = "line8";
                break;
        }

        db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document(Integer.toString(1))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            s1_isSit = (Boolean) document.getData().get("s1_isSit");
                            s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                            s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                            s2_isSit = (Boolean) document.getData().get("s2_isSit");
                            s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                            s2_isReservation = (Boolean) document.getData().get("s2_isReservation");

                            //첫 번째 좌석 상태.
                            if (s1_isSit == false) {
                                if (s1_isReservation == false) {
                                    seat1_State[0] = 0;
                                } else {
                                    seat1_State[0] = 2;
                                }
                            } else {
                                if (s1_isPregnant == false && s1_isReservation == false) {
                                    seat1_State[0] = 1;
                                } else {
                                    seat1_State[0] = 2;
                                }
                            }

                            //두 번째 좌석 상태.
                            if (s2_isSit == false) {
                                if (s2_isReservation == false) {
                                    seat2_State[0] = 0;
                                } else {
                                    seat2_State[0] = 2;
                                }
                            } else {
                                if (s2_isPregnant == false && s2_isReservation == false) {
                                    seat2_State[0] = 1;
                                } else {
                                    seat2_State[0] = 2;
                                }
                            }
                            Log.d(TAG, "" + s1_isReservation + " " + s1_isPregnant + " " + s1_isSit);
                            Log.d(TAG, "" + s2_isReservation + " " + s2_isPregnant + " " + s2_isSit);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document(Integer.toString(2))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            s1_isSit = (Boolean) document.getData().get("s1_isSit");
                            s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                            s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                            s2_isSit = (Boolean) document.getData().get("s2_isSit");
                            s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                            s2_isReservation = (Boolean) document.getData().get("s2_isReservation");

                            //첫 번째 좌석 상태.
                            if (s1_isSit == false) {
                                if (s1_isReservation == false) {
                                    seat1_State[1] = 0;
                                } else {
                                    seat1_State[1] = 2;
                                }
                            } else {
                                if (s1_isPregnant == false && s1_isReservation == false) {
                                    seat1_State[1] = 1;
                                } else {
                                    seat1_State[1] = 2;
                                }
                            }

                            //두 번째 좌석 상태.
                            if (s2_isSit == false) {
                                if (s2_isReservation == false) {
                                    seat2_State[1] = 0;
                                } else {
                                    seat2_State[1] = 2;
                                }
                            } else {
                                if (s2_isPregnant == false && s2_isReservation == false) {
                                    seat2_State[1] = 1;
                                } else {
                                    seat2_State[1] = 2;
                                }
                            }
                            Log.d(TAG, "" + s1_isReservation + " " + s1_isPregnant + " " + s1_isSit);
                            Log.d(TAG, "" + s2_isReservation + " " + s2_isPregnant + " " + s2_isSit);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        for (int i = 1; i <= 6; i++) {
            if (seat1_State[i - 1] < 2 && seat2_State[i - 1] < 2) {
                bt_State[i - 1].setText("2");
            }
            else if (seat1_State[i - 1] < 2 || seat2_State[i - 1] < 2) {
                bt_State[i - 1].setText("1");
            }
            else {
                bt_State[i - 1].setText("0");
            }
            Log.d(TAG, "ABCDEFG: " + i + " HOHOHO " + seat1_State[i - 1] + " " + seat2_State[i - 1] +"\n");
        }
        /*
        for (int i = 1; i <= 6; i++) {
            carNum = i - 1;
            db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document(Integer.toString(i))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                s1_isSit = (Boolean) document.getData().get("s1_isSit");
                                s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                                s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                                s2_isSit = (Boolean) document.getData().get("s2_isSit");
                                s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                                s2_isReservation = (Boolean) document.getData().get("s2_isReservation");

                                //첫 번째 좌석 상태.
                                if (s1_isSit == false) {
                                    if (s1_isReservation == false) {
                                        seat1_State[carNum] = 0;
                                    } else {
                                        seat1_State[carNum] = 2;
                                    }
                                } else {
                                    if (s1_isPregnant == false && s1_isReservation == false) {
                                        seat1_State[carNum] = 1;
                                    } else {
                                        seat1_State[carNum] = 2;
                                    }
                                }

                                //두 번째 좌석 상태.
                                if (s2_isSit == false) {
                                    if (s2_isReservation == false) {
                                        seat2_State[carNum] = 0;
                                    } else {
                                        seat2_State[carNum] = 2;
                                    }
                                } else {
                                    if (s2_isPregnant == false && s2_isReservation == false) {
                                        seat2_State[carNum] = 1;
                                    } else {
                                        seat2_State[carNum] = 2;
                                    }
                                }

                                tv_State[carNum].setText("" + carNum);
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
            if (seat1_State[i - 1] < 2 && seat2_State[i - 1] < 2) {
                bt_State[i - 1].setText("2");
            }
            else if (seat1_State[i - 1] < 2 || seat2_State[i - 1] < 2) {
                bt_State[i - 1].setText("1");
            }
            else {
                bt_State[i - 1].setText("0");
            }
        }

         */

        findViewById(R.id.button_StateLeft).setOnClickListener(onClickListener);
        findViewById(R.id.button_State5).setOnClickListener(onClickListener);
        findViewById(R.id.button_State4).setOnClickListener(onClickListener);
        findViewById(R.id.button_State3).setOnClickListener(onClickListener);
        findViewById(R.id.button_State2).setOnClickListener(onClickListener);
        findViewById(R.id.button_StateRight).setOnClickListener(onClickListener);

        findViewById(R.id.button_Seat1).setOnClickListener(onClickListener);
        findViewById(R.id.button_Seat2).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            //final FirebaseFirestore db = FirebaseFirestore.getInstance();
            TextView tv_State;
            String numOfCars;
            String driveInfo = "1";
            String laneInfo = "1";
            //Log.d(TAG, "driveInfoWayCode : " + driveInfoWayCode[0] + ", driveInfoLaneName : " + driveInfoLaneName[0]);

            if (driveInfoWayCode[0] == 1) driveInfo = "Up";
            else driveInfo = "Down";

            switch (driveInfoLaneName[0]) {
                case "2호선":
                    laneInfo = "line2";
                    break;
                case "8호선":
                    laneInfo = "line8";
                    break;
            }
            //Log.d(TAG, "diveInfo : " + driveInfo + ", laneInfo : " + laneInfo);
            //CollectionReference colRef = db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car");

            switch (v.getId()) {
                //button_Seat1~2 는 좌석 선택 버튼.
                case R.id.button_Seat1:
                    if (seat1_State[now] == 0) {
                        myStartActivity(Ready2Activity.class);
                    } else if (seat1_State[now] == 1) {
                        startToast("앉아있는 일반인이 있습니다.");
                    } else if (seat1_State[now] == 2) {
                        startToast("이미 예약되거나 다른 산모가 사용 중인 좌석입니다.");
                    } else {
                        startToast("에러입니다.");
                    }
                    break;
                case R.id.button_Seat2:
                    if (seat2_State[now] == 0) {
                        myStartActivity(Ready2Activity.class);
                    } else if (seat2_State[now] == 1) {
                        startToast("앉아있는 일반인이 있습니다.");
                    } else if (seat2_State[now] == 2) {
                        startToast("이미 예약되거나 다른 산모가 사용 중인 좌석입니다.");
                    } else {
                        startToast("에러입니다.");
                    }
                    break;

                //button_State들은 열차 칸 선택 버튼.
                case R.id.button_StateRight:
                    //tv_State = findViewById(R.id.textView_StateRight);
                    //numOfCars = (String) tv_State.getText();
                    now = 0;

                    /*
                    db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document(numOfCars)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        s1_isSit = (Boolean) document.getData().get("s1_isSit");
                                        s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                                        s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                                        s2_isSit = (Boolean) document.getData().get("s2_isSit");
                                        s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                                        s2_isReservation = (Boolean) document.getData().get("s2_isReservation");
                                        Log.d(TAG, String.valueOf(s1_isSit) + "1");
                                        Log.d(TAG, String.valueOf(s1_isPregnant) + "2");
                                        Log.d(TAG, String.valueOf(s1_isReservation) + "3");
                                        Log.d(TAG, String.valueOf(s2_isSit) + "4");
                                        Log.d(TAG, String.valueOf(s2_isPregnant) + "5");
                                        Log.d(TAG, String.valueOf(s2_isReservation) + "6");

                                        checkSeats(v);
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }

                                }
                            });

                     */
                    doSetBtnColor();
                    break;
                case R.id.button_State2:
                    //tv_State = findViewById(R.id.textView_State2);
                    //numOfCars = (String) tv_State.getText();
                    now = 1;
                    doSetBtnColor();
                    break;
                case R.id.button_State3:
                    //tv_State = findViewById(R.id.textView_State3);
                    //numOfCars = (String) tv_State.getText();
                    now = 2;
                    doSetBtnColor();
                    break;
                case R.id.button_State4:
                    //tv_State = findViewById(R.id.textView_State4);
                    //numOfCars = (String) tv_State.getText();
                    now = 3;
                    doSetBtnColor();
                    break;
                case R.id.button_State5:
                    //tv_State = findViewById(R.id.textView_State5);
                    //numOfCars = (String) tv_State.getText();
                    now = 4;
                    seat1_State[now] = 0;
                    seat2_State[now] = 0;
                    doSetBtnColor();
                    break;
                case R.id.button_StateLeft:
                    //tv_State = findViewById(R.id.textView_StateLeft);
                    //numOfCars = (String) tv_State.getText();  //열차 칸 번호.
                    now = 5;
                    doSetBtnColor();
                    break;
            }
        }
    };

    private void doSetBtnColor() {
        setBtnColor(R.id.button_Seat1, seat1_State[now]);
        setBtnColor(R.id.button_Seat2, seat2_State[now]);
    }

    private void setBtnColor(int btnId, int check) {
        if (check == 0) {
            findViewById(btnId).setBackgroundColor(Color.parseColor("#008000")); //Green
        } else if (check == 1) {
            findViewById(btnId).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
        } else if (check == 2) {
            findViewById(btnId).setBackgroundColor(Color.parseColor("#FF0000")); //Red
        } else {
            findViewById(btnId).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
        }
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}