package com.example.subway_for_pregnant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ViewSeatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_seats);

        findViewById(R.id.button_State1).setOnClickListener(onClickListener);
        findViewById(R.id.button_State2).setOnClickListener(onClickListener);
        findViewById(R.id.button_State3).setOnClickListener(onClickListener);
        findViewById(R.id.button_State4).setOnClickListener(onClickListener);
        findViewById(R.id.button_State5).setOnClickListener(onClickListener);
        findViewById(R.id.button_State6).setOnClickListener(onClickListener);
        findViewById(R.id.button_State7).setOnClickListener(onClickListener);
        findViewById(R.id.button_State8).setOnClickListener(onClickListener);
        findViewById(R.id.button_State9).setOnClickListener(onClickListener);
        findViewById(R.id.button_State10).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_State1:
                    myStartActivity(Ready2Activity.class);
                    break;
                    //임시로 지정
                //나중에 DB에서 좌석 현황 불러올 것
                case R.id.button_State2:
                    startToast("이미 예약된 좌석입니다.");
                    break;
                case R.id.button_State3:
                    startToast("이미 예약된 좌석입니다.");
                    break;
                case R.id.button_State4:
                    startToast("이미 예약된 좌석입니다.");
                    break;
                case R.id.button_State5:
                    startToast("이미 예약된 좌석입니다.");
                    break;
                case R.id.button_State6:
                    startToast("이미 예약된 좌석입니다.");
                    break;
                case R.id.button_State7:
                    startToast("이미 예약된 좌석입니다.");
                    break;
                case R.id.button_State8:
                    startToast("이미 예약된 좌석입니다.");
                    break;
                case R.id.button_State9:
                    startToast("이미 예약된 좌석입니다.");
                    break;
                case R.id.button_State10:
                    startToast("이미 예약된 좌석입니다.");
                    break;
            }
        }
    };

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
