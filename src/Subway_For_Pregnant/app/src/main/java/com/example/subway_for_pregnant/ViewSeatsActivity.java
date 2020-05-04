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
    private static final String TAG = "ViewSeatsActivity";
    int[] seat1_State, seat2_State;  //지금은 임시로 지정해둠. 0 = 예약가능, 1 = 일반인 사용중, 2 = 예약 불가.
    int carNum;
    int now = 0;

    String globalStartName;     //출발역
    String globalEndName;       //도착역
    int globalStationCount;     //정차역 수

    int stationsLength;         //역 개수. 즉 stations 라고 앞에 붙은 데이터들의 Length.
    String[] stationsStartName; //구간마다 현재역 이름
    int[] stationsStartID;      //구간마다 현재역 코드
    String[] stationsEndName;   //구간마다 다음역 이름
    int[] stationsEndSID;       //구간마다 다음역 코드

    int driveInfoLength;        //노선 개수. 환승 없으면 1, 1번 환승은 2. 이런식으로.
    int[] driveInfoWayCode;     //방면 코드 (1:상행, 2:하행)
    String[] driveInfoLaneName;  //노선 이름. 예: "8호선", "분당선".

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
        globalStartName = intent.getExtras().getString("globalStartName");
        globalEndName = intent.getExtras().getString("globalEndName");
        globalStationCount = intent.getExtras().getInt("globalStationCount");

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

        for (int i = 1; i <= 6; i++) {
            final int carNum = i;
            db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document(Integer.toString(carNum))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                boolean s1_isSit = (Boolean) document.getData().get("s1_isSit");
                                boolean s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                                boolean s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                                boolean s2_isSit = (Boolean) document.getData().get("s2_isSit");
                                boolean s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                                boolean s2_isReservation = (Boolean) document.getData().get("s2_isReservation");

                                int seat1_State = -1;
                                int seat2_State = -1;
                                Button bt_State = findViewById(R.id.button_StateRight);

                                switch (carNum) {
                                    case 1:
                                        bt_State = findViewById(R.id.button_StateRight);
                                        break;
                                    case 2:
                                        bt_State = findViewById(R.id.button_State2);
                                        break;
                                    case 3:
                                        bt_State = findViewById(R.id.button_State3);
                                        break;
                                    case 4:
                                        bt_State = findViewById(R.id.button_State4);
                                        break;
                                    case 5:
                                        bt_State = findViewById(R.id.button_State5);
                                        break;
                                    case 6:
                                        bt_State = findViewById(R.id.button_StateLeft);
                                        break;
                                }

                                //첫 번째 좌석 상태.
                                if (s1_isSit == false) {
                                    if (s1_isReservation == false) {
                                        seat1_State = 0;
                                    } else {
                                        seat1_State = 2;
                                    }
                                } else {
                                    if (s1_isPregnant == false && s1_isReservation == false) {
                                        seat1_State = 1;
                                    } else {
                                        seat1_State = 2;
                                    }
                                }

                                //두 번째 좌석 상태.
                                if (s2_isSit == false) {
                                    if (s2_isReservation == false) {
                                        seat2_State = 0;
                                    } else {
                                        seat2_State = 2;
                                    }
                                } else {
                                    if (s2_isPregnant == false && s2_isReservation == false) {
                                        seat2_State = 1;
                                    } else {
                                        seat2_State = 2;
                                    }
                                }

                                // tv_State[carNum].setText("" + carNum);

                                if (seat1_State < 2 && seat2_State < 2) {
                                    bt_State.setText("2");
                                } else if (seat1_State < 2 || seat2_State < 2) {
                                    bt_State.setText("1");
                                } else {
                                    bt_State.setText("0");
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

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
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            String driveInfo = "1";
            String laneInfo = "1";

            TextView tv_carNum = findViewById(R.id.textView_carNum);
            char nowCarNum = '9';
            String showCarNum = "";

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

            switch (v.getId()) {
                case R.id.button_StateRight:
                    db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document("1")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        boolean s1_isSit = (Boolean) document.getData().get("s1_isSit");
                                        boolean s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                                        boolean s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                                        boolean s2_isSit = (Boolean) document.getData().get("s2_isSit");
                                        boolean s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                                        boolean s2_isReservation = (Boolean) document.getData().get("s2_isReservation");

                                        int seat1_State;
                                        int seat2_State;

                                        //첫 번째 좌석 상태.
                                        if (s1_isSit == false) {
                                            if (s1_isReservation == false) {
                                                seat1_State = 0;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        } else {
                                            if (s1_isPregnant == false && s1_isReservation == false) {
                                                seat1_State = 1;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        }

                                        //두 번째 좌석 상태.
                                        if (s2_isSit == false) {
                                            if (s2_isReservation == false) {
                                                seat2_State = 0;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        } else {
                                            if (s2_isPregnant == false && s2_isReservation == false) {
                                                seat2_State = 1;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        }

                                        TextView tv_carNum = findViewById(R.id.textView_carNum);
                                        tv_carNum.setText(tv_State[0] + "번째 칸");
                                        doSetBtnColor(R.id.button_Seat1, seat1_State, R.id.button_Seat2, seat2_State);
                                        /*
                                        if (seat1_State == 0) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat1_State == 1) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat1_State == 2) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                        if (seat2_State == 0) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat2_State == 1) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat2_State == 2) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                         */
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                    break;
                case R.id.button_State2:
                    db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document("2")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        boolean s1_isSit = (Boolean) document.getData().get("s1_isSit");
                                        boolean s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                                        boolean s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                                        boolean s2_isSit = (Boolean) document.getData().get("s2_isSit");
                                        boolean s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                                        boolean s2_isReservation = (Boolean) document.getData().get("s2_isReservation");

                                        int seat1_State;
                                        int seat2_State;

                                        //첫 번째 좌석 상태.
                                        if (s1_isSit == false) {
                                            if (s1_isReservation == false) {
                                                seat1_State = 0;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        } else {
                                            if (s1_isPregnant == false && s1_isReservation == false) {
                                                seat1_State = 1;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        }

                                        //두 번째 좌석 상태.
                                        if (s2_isSit == false) {
                                            if (s2_isReservation == false) {
                                                seat2_State = 0;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        } else {
                                            if (s2_isPregnant == false && s2_isReservation == false) {
                                                seat2_State = 1;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        }

                                        TextView tv_carNum = findViewById(R.id.textView_carNum);
                                        tv_carNum.setText(tv_State[1].getText().toString() + "번째 칸");
                                        doSetBtnColor(R.id.button_Seat1, seat1_State, R.id.button_Seat2, seat2_State);
                                        /*
                                        if (seat1_State == 0) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat1_State == 1) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat1_State == 2) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                        if (seat2_State == 0) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat2_State == 1) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat2_State == 2) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                         */
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                    break;
                case R.id.button_State3:
                    db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document("3")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        boolean s1_isSit = (Boolean) document.getData().get("s1_isSit");
                                        boolean s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                                        boolean s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                                        boolean s2_isSit = (Boolean) document.getData().get("s2_isSit");
                                        boolean s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                                        boolean s2_isReservation = (Boolean) document.getData().get("s2_isReservation");

                                        int seat1_State;
                                        int seat2_State;

                                        //첫 번째 좌석 상태.
                                        if (s1_isSit == false) {
                                            if (s1_isReservation == false) {
                                                seat1_State = 0;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        } else {
                                            if (s1_isPregnant == false && s1_isReservation == false) {
                                                seat1_State = 1;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        }

                                        //두 번째 좌석 상태.
                                        if (s2_isSit == false) {
                                            if (s2_isReservation == false) {
                                                seat2_State = 0;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        } else {
                                            if (s2_isPregnant == false && s2_isReservation == false) {
                                                seat2_State = 1;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        }

                                        TextView tv_carNum = findViewById(R.id.textView_carNum);
                                        tv_carNum.setText(tv_State[2].getText().toString() + "번째 칸");
                                        doSetBtnColor(R.id.button_Seat1, seat1_State, R.id.button_Seat2, seat2_State);
                                        /*
                                        if (seat1_State == 0) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat1_State == 1) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat1_State == 2) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                        if (seat2_State == 0) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat2_State == 1) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat2_State == 2) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                         */
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                    break;
                case R.id.button_State4:
                    db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document("4")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        boolean s1_isSit = (Boolean) document.getData().get("s1_isSit");
                                        boolean s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                                        boolean s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                                        boolean s2_isSit = (Boolean) document.getData().get("s2_isSit");
                                        boolean s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                                        boolean s2_isReservation = (Boolean) document.getData().get("s2_isReservation");

                                        int seat1_State;
                                        int seat2_State;

                                        //첫 번째 좌석 상태.
                                        if (s1_isSit == false) {
                                            if (s1_isReservation == false) {
                                                seat1_State = 0;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        } else {
                                            if (s1_isPregnant == false && s1_isReservation == false) {
                                                seat1_State = 1;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        }

                                        //두 번째 좌석 상태.
                                        if (s2_isSit == false) {
                                            if (s2_isReservation == false) {
                                                seat2_State = 0;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        } else {
                                            if (s2_isPregnant == false && s2_isReservation == false) {
                                                seat2_State = 1;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        }

                                        TextView tv_carNum = findViewById(R.id.textView_carNum);
                                        tv_carNum.setText(tv_State[3].getText().toString() + "번째 칸");
                                        doSetBtnColor(R.id.button_Seat1, seat1_State, R.id.button_Seat2, seat2_State);
                                        /*
                                        if (seat1_State == 0) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat1_State == 1) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat1_State == 2) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                        if (seat2_State == 0) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat2_State == 1) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat2_State == 2) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                         */
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                    break;
                case R.id.button_State5:
                    db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document("5")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        boolean s1_isSit = (Boolean) document.getData().get("s1_isSit");
                                        boolean s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                                        boolean s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                                        boolean s2_isSit = (Boolean) document.getData().get("s2_isSit");
                                        boolean s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                                        boolean s2_isReservation = (Boolean) document.getData().get("s2_isReservation");

                                        int seat1_State;
                                        int seat2_State;

                                        //첫 번째 좌석 상태.
                                        if (s1_isSit == false) {
                                            if (s1_isReservation == false) {
                                                seat1_State = 0;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        } else {
                                            if (s1_isPregnant == false && s1_isReservation == false) {
                                                seat1_State = 1;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        }

                                        //두 번째 좌석 상태.
                                        if (s2_isSit == false) {
                                            if (s2_isReservation == false) {
                                                seat2_State = 0;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        } else {
                                            if (s2_isPregnant == false && s2_isReservation == false) {
                                                seat2_State = 1;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        }

                                        TextView tv_carNum = findViewById(R.id.textView_carNum);
                                        tv_carNum.setText(tv_State[4].getText().toString() + "번째 칸");
                                        doSetBtnColor(R.id.button_Seat1, seat1_State, R.id.button_Seat2, seat2_State);
                                        /*
                                        if (seat1_State == 0) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat1_State == 1) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat1_State == 2) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                        if (seat2_State == 0) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat2_State == 1) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat2_State == 2) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                         */
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                    break;
                case R.id.button_StateLeft:
                    db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document("6")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        boolean s1_isSit = (Boolean) document.getData().get("s1_isSit");
                                        boolean s1_isPregnant = (Boolean) document.getData().get("s1_isPregnant");
                                        boolean s1_isReservation = (Boolean) document.getData().get("s1_isReservation");
                                        boolean s2_isSit = (Boolean) document.getData().get("s2_isSit");
                                        boolean s2_isPregnant = (Boolean) document.getData().get("s2_isPregnant");
                                        boolean s2_isReservation = (Boolean) document.getData().get("s2_isReservation");

                                        int seat1_State;
                                        int seat2_State;

                                        //첫 번째 좌석 상태.
                                        if (s1_isSit == false) {
                                            if (s1_isReservation == false) {
                                                seat1_State = 0;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        } else {
                                            if (s1_isPregnant == false && s1_isReservation == false) {
                                                seat1_State = 1;
                                            } else {
                                                seat1_State = 2;
                                            }
                                        }

                                        //두 번째 좌석 상태.
                                        if (s2_isSit == false) {
                                            if (s2_isReservation == false) {
                                                seat2_State = 0;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        } else {
                                            if (s2_isPregnant == false && s2_isReservation == false) {
                                                seat2_State = 1;
                                            } else {
                                                seat2_State = 2;
                                            }
                                        }

                                        TextView tv_carNum = findViewById(R.id.textView_carNum);
                                        tv_carNum.setText(tv_State[5].getText().toString() + "번째 칸");
                                        doSetBtnColor(R.id.button_Seat1, seat1_State, R.id.button_Seat2, seat2_State);
                                        /*
                                        if (seat1_State == 0) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat1_State == 1) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat1_State == 2) {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat1).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                        if (seat2_State == 0) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#008000")); //Green
                                        } else if (seat2_State == 1) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
                                        } else if (seat2_State == 2) {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#FF0000")); //Red
                                        } else {
                                            findViewById(R.id.button_Seat2).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
                                        }

                                         */
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                    break;
                case R.id.button_Seat1:
                    Button bt_Seat1 = findViewById(R.id.button_Seat1);

                    if (bt_Seat1.getText().equals("0") || bt_Seat1.getText().equals("1")) {
                        showCarNum = tv_carNum.getText().toString();
                        nowCarNum = showCarNum.charAt(0);
                        startToast(nowCarNum + "");
                        myStartActivity(Ready2Activity.class);
                    }
                    else if (bt_Seat1.getText().equals("2")) {
                        startToast("현재 예약이 불가능한 좌석입니다.");
                    }
                    else {
                        startToast("데이터를 불러오기 전입니다.");
                    }

                    break;
                case R.id.button_Seat2:
                    Button bt_Seat2 = findViewById(R.id.button_Seat2);

                    if (bt_Seat2.getText().equals("0") || bt_Seat2.getText().equals("1")) {
                        showCarNum = tv_carNum.getText().toString();
                        nowCarNum = showCarNum.charAt(0);
                        startToast(nowCarNum + "");
                        myStartActivity(Ready2Activity.class);
                    }
                    else if (bt_Seat2.getText().equals("2")) {
                        startToast("현재 예약이 불가능한 좌석입니다.");
                    }
                    else {
                        startToast("데이터를 불러오기 전입니다.");
                    }

                    break;
            }
/*
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
 */
            //CollectionReference colRef = db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car");
/*
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
/*
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

 */
        }
    };

    private void doSetBtnColor(int btnId1, int seat1_State, int btnId2, int seat2_State) {
        setBtnColor(btnId1, seat1_State);
        setBtnColor(btnId2, seat2_State);
    }

    private void setBtnColor(int btnId, int check) {
        Button btn = findViewById(btnId);
        if (check == 0) {
            btn.setBackgroundColor(Color.parseColor("#008000")); //Green
            btn.setText("0");
            btn.setTextColor(Color.parseColor("#008000"));
        } else if (check == 1) {
            btn.setBackgroundColor(Color.parseColor("#FFA500")); //Gold
            btn.setText("1");
            btn.setTextColor(Color.parseColor("#FFA500"));
        } else if (check == 2) {
            btn.setBackgroundColor(Color.parseColor("#FF0000")); //Red
            btn.setText("2");
            btn.setTextColor(Color.parseColor("#FF0000"));
        } else {
            btn.setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
            btn.setText("3");
            btn.setTextColor(Color.parseColor("#545454"));
        }
    }

    private void myStartActivity(Class c) {
        Intent intent = getIntent();
        intent.getExtras();
        Intent intent2 = new Intent(this, c);
        intent2.putExtras(intent);
        startActivity(intent2);
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}