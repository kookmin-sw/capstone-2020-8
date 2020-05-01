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
<<<<<<< HEAD
=======
    private static final String TAG = "VS";
>>>>>>> f97dd7d76a87b0e32539d0940d6903889cdccd24
    int seat1_State = -1, seat2_State = -1;  //지금은 임시로 지정해둠. 0 = 예약가능, 1 = 일반인 사용중, 2 = 예약 불가.

    int stationsLength;         //역 개수. 즉 stations 라고 앞에 붙은 데이터들의 Length.
    String[] stationsStartName; //구간마다 현재역 이름
    int[] stationsStartID;      //구간마다 현재역 코드
    String[] stationsEndName;   //구간마다 다음역 이름
    int[] stationsEndSID;       //구간마다 다음역 코드

    int driveInfoLength;        //노선 개수. 환승 없으면 1, 1번 환승은 2. 이런식으로.
    int[] driveInfoWayCode;     //방면 코드 (1:상행, 2:하행)
<<<<<<< HEAD
=======
    String[] driveInfoLaneName;  //노선 이름. 예: "8호선", "분당선".
>>>>>>> f97dd7d76a87b0e32539d0940d6903889cdccd24

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_seats);

        Intent intent = getIntent();
        //인텐트 호출
        stationsLength = intent.getExtras().getInt("stationsLength");   //역 개수. 즉 stations 라고 앞에 붙은 데이터들의 Length.
        stationsStartName = new String[stationsLength];    //구간마다 현재역 이름
        stationsStartID = new int[stationsLength];            //구간마다 현재역 코드
        stationsEndName = new String[stationsLength];      //구간마다 다음역 이름
        stationsEndSID = new int[stationsLength];             //구간마다 다음역 코드

        driveInfoLength = intent.getExtras().getInt("driveInfoLength");     //노선 개수. 환승 없으면 1, 1번 환승은 2. 이런식으로.
        driveInfoWayCode = new int[driveInfoLength];      //방면 코드 (1:상행, 2:하행)
<<<<<<< HEAD
=======
        driveInfoLaneName = new String[driveInfoLength];

