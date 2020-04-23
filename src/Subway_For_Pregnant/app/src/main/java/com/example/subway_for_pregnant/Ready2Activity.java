package com.example.subway_for_pregnant;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class Ready2Activity extends AppCompatActivity {

    private static final String TAG = "Ready2Activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);
        findViewById(R.id.backward_button);
        Log.d(TAG, "Ready");

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backward_button:
                    break;
            }
        }
    };
}


