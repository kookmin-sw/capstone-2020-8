package com.example.subway_for_pregnant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;


public class PopupActivity extends Activity {


    TextView textView;
    String trainName;
    int total_size = 0;
    int exchangeInfoLength;
    int stationsStartID;
    int stationsEndSID;
    boolean[] sheet1 = new boolean[6];
    boolean[] sheet2 = new boolean[6];

    private static final String TAG = "PopupActivity";
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    String laneInfoDB;
    String driveInfoDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_train_popupi);

        textView = (TextView) findViewById(R.id.txtText);
        textView.setText("로딩중...");

        Arrays.fill(sheet1, true);
        Arrays.fill(sheet2, true);


        //데이터 가져오기
        Intent intent = getIntent();
        //data = intent.getStringExtra("data");
        laneInfoDB = intent.getStringExtra("laneInfoDB");
        driveInfoDB = intent.getStringExtra("driveInfoDB");

        exchangeInfoLength = intent.getExtras().getInt("exChangeInfoLength");
        stationsStartID = intent.getExtras().getInt("stationsStartID" + 0);
        stationsEndSID = intent.getExtras().getInt("stationsEndSID");
        trainName = intent.getStringExtra("trainName");

        countThread th1 = new countThread();
        checkThread th2 = new checkThread();

        Log.d(TAG, "스레드 1 시작");
        th1.start();
        try{
            th1.join();
            Log.d(TAG, "스레드 1 조인");
        }catch (InterruptedException e){
        }
        Log.d(TAG, "스레드 2 시작");
        th2.start();

        //DB 값 불러오기 끝

    }

    private class checkThread extends Thread{
        public void run(){
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){
            }

            for(int i=0;i<6;i++){
                if(sheet1[i] == true){
                    total_size++;
                }
                if(sheet2[i]==true){
                    total_size++;
                }
            }

            textView.setText("현재 좌석 현황 : ");
            textView.append(Integer.toString(total_size));
        }
    }

    private class countThread extends Thread{
        public void run(){
            db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB).document(trainName).collection("car")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<QuerySnapshot> task2) {

                            Log.d(TAG, "task2 : " + task2);

                            if (task2.isSuccessful()) {
                                for (QueryDocumentSnapshot queryDocumentSnapshot2 : task2.getResult()) {

                                    final String cnt = queryDocumentSnapshot2.getId();

                                    Log.d(TAG, "count : " + cnt);
                                    Log.d(TAG, "경로 : " + laneInfoDB + " " + driveInfoDB);


                                    db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB).document(trainName).collection("car").document(cnt).collection("section")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull final Task<QuerySnapshot> task3) {
                                                    if (task3.isSuccessful()) {
                                                        Log.d(TAG, "디비문 내용 : " + trainName + " " + cnt);
                                                        Log.d(TAG, "출발역 : " + stationsStartID);
                                                        Log.d(TAG, "도착역 : " + stationsEndSID);
                                                        Log.d(TAG, "호선 : " + laneInfoDB);
                                                        Log.d(TAG, "상행 하행 구분 : " + driveInfoDB);

                                                        if (driveInfoDB.equals("Down")) {//하행


                                                            for (QueryDocumentSnapshot queryDocumentSnapshot : task3.getResult()) {

                                                                try {
                                                                    Log.d(TAG, queryDocumentSnapshot.getId());
                                                                    if (exchangeInfoLength > 0) { //환승을 하는 경우
                                                                        if (laneInfoDB.equals("line8")) {
                                                                            if (stationsStartID >= 815) {

                                                                                if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && 815 <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                    sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                                if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && 815 <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                    sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                            } else {

                                                                                if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= 815) {
                                                                                    sheet1[Integer.parseInt(cnt) - 1] = false;

                                                                                }

                                                                                if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= 815) {
                                                                                    sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                                    Log.d(TAG, "true 발견");
                                                                                    Log.d(TAG, String.valueOf(sheet2[Integer.parseInt(cnt) - 1]));
                                                                                }
                                                                            }
                                                                        }
                                                                        if (laneInfoDB.equals("line9")) {
                                                                            if (stationsStartID >= 933) {

                                                                                if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && 933 <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                    sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                                if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && 933 <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                    sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                            } else {

                                                                                if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= 933) {
                                                                                    sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                                if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= 933) {
                                                                                    sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                            }
                                                                        }

                                                                    } else {//환승을 안하는 경우

                                                                        if (laneInfoDB.equals("line8")) {

                                                                            if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsEndSID) {
                                                                                sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                            }
                                                                            if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsEndSID) {
                                                                                sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                            }
                                                                        }

                                                                        if (laneInfoDB.equals("line9")) {
                                                                            if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && stationsEndSID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                            }
                                                                            if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && stationsEndSID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                            }
                                                                        }
                                                                    }
                                                                    Thread.sleep(10);

                                                                    Log.d(TAG, "사이즈 크기가 미리 측정되었는가 ? => " + total_size);

                                                                } catch (InterruptedException e) {
                                                                    Log.d(TAG, "error" + e);
                                                                }

                                                            }
                                                        }

                                                        if (driveInfoDB.equals("Up")) {//상행
                                                            for (QueryDocumentSnapshot queryDocumentSnapshot : task3.getResult()) {
                                                                try {
                                                                    if (exchangeInfoLength > 0) { //환승을 하는 경우
                                                                        if (laneInfoDB.equals("line8")) {
                                                                            if (815 >= stationsStartID) {
                                                                                if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= 815) {
                                                                                    sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                                if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= 815) {
                                                                                    sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                            } else {
                                                                                if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && 815 <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                    sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                                if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && 815 <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                    sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                            }
                                                                        }

                                                                        if (laneInfoDB.equals("line9")) {
                                                                            if (stationsStartID >= 933) {
                                                                                if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= 933) {
                                                                                    sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                                if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= 933) {
                                                                                    sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                            } else {
                                                                                if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && 933 <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                    sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                                if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && 933 <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                    sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                                }
                                                                            }
                                                                        }
                                                                    } else {//환승을 안하는 경우
                                                                        if (laneInfoDB.equals("line8")) {
                                                                            if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && stationsEndSID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                            }
                                                                            if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && stationsEndSID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsStartID) {
                                                                                sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                            }
                                                                        }
                                                                        if (laneInfoDB.equals("line9")) {
                                                                            if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsEndSID) {
                                                                                sheet1[Integer.parseInt(cnt) - 1] = false;
                                                                            }
                                                                            if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(true) && stationsStartID <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsEndSID) {
                                                                                sheet2[Integer.parseInt(cnt) - 1] = false;
                                                                            }
                                                                        }

                                                                    }

                                                                    Thread.sleep(10);

                                                                    Log.d(TAG, "사이즈 크기는 " + total_size);
                                                                } catch (InterruptedException e) {
                                                                    Log.d(TAG, "error" + e);
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        Log.d(TAG, "ERROR");
                                                    }

                                                }
                                            });
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {

                                    }

                                }


                            }

                        }
                    });
        }
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
