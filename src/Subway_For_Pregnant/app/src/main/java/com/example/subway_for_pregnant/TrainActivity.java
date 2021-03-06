package com.example.subway_for_pregnant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import android.content.Context;
import android.graphics.Paint;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class TrainActivity extends AppCompatActivity {

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
    int exchangeInfoLength;
    int[] exChangeInfoExSID;
    int[] exChangeInfoFastTrain;
    int[] exChangeInfoFastDoor;

    int pastStationCount = 0;
    int transferCount = 0;
    List<String> train = new ArrayList<>();

    String showResult1;
    String showResult2;
    int buttonMode = 1;

    int[] imgs = {R.drawable.node_icon, R.drawable.ic_more_vert_black_24dp};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.sample);

        TextView tv_sample = findViewById(R.id.textView_sample);
        Button bt_moreStations = findViewById(R.id.button_moreStations);
        final ListView listView = findViewById(R.id.listView);

        tv_sample.setMovementMethod(new ScrollingMovementMethod());

        String laneInfo = "0";
        String driveInfo = "0";

        initIntents();

        showResult1 = "";
        showResult1 += ("출발역: " + globalStartName + "\n도착역: " + globalEndName + "\n\n");
        showResult2 = "";
        showResult2 += ("출발역: " + globalStartName + "\n도착역: " + globalEndName + "\n\n");

        int count = 0;
        for (int i = 0; i < driveInfoLength; i++) {
            showResult1 += ("<" + driveInfoLaneName[i] + ">\n");
            showResult2 += ("<" + driveInfoLaneName[i] + ">\n");

            //tv_sample.setCompoundDrawablesWithIntrinsicBounds(R.drawable.node_icon,0,0,0);
            showResult1 += ("● " + stationsStartName[count] + "\n" + "↓" + "\n");  //현재역
            //tv_sample.setCompoundDrawablesWithIntrinsicBounds(R.drawable.node_icon,0,0,0);
            showResult2 += ("● " + stationsStartName[count] + "\n" + "↓" + "\n");  //현재역

            //tv_sample.setCompoundDrawablesWithIntrinsicBounds(R.drawable.node_icon,0,0,0);
            showResult1 += ("● " + stationsEndName[driveInfoStationCount[i] + count - 1] + "\n");

            for (int j = count; j < driveInfoStationCount[i] + count; j++) {
                //tv_sample.setCompoundDrawablesWithIntrinsicBounds(R.drawable.node_icon,0,0,0);
                showResult2 += ("● " + stationsEndName[j] + "(" + stationsTravelTime[j] + "분)\n" + "↓" + "\n");
                //다음역 (현 구간 소요 시간)
            }

            showResult1 += (driveInfoStationCount[i] + "개 역 이동\n\n");
            showResult2 += (driveInfoStationCount[i] + "개 역 이동\n\n");

            if (driveInfoLength > 1 && i < driveInfoLength - 1) {
                showResult1 += ("== " + "빠른 환승: " + exChangeInfoFastTrain[i] + "-" + exChangeInfoFastDoor[i] + " ==\n");
                showResult2 += ("== " + "빠른 환승: " + exChangeInfoFastTrain[i] + "-" + exChangeInfoFastDoor[i] + " ==\n");
            }

            showResult1 += ("\n");
            showResult2 += ("\n");
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

                    }
                });

        bt_moreStations.setOnClickListener(onClickListener);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), position + " 번째 값 : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                Intent intent1 = new Intent(parent.getContext(), PopupActivity.class);
                //intent1.putExtra("data",Integer.toString(total_size));

                intent1.putExtras(intent);

                intent1.putExtra("trainName", parent.getItemAtPosition(position).toString());
                Log.d(TAG,parent.getItemAtPosition(position).toString());
                intent1.putExtra("laneInfoDB", laneInfoDB);
                intent1.putExtra("driveInfoDB", driveInfoDB);
                intent1.putExtra("stationsEndSID", stationsEndSID[stationsLength - 1]);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivityForResult(intent1, 1);
                //myStartActivity(ViewSeatsActivity.class, parent.getItemAtPosition(position).toString());
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

                        //tv_sample.setCompoundDrawablesWithIntrinsicBounds(R.drawable.node_icon,0,0,0);
                        tv_sample.setText(showResult2);
                        bt_moreSt.setText("간단히");
                        buttonMode = 2;
                    }
                    else {

                        //tv_sample.setCompoundDrawablesWithIntrinsicBounds(R.drawable.node_icon,0,0,0);
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
        exchangeInfoLength = intent.getExtras().getInt("exChangeInfoLength");
        if (exchangeInfoLength > 0) {
            exChangeInfoExSID = new int[exchangeInfoLength];
            exChangeInfoFastTrain = new int[exchangeInfoLength];
            exChangeInfoFastDoor = new int[exchangeInfoLength];
        }

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

        for (int i = 0; i < exchangeInfoLength; i++) {
            exChangeInfoExSID[i] = intent.getExtras().getInt("exChangeInfoExSID" + i);
            exChangeInfoFastTrain[i] = intent.getExtras().getInt("exChangeInfoFastTrain" + i);
            exChangeInfoFastDoor[i] = intent.getExtras().getInt("exChangeInfoFastDoor" + i);
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

    /*
    private void myStartActivity(Class c, String position) {
        Intent intent = getIntent();
        intent.getExtras();
        Intent intent2 = new Intent(this, c);
        intent2.putExtras(intent);
        intent2.putExtra("trainName", position);

        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
    }*/

    private void myStartActivity2(Class c) {
        Intent intent = new Intent(this, c);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onBackPressed() {
        myStartActivity2(MainActivity.class);
    }

}
