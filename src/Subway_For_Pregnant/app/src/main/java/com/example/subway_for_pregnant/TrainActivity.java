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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TrainActivity extends AppCompatActivity {

    private static final String TAG = "TrainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample);

        TextView tv_sample = findViewById(R.id.textView_sample);

        Intent intent = getIntent();

        String globalStartName = intent.getExtras().getString("globalStartName");
        String globalEndName = intent.getExtras().getString("globalEndName");
        int driveInfoLength = intent.getExtras().getInt("driveInfoLength");
        int[] driveInfoStationCount = new int[driveInfoLength];
        String[] driveInfoLaneName = new String[driveInfoLength];
        int stationsLength = intent.getExtras().getInt("stationsLength");
        String[] stationsStartName = new String[stationsLength];
        String[] stationsEndName = new String[stationsLength];
        int[] stationsTravelTime = new int[stationsLength];

        for (int i = 0; i < driveInfoLength; i++) {
            driveInfoStationCount[i] = intent.getExtras().getInt("driveInfoStationCount" + i);
            driveInfoLaneName[i] = intent.getExtras().getString("driveInfoLaneName" + i);
        }

        for (int i = 0; i < stationsLength; i++) {
            stationsStartName[i] = intent.getExtras().getString("stationsStartName" + i);
            stationsEndName[i] = intent.getExtras().getString("stationsEndName" + i);
            stationsTravelTime[i] = intent.getExtras().getInt("stationsTravelTime" + i);
        }

        String showResult = "";
        showResult += ("출발역: " + globalStartName + "\n도착역: " + globalEndName + "\n\n");
        int count = 0;
        for (int i = 0; i < driveInfoLength; i++) {
            showResult += ("<" + driveInfoLaneName[i] + ">\n");
            showResult += (driveInfoStationCount[i] + "개 역 이동\n");
            for (int j = count; j < driveInfoStationCount[i] + count; j++) {
                showResult += (stationsStartName[j] + " -> ");
            }
            showResult += (stationsEndName[driveInfoStationCount[i] + count - 1] + "\n\n");
            count = driveInfoStationCount[i];
        }

        tv_sample.setText(showResult);

        final ListView listView = findViewById(R.id.listView);


        //DB 선언
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference documentReference = db.collection("subway").document("static").collection("10car");

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });



        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                new String[] {"4998번","4999번","5000번"}));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), position+" 번째 값 : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                myStartActivity(ViewSeatsActivity.class);

            }
        });
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

}

