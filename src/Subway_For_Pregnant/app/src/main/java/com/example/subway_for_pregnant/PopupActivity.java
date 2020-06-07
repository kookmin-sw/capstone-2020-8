package com.example.subway_for_pregnant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;


public class PopupActivity extends Activity {


    TextView textView;
    String trainName;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_train_popupi);

        textView = (TextView) findViewById(R.id.txtText);


        //데이터 가져오기
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        trainName = intent.getStringExtra("trainName");
        textView.setText("현재 좌석 현황 : ");
        textView.append(data);


    }

    public void mOnOpen(View v){
        //데이터 전달하기

        myStartActivity(ViewSeatsActivity.class, trainName);

        //액티비티(팝업) 닫기
        finish();
    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }
    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }*/

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

    private void myStartActivity(Class c,String position) {
        Intent intent = getIntent();
        intent.getExtras();
        Intent intent2 = new Intent(this, c);
        intent2.putExtras(intent);
        intent2.putExtra("trainName", position);

        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
    }

}
