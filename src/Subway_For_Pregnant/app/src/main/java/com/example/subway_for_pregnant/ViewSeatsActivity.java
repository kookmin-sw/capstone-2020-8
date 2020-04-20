package com.example.subway_for_pregnant;

import android.os.Bundle;
import android.view.View;

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
                    break;
                case R.id.button_State2:
                    break;
                case R.id.button_State3:
                    break;
                case R.id.button_State4:
                    break;
                case R.id.button_State5:
                    break;
                case R.id.button_State6:
                    break;
                case R.id.button_State7:
                    break;
                case R.id.button_State8:
                    break;
                case R.id.button_State9:
                    break;
                case R.id.button_State10:
                    break;
            }
        }
    };
}