>>>>>>> f97dd7d76a87b0e32539d0940d6903889cdccd24

        for (int i = 0; i < stationsLength; i++) {
            stationsStartName[i] = intent.getExtras().getString("stationsStartName" + i);
            stationsStartID[i] = intent.getExtras().getInt("stationsStartID" + i);
            stationsEndName[i] = intent.getExtras().getString("stationsEndName" + i);
            stationsEndSID[i] = intent.getExtras().getInt("stationsEndSID" + i);
            //stationsTravelTime[i] = intent.getExtras().getInt("stationsTravelTime" + i);
        }

        for (int i = 0; i < driveInfoLength; i++) {
<<<<<<< HEAD
            driveInfoWayCode[i] = intent.getExtras().getInt("driveInfoWayCode");
=======
            driveInfoLaneName[i] = intent.getExtras().getString("driveInfoLaneName" + i);
            driveInfoWayCode[i] = intent.getExtras().getInt("driveInfoWayCode" + i);
>>>>>>> f97dd7d76a87b0e32539d0940d6903889cdccd24
        }

        TextView station = findViewById(R.id.textView_Station);  //인텐트 값 잘 받아오는지 확인하기 위한 임시 TextView.
        station.setText(stationsStartName[0]);

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
<<<<<<< HEAD
        public void onClick(View v) {
            TextView tv_State;
            int numOfCars;

            switch (v.getId()){
                //button_State들은 열차 칸 선택 버튼.
                case R.id.button_StateLeft:
                    tv_State = findViewById(R.id.textView_StateLeft);
                    numOfCars = Integer.parseInt((String) tv_State.getText());  //열차 칸 번호. int형 변수.

                    //여기에 나중에 DB에서 좌석 현황 불러올 것.
=======
        public void onClick(final View v) {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            TextView tv_State;
            String numOfCars;
            String driveInfo = "1";
            String laneInfo = "1";
            Log.d(TAG, "driveInfoWayCode : " + driveInfoWayCode[0] + ", driveInfoLaneName : " + driveInfoLaneName[0]);

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
            Log.d(TAG, "diveInfo : " + driveInfo + ", laneInfo : " + laneInfo);
            CollectionReference colRef = db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car");

            switch (v.getId()) {
                //button_State들은 열차 칸 선택 버튼.
                case R.id.button_StateLeft:
                    tv_State = findViewById(R.id.textView_StateLeft);
                    numOfCars = (String) tv_State.getText();  //열차 칸 번호.
>>>>>>> f97dd7d76a87b0e32539d0940d6903889cdccd24

                    checkSeats(v);
                    //불러온 데이터로 좌석 버튼 2개 색상 변경. 현재는 색상 변경 임의로 지정.
                    //DB에서 값을 받아올 수 있게 되면 v.getId()가 아니라 DB값으로 바꿀 예정. checkSeats() 함수도 수정 예정.
                    break;
                case R.id.button_State5:
                    tv_State = findViewById(R.id.textView_State5);
<<<<<<< HEAD
                    numOfCars = Integer.parseInt((String) tv_State.getText());
=======
                    numOfCars = (String) tv_State.getText();
>>>>>>> f97dd7d76a87b0e32539d0940d6903889cdccd24

                    checkSeats(v);
                    break;
                case R.id.button_State4:
                    tv_State = findViewById(R.id.textView_State4);
<<<<<<< HEAD
                    numOfCars = Integer.parseInt((String) tv_State.getText());
=======
                    numOfCars = (String) tv_State.getText();
>>>>>>> f97dd7d76a87b0e32539d0940d6903889cdccd24

                    checkSeats(v);
                    break;
                case R.id.button_State3:
                    tv_State = findViewById(R.id.textView_State3);
<<<<<<< HEAD
                    numOfCars = Integer.parseInt((String) tv_State.getText());
=======
                    numOfCars = (String) tv_State.getText();
>>>>>>> f97dd7d76a87b0e32539d0940d6903889cdccd24

                    checkSeats(v);
                    break;
                case R.id.button_State2:
                    tv_State = findViewById(R.id.textView_State2);
<<<<<<< HEAD
                    numOfCars = Integer.parseInt((String) tv_State.getText());
=======
                    numOfCars = (String) tv_State.getText();
>>>>>>> f97dd7d76a87b0e32539d0940d6903889cdccd24

                    checkSeats(v);
                    break;
                case R.id.button_StateRight:
                    tv_State = findViewById(R.id.textView_StateRight);
<<<<<<< HEAD
                    numOfCars = Integer.parseInt((String) tv_State.getText());

                    checkSeats(v);
=======
                    numOfCars = (String) tv_State.getText();

                    db.collection("Demo_subway").document(laneInfo).collection(driveInfo).document("2101").collection("car").document(numOfCars)
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
>>>>>>> f97dd7d76a87b0e32539d0940d6903889cdccd24
                    break;

                //button_Seat1~2 는 좌석 선택 버튼.
                case R.id.button_Seat1:
                    if (seat1_State == 0) {
                        myStartActivity(Ready2Activity.class);
                    } else if (seat1_State == 1) {
                        startToast("앉아있는 일반인이 있습니다.");
                    } else if (seat1_State == 2) {
                        startToast("이미 예약되거나 다른 산모가 사용 중인 좌석입니다.");
                    } else {
                        startToast("에러입니다.");
                    }
                    break;
                case R.id.button_Seat2:
                    if (seat2_State == 0) {
                        myStartActivity(Ready2Activity.class);
                    } else if (seat2_State == 1) {
                        startToast("앉아있는 일반인이 있습니다.");
                    } else if (seat2_State == 2) {
                        startToast("이미 예약되거나 다른 산모가 사용 중인 좌석입니다.");
                    } else {
                        startToast("에러입니다.");
                    }
                    break;
            }
        }
    };

    private void checkSeats(View v) {
        Button btn = (Button) findViewById(v.getId());
        int emptySeats = Integer.parseInt(btn.getText().toString());

        //임의로 색상 변경 설정. DB에서 데이터를 받으면 여기 if문 조건을 변경해야함.
        if (emptySeats == 2) {
            startToast(emptySeats + "개의 좌석이 비어있습니다.");
            seat1_State = 0;
            seat2_State = 0;
            setBtnColor(R.id.button_Seat1, seat1_State);
            setBtnColor(R.id.button_Seat2, seat2_State);
        } else if (emptySeats == 1) {
            startToast(emptySeats + "개의 좌석이 비어있습니다.");
            seat1_State = 2;
            seat2_State = 0;
            setBtnColor(R.id.button_Seat1, seat1_State);
            setBtnColor(R.id.button_Seat2, seat2_State);
        } else {
            startToast("좌석 데이터 오류입니다.");
            seat1_State = -1;
            seat2_State = -1;
            setBtnColor(R.id.button_Seat1, seat1_State);
            setBtnColor(R.id.button_Seat2, seat2_State);
        }
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