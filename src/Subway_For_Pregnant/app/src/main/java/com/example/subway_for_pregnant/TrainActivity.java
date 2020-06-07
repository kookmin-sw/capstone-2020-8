package com.example.subway_for_pregnant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class TrainActivity extends AppCompatActivity {

    static public int ORIENTATION_HORIZONTAL = 0;
    static public int ORIENTATION_VERTICAL = 1;
    private Paint mPaint;
    private int orientation;

    private static final String TAG = "TrainActivity";
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    String globalStartName;
    String globalEndName;
    int driveInfoLength;
    int[] driveInfoStationCount;
    int[] driveInfoWayCode;
    String[] driveInfoLaneName;
    int stationsLength;
    String[] stationsStartName;
    int[] stationsStartID;
    String[] stationsEndName;
    int[] stationsEndSID;
    int[] stationsTravelTime;

    int pastStationCount = 0;
    int transferCount = 0;
    int total_size = 0;
    List<String> train = new ArrayList<>();

    String showResult1;
    String showResult2;
    int buttonMode = 1;

    int[] imgs = {R.drawable.node_icon, R.drawable.ic_more_vert_black_24dp};


    Context context;
    AttributeSet attrs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.sample);

        TextView tv_sample = findViewById(R.id.textView_sample);
        final ListView listView = findViewById(R.id.listView);

        String laneInfo = "0";
        String driveInfo = "0";

        initIntents();

        ImageView image=new ImageView(this);

        showResult1 = "";
        showResult1 += ("출발역: " + globalStartName + "\n도착역: " + globalEndName + "\n\n");
        showResult2 = "";
        showResult2 += ("출발역: " + globalStartName + "\n도착역: " + globalEndName + "\n\n");

        int count = 0;
        for (int i = 0; i < driveInfoLength; i++) {
            showResult1 += ("<" + driveInfoLaneName[i] + ">\n");
            showResult2 += ("<" + driveInfoLaneName[i] + ">\n");

            //image.setImageResource(R.drawable.node_icon);
            System.out.print(imgs[0]);
            showResult1 += (stationsStartName[count] + "\n");  //현재역
            //image.setImageResource(R.drawable.ic_more_vert_black_24dp);
            System.out.print(imgs[1]);
            System.out.println("\n");

            //image.setImageResource(R.drawable.node_icon);
            System.out.print(imgs[0]);
            showResult2 += (stationsStartName[count] + "\n");  //현재역
            //image.setImageResource(R.drawable.ic_more_vert_black_24dp);
            System.out.print(imgs[1]);
            System.out.println("\n");

            showResult1 += (stationsEndName[driveInfoStationCount[i] + count - 1] + "\n");
            //showResult1 += (stationsEndName[driveInfoStationCount[i] + count - 1] + "(" + stationsTravelTime[driveInfoStationCount[i] + count - 1] + "분)\n");
            for (int j = count; j < driveInfoStationCount[i] + count; j++) {
                //image.setImageResource(R.drawable.node_icon);
                System.out.print(imgs[0]);
                showResult2 += (stationsEndName[j] + "(" + stationsTravelTime[j] + "분)\n");
                //image.setImageResource(R.drawable.ic_more_vert_black_24dp);
                System.out.print(imgs[1]);
                System.out.println("\n");
                //다음역 (현 구간 소요 시간)
            }
            showResult1 += (driveInfoStationCount[i] + "개 역 이동\n");
            showResult2 += (driveInfoStationCount[i] + "개 역 이동\n");
            showResult1 += ("\n\n");
            showResult2 += ("\n\n");
            count += driveInfoStationCount[i];
        }

        tv_sample.setText(showResult1);

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

        Intent intent = getIntent();

        final int stationsLength = intent.getExtras().getInt("stationsLength");   //역 개수. 즉 stations 라고 앞에 붙은 데이터들의 Length.


        db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                train.add(queryDocumentSnapshot.getId());
                                Log.d(TAG, queryDocumentSnapshot.getId() + " => 선택");
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        listView.setAdapter(new ArrayAdapter<String>(TrainActivity.this, android.R.layout.simple_list_item_1, train));

                        db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB).document(train.get(0)).collection("car")
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

                                                db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB).document(train.get(0)).collection("car").document(cnt).collection("section")
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull final Task<QuerySnapshot> task3) {
                                                                if (task3.isSuccessful()) {
                                                                    Log.d(TAG, "디비문 내용 : " + train.get(0) + " " + cnt);
                                                                    Log.d(TAG, "출발역 : " + stationsStartID[0]);
                                                                    Log.d(TAG, "도착역 : " + stationsEndSID[stationsLength - 1]);
                                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task3.getResult()) {
                                                                        try {
                                                                            if (queryDocumentSnapshot.getData().get("s1_isReservation").equals(false) && stationsStartID[0] <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsEndSID[stationsLength - 1]) {
                                                                                total_size++;
                                                                            }
                                                                            if (queryDocumentSnapshot.getData().get("s2_isReservation").equals(false) && stationsStartID[0] <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsEndSID[stationsLength - 1]) {
                                                                                total_size++;
                                                                            }
                                                                            Thread.sleep(100);
                                                                            Log.d(TAG, "사이즈 크기는 " + total_size);
                                                                        } catch (InterruptedException e) {
                                                                        }

                                                                    }
                                                                } else {
                                                                    Log.d(TAG, "ERROR");
                                                                }
                                                            }
                                                        });
                                                try {
                                                    Thread.sleep(1000);
                                                } catch (InterruptedException e) {
                                                }
                                            }
                                        }
                                    }
                                });
                    }
                });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), position + " 번째 값 : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                myStartActivity(ViewSeatsActivity.class, parent.getItemAtPosition(position).toString());
            }
        });


    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            TextView tv_sample = findViewById(R.id.textView_sample);
            Button bt_moreSt = findViewById(R.id.button_moreStations);

            switch (v.getId()) {
                case R.id.button_moreStations:
                    if (buttonMode == 1) {
                        tv_sample.setText(showResult2);
                        bt_moreSt.setText("간단히");
                        buttonMode = 2;
                    }
                    else {
                        tv_sample.setText(showResult1);
                        bt_moreSt.setText("자세히");
                        buttonMode = 1;
                    }
                    break;
                default:
                    break;
            }

        }
    };

    private void initIntents() {
        Intent intent = getIntent();

        globalStartName = intent.getExtras().getString("globalStartName");
        globalEndName = intent.getExtras().getString("globalEndName");
        driveInfoLength = intent.getExtras().getInt("driveInfoLength");
        driveInfoStationCount = new int[driveInfoLength];
        driveInfoWayCode = new int[driveInfoLength];
        driveInfoLaneName = new String[driveInfoLength];
        stationsLength = intent.getExtras().getInt("stationsLength");
        stationsStartName = new String[stationsLength];
        stationsStartID = new int[stationsLength];
        stationsEndName = new String[stationsLength];
        stationsEndSID = new int[stationsLength];
        stationsTravelTime = new int[stationsLength];

        for (int i = 0; i < driveInfoLength; i++) {
            driveInfoStationCount[i] = intent.getExtras().getInt("driveInfoStationCount" + i);
            driveInfoWayCode[i] = intent.getExtras().getInt("driveInfoWayCode" + i);
            driveInfoLaneName[i] = intent.getExtras().getString("driveInfoLaneName" + i);
        }

        for (int i = 0; i < stationsLength; i++) {
            stationsStartName[i] = intent.getExtras().getString("stationsStartName" + i);
            stationsStartID[i] = intent.getExtras().getInt("stationsStartID" + i);
            stationsEndName[i] = intent.getExtras().getString("stationsEndName" + i);
            stationsEndSID[i] = intent.getExtras().getInt("stationsEndSID" + i);
            stationsTravelTime[i] = intent.getExtras().getInt("stationsTravelTime" + i);
        }

        try {
            pastStationCount = intent.getExtras().getInt("pastStationCount");
            transferCount = intent.getExtras().getInt("transferCount");
        }
        catch (NullPointerException e) {
            pastStationCount = 0;
            transferCount = 0;
        }
    }

    private void myStartActivity(Class c, String position) {
        Intent intent = getIntent();
        intent.getExtras();
        Intent intent2 = new Intent(this, c);
        intent2.putExtras(intent);
        intent2.putExtra("trainName", position);

        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
    }


}
