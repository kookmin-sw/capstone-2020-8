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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TrainActivity extends AppCompatActivity {

    private static final String TAG = "TrainActivity";
    final FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sample);

        TextView tv_sample = findViewById(R.id.textView_sample);
        final ListView listView = findViewById(R.id.listView);

        Intent intent = getIntent();

        String globalStartName = intent.getExtras().getString("globalStartName");
        String globalEndName = intent.getExtras().getString("globalEndName");
        int driveInfoLength = intent.getExtras().getInt("driveInfoLength");
        int[] driveInfoStationCount = new int[driveInfoLength];
        String[] driveInfoLaneName = new String[driveInfoLength];
        int stationsLength = intent.getExtras().getInt("stationsLength");
        String[] stationsStartName = new String[stationsLength];
        int[] stationsStartID = new int[stationsLength];
        String[] stationsEndName = new String[stationsLength];
        int[] stationsEndSID = new int[stationsLength];
        int[] stationsTravelTime = new int[stationsLength];

        for (int i = 0; i < driveInfoLength; i++) {
            driveInfoStationCount[i] = intent.getExtras().getInt("driveInfoStationCount" + i);
            driveInfoLaneName[i] = intent.getExtras().getString("driveInfoLaneName" + i);
        }

        for (int i = 0; i < stationsLength; i++) {
            stationsStartName[i] = intent.getExtras().getString("stationsStartName" + i);
            stationsStartID[i] = intent.getExtras().getInt("stationsStartID" + i);
            stationsEndName[i] = intent.getExtras().getString("stationsEndName" + i);
            stationsEndSID[i] = intent.getExtras().getInt("stationsEndSID" + i);
            stationsTravelTime[i] = intent.getExtras().getInt("stationsTravelTime" + i);
        }

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


            db.collection("Demo_subway").document("line8").collection("Up")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<String> train = new ArrayList<String>();

                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                    train.add(queryDocumentSnapshot.getId());
                                    Log.d(TAG, queryDocumentSnapshot.getId() + " => " + queryDocumentSnapshot.getData());

                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }

                            listView.setAdapter(new ArrayAdapter<String>(TrainActivity.this, android.R.layout.simple_list_item_1, train));

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Toast.makeText(getApplicationContext(), position + " 번째 값 : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                                    myStartActivity(ViewSeatsActivity.class, parent.getItemAtPosition(position).toString());
                                }
                            });
                        }
                    });
        }

    private void myStartActivity(Class c, String position) {

        Intent intent = getIntent();
        intent.getExtras();
        Intent intent2 = new Intent(this, c);
        intent2.putExtras(intent);
        intent2.putExtra("train_number",position);
        startActivity(intent2);
    }

}

