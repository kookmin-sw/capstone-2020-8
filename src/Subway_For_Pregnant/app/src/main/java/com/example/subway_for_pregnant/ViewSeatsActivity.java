package com.example.subway_for_pregnant;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ViewSeatsActivity extends AppCompatActivity {
    private static final String TAG = "ViewSeatsActivity";
    int[] seat1, seat2;  //지금은 임시로 지정해둠. 0 = 예약가능, 1 = 일반인 사용중, 2 = 예약 불가.
    int sectionStartGlobal;
    int sectionEndGlobal;
    int btnNumGlobal;
    int carNumGlobal;
    int trainLength = 6;  //해당 열차의 길이. 열차 칸의 총 개수.
    int forCount = 0;
    int isReservationCount = 0;

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
    int[] driveInfoStationCount;

    int pastStationCount = 0;
    int transferCount = 0;
    String trainName;

    Button[] bt_State;
    TextView[] tv_State;

    String userID;
    int checkReservation = 0;

    int btnState1 = -1;
    int btnState2 = -1;

    String historyDB = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_seats);

        Intent intent = getIntent();

        //인텐트 호출
        globalStartName = intent.getExtras().getString("globalStartName");
        globalEndName = intent.getExtras().getString("globalEndName");
        globalStationCount = intent.getExtras().getInt("globalStationCount");

        stationsLength = intent.getExtras().getInt("stationsLength");   //역 개수. 즉 stations 라고 앞에 붙은 데이터들의 Length.
        stationsStartName = new String[stationsLength];    //구간마다 현재역 이름
        stationsStartID = new int[stationsLength];         //구간마다 현재역 코드
        stationsEndName = new String[stationsLength];      //구간마다 다음역 이름
        stationsEndSID = new int[stationsLength];          //구간마다 다음역 코드

        driveInfoLength = intent.getExtras().getInt("driveInfoLength");     //노선 개수. 환승 없으면 1, 1번 환승은 2. 이런식으로.
        driveInfoWayCode = new int[driveInfoLength];      //방면 코드 (1:상행, 2:하행)
        driveInfoLaneName = new String[driveInfoLength];  //승차역 호선명
        driveInfoStationCount = new int[driveInfoLength];  //이동 역 수

        userID = intent.getExtras().getString("user");

        historyDB = intent.getExtras().getString("historyDB");

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
            driveInfoStationCount[i] = intent.getExtras().getInt("driveInfoStationCount" + i);
        }

        trainName = intent.getExtras().getString("trainName");

        try {
            pastStationCount = intent.getExtras().getInt("pastStationCount");
            transferCount = intent.getExtras().getInt("transferCount");
        }
        catch (NullPointerException e) {
            pastStationCount = 0;
            transferCount = 0;
        }

        String laneInfo = "1";
        String driveInfo = "1";

        sectionStartGlobal = stationsStartID[pastStationCount];     //출발역
        sectionEndGlobal = stationsStartID[pastStationCount + driveInfoStationCount[transferCount] - 1];  //도착역

        seat1 = new int[trainLength];
        seat2 = new int[trainLength];

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

        if (driveInfoWayCode[transferCount] == 1) driveInfo = "Up";
        else driveInfo = "Down";

        switch (driveInfoLaneName[transferCount]) {
            case "2호선":
                laneInfo = "line2";
                break;
            case "8호선":
                laneInfo = "line8";
                break;
            case "9호선":
                laneInfo = "line9";
                break;
            default:
                break;
        }

        initSeatStateBtn();
        getCarState(laneInfo, driveInfo, 0);

        findViewById(R.id.button_StateLeft).setOnClickListener(onClickListener);
        findViewById(R.id.button_State5).setOnClickListener(onClickListener);
        findViewById(R.id.button_State4).setOnClickListener(onClickListener);
        findViewById(R.id.button_State3).setOnClickListener(onClickListener);
        findViewById(R.id.button_State2).setOnClickListener(onClickListener);
        findViewById(R.id.button_StateRight).setOnClickListener(onClickListener);

        findViewById(R.id.button_Seat1).setOnClickListener(onClickListener);
        findViewById(R.id.button_Seat2).setOnClickListener(onClickListener);
        findViewById(R.id.button_Refresh).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            String driveInfo = "1";
            String laneInfo = "1";

            if (driveInfoWayCode[transferCount] == 1) driveInfo = "Up";
            else driveInfo = "Down";

            switch (driveInfoLaneName[transferCount]) {
                case "2호선":
                    laneInfo = "line2";
                    break;
                case "8호선":
                    laneInfo = "line8";
                    break;
                case "9호선":
                    laneInfo = "line9";
                    break;
                default:
                    break;
            }

            final String laneInfoDB = laneInfo;
            final String driveInfoDB = driveInfo;
            final int carNum;

            switch (v.getId()) {
                case R.id.button_StateRight:
                    carNumGlobal = 1;
                    doSetBtnColor(1, laneInfoDB, driveInfoDB);
                    break;
                case R.id.button_State2:
                    carNumGlobal = 2;
                    doSetBtnColor(2, laneInfoDB, driveInfoDB);
                    break;
                case R.id.button_State3:
                    carNumGlobal = 3;
                    doSetBtnColor(3, laneInfoDB, driveInfoDB);
                    break;
                case R.id.button_State4:
                    carNumGlobal = 4;
                    doSetBtnColor(4, laneInfoDB, driveInfoDB);
                    break;
                case R.id.button_State5:
                    carNumGlobal = 5;
                    doSetBtnColor(5, laneInfoDB, driveInfoDB);
                    break;
                case R.id.button_StateLeft:
                    carNumGlobal = 6;
                    doSetBtnColor(6, laneInfoDB, driveInfoDB);
                    break;
                case R.id.button_Seat1:
                    Button bt_Seat1 = findViewById(R.id.button_Seat1);
                    btnNumGlobal = 1;
                    carNum = carNumGlobal;

                    if (btnState1 == 0 || btnState1 == 1) {
                        startReservation(laneInfoDB, driveInfoDB, carNum);
                        //myStartActivity(Ready2Activity.class);
                    }
                    else if (btnState1 == 2) {
                        startToast("현재 예약이 불가능한 좌석입니다.");
                    }
                    else {
                        startToast("데이터를 불러오기 전입니다.");
                    }
                    break;
                case R.id.button_Seat2:
                    Button bt_Seat2 = findViewById(R.id.button_Seat2);
                    btnNumGlobal = 2;
                    carNum = carNumGlobal;

                    if (btnState2 == 0 || btnState2 == 1) {
                        startReservation(laneInfoDB, driveInfoDB, carNum);
                        //myStartActivity(Ready2Activity.class);
                    }
                    else if (btnState2 == 2) {
                        startToast("현재 예약이 불가능한 좌석입니다.");
                    }
                    else {
                        startToast("데이터를 불러오기 전입니다.");
                    }
                    break;
                case R.id.button_Refresh:
                    initSeatStateBtn();
                    getCarState(laneInfo, driveInfo, 0);
                    break;
                default:
                    break;
            }
        }
    };

    private void initSeatStateBtn() {
        Button bt_StateRight = findViewById(R.id.button_StateRight);
        Button bt_State2 = findViewById(R.id.button_State2);
        Button bt_State3 = findViewById(R.id.button_State3);
        Button bt_State4 = findViewById(R.id.button_State4);
        Button bt_State5 = findViewById(R.id.button_State5);
        Button bt_StateLeft = findViewById(R.id.button_StateLeft);
        Button bt_Seat1 = findViewById(R.id.button_Seat1);
        Button bt_Seat2 = findViewById(R.id.button_Seat2);

        bt_StateRight.setText("0");
        bt_State2.setText("0");
        bt_State3.setText("0");
        bt_State4.setText("0");
        bt_State5.setText("0");
        bt_StateLeft.setText("0");

        btnState1 = 3;
        btnState2 = 3;

        bt_Seat1.setBackgroundResource(R.drawable.selector_button_gray);
        bt_Seat2.setBackgroundResource(R.drawable.selector_button_gray);

        forCount = 0;
        isReservationCount = 0;
        checkReservation = 0;
    }

    private void getCarState(String laneInfo, String driveInfo, final int select) {
        for (int i = 1; i <= 6; i++) {  //열차 6칸.
            getCarState2(laneInfo, driveInfo, i, select);
        }
    }

    private void getCarState2(final String laneInfoDB, final String driveInfoDB, final int carNum, final int select) {
        if (sectionStartGlobal <= sectionEndGlobal) {
            for (int j = sectionStartGlobal; j <= sectionEndGlobal; j++) {
                getCarStateDB(laneInfoDB, driveInfoDB, carNum, j, select);
            }
        }
        else {
            for (int j = sectionStartGlobal; j >= sectionEndGlobal; j--) {
                getCarStateDB(laneInfoDB, driveInfoDB, carNum, j, select);
            }
        }
    }

    private void getCarStateDB(final String laneInfoDB, final String driveInfoDB, final int carNum, int j, final int select) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String trainNameDB = trainName;
        final int section = j;
        final int sectionStartDB = sectionStartGlobal;
        final int sectionEndDB = sectionEndGlobal;

        db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB).document(trainNameDB).collection("car").document(Integer.toString(carNum)).collection("section").document(Integer.toString(section))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d(TAG, String.valueOf(task.isSuccessful()));
                        if (task.isSuccessful()) {
                            DocumentSnapshot document4 = task.getResult();
                            boolean s1_isReservation_section = (Boolean) document4.getData().get("s1_isReservation");
                            boolean s2_isReservation_section = (Boolean) document4.getData().get("s2_isReservation");

                            int bt_State_num = 0;  // 홀수면 1번 자리에 예약 있음, 짝수면 2번 자리에 예약 있음.
                            int bt_State_setNum = 0;
                            int bt_State_length = 0;
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

                            //첫 번째 좌석에 예약이 있을 때.
                            if (s1_isReservation_section == true) {
                                bt_State_num = Integer.parseInt(bt_State.getText().toString().substring(0, 1));
                                if (bt_State_num % 2 == 0) {
                                    bt_State_setNum = bt_State_num + 1;
                                    bt_State.setText("" + bt_State_setNum + bt_State.getText().toString().substring(1));
                                }
                            }

                            //두 번째 좌석에 예약이 있을 때.
                            if (s2_isReservation_section == true) {
                                bt_State_num = Integer.parseInt(bt_State.getText().toString().substring(0, 1));
                                if (bt_State_num < 2) {
                                    bt_State_setNum = bt_State_num + 2;
                                    bt_State.setText("" + bt_State_setNum + bt_State.getText().toString().substring(1));
                                }
                            }
                            bt_State.setText(bt_State.getText().toString() + "0");

                            bt_State_length = bt_State.getText().toString().length() - 1;  //300000 이런 식으로 설정해줌. 맨 첫자리는 예약 상태, 그 후의 0의 개수는 확인한 구간 개수.
                            if (bt_State_length - 1 == sectionHigher(sectionStartDB, sectionEndDB) - sectionLower(sectionStartDB, sectionEndDB)) {
                                chooseSeatStateBtn(carNum);

                                if (select == 1) {
                                    doReservation(laneInfoDB, driveInfoDB, carNum);
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void chooseSeatStateBtn(final int carNum) {
        //선택한 열차칸의 번호를 구별해주는 함수.
        switch (carNum) {
            case 1:
                setSeatStateBtn(R.id.button_StateRight, carNum);
                break;
            case 2:
                setSeatStateBtn(R.id.button_State2, carNum);
                break;
            case 3:
                setSeatStateBtn(R.id.button_State3, carNum);
                break;
            case 4:
                setSeatStateBtn(R.id.button_State4, carNum);
                break;
            case 5:
                setSeatStateBtn(R.id.button_State5, carNum);
                break;
            case 6:
                setSeatStateBtn(R.id.button_StateLeft, carNum);
                break;
        }
    }

    private void setSeatStateBtn(int btnId, final int carNum) {
        //선택한 열차칸의 두 자리의 상태를 설정해주는 함수.
        Button bt_State = findViewById(btnId);
        int bt_State_num = Integer.parseInt(bt_State.getText().toString().substring(0, 1));

        if (bt_State_num == 0) {
            bt_State.setText("2");  // 해당 열차칸의 빈자리 2개.
            seat1[carNum - 1] = 0;
            seat2[carNum - 1] = 0;
        }
        else if (bt_State_num == 1) {
            bt_State.setText("1");  // 해당 열차칸의 빈자리 1개, 2번 자리가 비어있음.
            seat1[carNum - 1] = 2;
            seat2[carNum - 1] = 0;
        }
        else if (bt_State_num == 2) {
            bt_State.setText("1");  // 해당 열차칸의 빈자리 1개, 1번 자리가 비어있음.
            seat1[carNum - 1] = 0;
            seat2[carNum - 1] = 2;
        }
        else {
            bt_State.setText("0");  // 해당 열차칸의 빈자리 0개.
            seat1[carNum - 1] = 2;
            seat2[carNum - 1] = 2;
        }
    }

    private void doSetBtnColor(final int carNum, final String laneInfoDB, final String driveInfoDB) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String trainNameDB = trainName;

        db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB).document(trainNameDB).collection("car").document(Integer.toString(carNum))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final DocumentSnapshot document1 = task.getResult();
                            boolean s1_isSit = (Boolean) document1.getData().get("s1_isSit");
                            boolean s2_isSit = (Boolean) document1.getData().get("s2_isSit");

                            setBtnColor(R.id.button_Seat1, seat1[carNum - 1], s1_isSit, 1);
                            setBtnColor(R.id.button_Seat2, seat2[carNum - 1], s2_isSit, 2);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void setBtnColor(int btnId, int check, boolean isSit, int btnState) {
        Button btn = findViewById(btnId);
        if (check == 0 && isSit == true) {
            btn.setBackgroundResource(R.drawable.selector_button_gold);
            if (btnState == 1) {
                btnState1 = 1;
            } else {
                btnState2 = 1;
            }
        } else if (check == 0) {
            btn.setBackgroundResource(R.drawable.selector_button_green);
            if (btnState == 1) {
                btnState1 = 0;
            } else {
                btnState2 = 0;
            }
        } else if (check == 2) {
            btn.setBackgroundResource(R.drawable.selector_button_red);
            if (btnState == 1) {
                btnState1 = 2;
            } else {
                btnState2 = 2;
            }
        } else {
            btn.setBackgroundResource(R.drawable.selector_button_gray);
            if (btnState == 1) {
                btnState1 = 3;
            } else {
                btnState2 = 3;
            }
        }
    }

    private void startReservation(final String laneInfoDB, final String driveInfoDB, final int carNumDB) {
        if (sectionStartGlobal <= sectionEndGlobal) {
            for (int j = sectionStartGlobal; j <= sectionEndGlobal; j++) {
                checkIsReservationDB(laneInfoDB, driveInfoDB, carNumDB, j);
            }
        }
        else {
            for (int j = sectionStartGlobal; j >= sectionEndGlobal; j--) {
                checkIsReservationDB(laneInfoDB, driveInfoDB, carNumDB, j);
            }
        }
    }

    private void checkIsReservationDB(final String laneInfoDB, final String driveInfoDB, final int carNumDB, final int j) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String trainNameDB = trainName;

        db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB).document(trainNameDB).collection("car").document(Integer.toString(carNumDB)).collection("section").document(Integer.toString(j))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final DocumentSnapshot document1 = task.getResult();
                            boolean s1_isReservation = (Boolean) document1.getData().get("s1_isReservation");
                            boolean s2_isReservation = (Boolean) document1.getData().get("s2_isReservation");

                            forCount++;
                            if (btnNumGlobal == 1 && s1_isReservation == false) {
                                isReservationCount++;
                            } else if (btnNumGlobal == 2 && s2_isReservation == false) {
                                isReservationCount++;
                            }

                            if (forCount == driveInfoStationCount[transferCount]) {
                                if (forCount == isReservationCount) {
                                    getCarState2(laneInfoDB, driveInfoDB, carNumDB, 1);
                                    startToast("예약하였습니다.");
                                } else {
                                    startToast("예약을 할 수 없습니다.");
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void doReservation(final String laneInfoDB, final String driveInfoDB, final int carNum) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> data2 = new HashMap<>();
        final String trainNameDB = trainName; //현재 이 열차 밖에 없음.
        final String userIDDB = userID;
        String reservationInfo = laneInfoDB + ";" + driveInfoDB + ";" + trainNameDB + ";" + carNum + ";"
                + sectionStartGlobal + ";" + sectionEndGlobal + ";" + btnNumGlobal + ";" //호선 기준 예약한 첫 구간, 예약한 마지막 구간, 좌석 번호 (1 or 2)
                + stationsStartID[0] + ";" + stationsEndSID[globalStationCount - 1] + ";"  //검색한 출발역코드 도착역코드
                + stationsStartName[pastStationCount] + ";" + stationsEndName[pastStationCount + driveInfoStationCount[transferCount] - 1];  //호선 기준 출발역 도착역
        String transferInfo = "";

        if (transferCount + 1 < driveInfoLength) {
            transferInfo = (pastStationCount + driveInfoStationCount[transferCount]) + ";"  //pastStationCount: 지난 역 개수
                    + (transferCount + 1) + ";"  //transferCount: 환승한 횟수
                    + stationsStartID[0] + ";" + stationsEndSID[globalStationCount - 1];  //검색한 출발역코드 도착역코드
        }

        if (btnNumGlobal == 1) {
            data.put("s1_isReservation", true);
            data.put("s1_User", userID);
        } else {
            data.put("s2_isReservation", true);
            data.put("s2_User", userID);
        }
        data2.put("reservation_info", reservationInfo);
        data2.put("transfer_info", transferInfo);
        data2.put("history", historyDB);

        for (int i = sectionLower(sectionStartGlobal, sectionEndGlobal); i <= sectionHigher(sectionStartGlobal, sectionEndGlobal); i++) {
            final int section = i;
            db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB).document(trainNameDB).collection("car").document(Integer.toString(carNum)).collection("section").document(Integer.toString(section))
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                            checkReservation += 1;
                            if (checkReservation == sectionHigher(sectionStartGlobal, sectionEndGlobal) - sectionLower(sectionStartGlobal, sectionEndGlobal) + 2) {
                                myStartActivity(Ready2Activity.class);
                            }
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
                        checkReservation += 1;
                        if (checkReservation == sectionHigher(sectionStartGlobal, sectionEndGlobal) - sectionLower(sectionStartGlobal, sectionEndGlobal) + 2) {
                            myStartActivity(Ready2Activity.class);
                        }
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

        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
    }

    @Override
    public void onBackPressed() {
        myStartActivity(MainActivity.class);
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}