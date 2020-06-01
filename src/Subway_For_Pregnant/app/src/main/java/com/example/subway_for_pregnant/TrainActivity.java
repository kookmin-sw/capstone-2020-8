package com.example.subway_for_pregnant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

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

    int pastStationCount = 0;
    int transferCount = 0;
    int total_size = 0;
    List<String> train = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sample);

        TextView tv_sample = findViewById(R.id.textView_sample);
        final ListView listView = findViewById(R.id.listView);

        String laneInfo = "0";
        String driveInfo = "0";

        initIntents();

        String showResult = "";
        showResult += ("출발역: " + globalStartName + "\n도착역: " + globalEndName + "\n\n");
        int count = 0;
        for (int i = 0; i < driveInfoLength; i++) {
            showResult += ("<" + driveInfoLaneName[i] + ">\n");
            showResult += (driveInfoStationCount[i] + "개 역 이동\n");
            showResult += (stationsStartName[count] + "[" + stationsStartID[count] + "]");  //현재역 [현재역코드]
            for (int j = count; j < driveInfoStationCount[i] + count; j++) {
                showResult += (" -> " + stationsEndName[j] + "[" + stationsEndSID[j] + "] " + "(" + stationsTravelTime[j] + "분)");
                // -> 다음역 [다음역코드] (현 구간 소요 시간)
            }
            showResult += ("\n\n");
            count += driveInfoStationCount[i];
        }

        tv_sample.setText(showResult);

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

                                                                       Log.d(TAG,"task2 : "+task2);

                                                                       if (task2.isSuccessful()) {
                                                                           for (QueryDocumentSnapshot queryDocumentSnapshot2 : task2.getResult()) {

                                                                               final String cnt=queryDocumentSnapshot2.getId();

                                                                               Log.d(TAG, "count : " + cnt);
                                                                               Log.d(TAG, "경로 : " + laneInfoDB +" "+ driveInfoDB);

                                                                               db.collection("Demo_subway").document(laneInfoDB).collection(driveInfoDB).document(train.get(0)).collection("car").document(cnt).collection("section")
                                                                                       .get()
                                                                                       .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                           @Override
                                                                                           public void onComplete(@NonNull final Task<QuerySnapshot> task3) {
                                                                                               if (task3.isSuccessful()) {
                                                                                                   Log.d(TAG, "디비문 내용 : " + train.get(0) + " " + cnt);
                                                                                                   Log.d(TAG,"출발역 : "+stationsStartID[0]);
                                                                                                   Log.d(TAG,"도착역 : "+stationsEndSID[stationsLength-1]);
                                                                                                   for (QueryDocumentSnapshot queryDocumentSnapshot : task3.getResult()) {
                                                                                                       try {
                                                                                                           if(queryDocumentSnapshot.getData().get("s1_isReservation").equals(false) && stationsStartID[0] <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsEndSID[stationsLength-1]){
                                                                                                               total_size++;
                                                                                                           }
                                                                                                           if(queryDocumentSnapshot.getData().get("s2_isReservation").equals(false) && stationsStartID[0] <= Integer.parseInt(queryDocumentSnapshot.getId()) && Integer.parseInt(queryDocumentSnapshot.getId()) <= stationsEndSID[stationsLength-1]){
                                                                                                               total_size++;
                                                                                                           }
                                                                                                           Thread.sleep(100);
                                                                                                           Log.d(TAG, "사이즈 크기는 " + total_size);
                                                                                                       } catch (InterruptedException e) {
                                                                                                       }
                                                                                                   }
                                                                                               }
                                                                                               else{
                                                                                                   Log.d(TAG,"ERROR");
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
