package com.example.subway_for_pregnant;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ViewSeatsActivity extends AppCompatActivity {
    int seat1_State = -1, seat2_State = -1;  //0 = 예약가능, 1 = 일반인 사용중, 2 = 예약 불가.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_seats);

        //인텐트 호출
        Intent intent = getIntent();
        int stationsLength = intent.getExtras().getInt("stationsLength");   //역 개수. 즉 stations 라고 앞에 붙은 데이터들의 Length.
        String[] stationsStartName = new String[stationsLength];    //구간마다 현재역 이름
        int[] stationsStartID = new int[stationsLength];            //구간마다 현재역 코드
        String[] stationsEndName = new String[stationsLength];      //구간마다 다음역 이름
        int[] stationsEndSID = new int[stationsLength];             //구간마다 다음역 코드

        for (int i = 0; i < stationsLength; i++) {
            stationsStartName[i] = intent.getExtras().getString("stationsStartName" + i);
            stationsStartID[i] = intent.getExtras().getInt("stationsStartID" + i);
            stationsEndName[i] = intent.getExtras().getString("stationsEndName" + i);
            stationsEndSID[i] = intent.getExtras().getInt("stationsEndSID" + i);
            //stationsTravelTime[i] = intent.getExtras().getInt("stationsTravelTime" + i);
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
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_StateLeft:
                    //나중에 DB에서 좌석 현황 불러올 것.
                    checkSeats(v);  //불러온 데이터로 좌석 버튼 2개 색상 변경. 현재는 색상 변경 임의로 지정.
                    break;
                case R.id.button_State5:
                    checkSeats(v);
                    break;
                case R.id.button_State4:
                    checkSeats(v);
                    break;
                case R.id.button_State3:
                    checkSeats(v);
                    break;
                case R.id.button_State2:
                    checkSeats(v);
                    break;
                case R.id.button_StateRight:
                    checkSeats(v);
                    break;

                case R.id.button_Seat1:
                    if (seat1_State == 0) {
                        myStartActivity(Ready2Activity.class);
                    }
                    else if (seat1_State == 1) {
                        startToast("앉아있는 일반인이 있습니다.");
                    }
                    else if (seat1_State == 2) {
                        startToast("이미 예약되거나 다른 산모가 사용 중인 좌석입니다.");
                    }
                    else {
                        startToast("에러입니다.");
                    }
                    break;
                case R.id.button_Seat2:
                    if (seat2_State == 0) {
                        myStartActivity(Ready2Activity.class);
                    }
                    else if (seat2_State == 1) {
                        startToast("앉아있는 일반인이 있습니다.");
                    }
                    else if (seat2_State == 2) {
                        startToast("이미 예약되거나 다른 산모가 사용 중인 좌석입니다.");
                    }
                    else {
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
        }
        else if (emptySeats == 1) {
            startToast(emptySeats + "개의 좌석이 비어있습니다.");
            seat1_State = 2;
            seat2_State = 0;
            setBtnColor(R.id.button_Seat1, seat1_State);
            setBtnColor(R.id.button_Seat2, seat2_State);
        }
        else {
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
        }
        else if (check == 1) {
            findViewById(btnId).setBackgroundColor(Color.parseColor("#FFA500")); //Gold
        }
        else if (check == 2) {
            findViewById(btnId).setBackgroundColor(Color.parseColor("#FF0000")); //Red
        }
        else {
            findViewById(btnId).setBackgroundColor(Color.parseColor("#545454")); //Dark Gray
        }
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
