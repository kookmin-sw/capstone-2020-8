package com.example.subway_for_pregnant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ODsayService odsayService;
    private JSONObject jsonObject;

    FirebaseUser user;
    String getReservationInfo;
    String reserveInfo[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (user == null) {
            myStartActivity3(LoginActivity.class);
        } else {
            db.collection("user").whereEqualTo("id", user.getEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    getReservationInfo = (String) document.getData().get("reservation_info");

                                    if (getReservationInfo.length() > 0) {
                                        reserveInfo = getReservationInfo.split(";");
                                        for (int i = 0; i < reserveInfo.length; i++) {
                                            Log.d(TAG, reserveInfo[i]);
                                        }
                                    }
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

        odsayService = ODsayService.init(MainActivity.this, getString(R.string.odsay_key));
        odsayService.setReadTimeout(5000);
        odsayService.setConnectionTimeout(5000);

        findViewById(R.id.logoutButton).setOnClickListener(onClickListener);
        findViewById(R.id.trainbutton).setOnClickListener(onClickListener);
        findViewById(R.id.bluetoothButton).setOnClickListener(onClickListener);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.logoutButton:
                    FirebaseAuth.getInstance().signOut();
                    myStartActivity3(MainActivity.class);
                    break;
                case R.id.trainbutton:
                    if (getReservationInfo.length() > 0) {
                        doFindSubway();
                    }
                    else {
                        myStartActivity(FindSubwayActivity.class);
                    }
                    break;
                case R.id.bluetoothButton:
                    myStartActivity(SampleBluetoothActivity.class);
                    break;
                /*case R.id.socketButton:
                    myStartActivity(NodeTestActivity.class);
                    break;*/
            }
        }
    };

    private OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
        @Override
        public void onSuccess(ODsayData oDsayData, API api) {
            jsonObject = oDsayData.getJson();
            myStartActivity2(Ready2Activity.class);
        }

        @Override
        public void onError(int i, String errorMessage, API api) {
            Log.d(TAG, "API : " + api.name() + "\n" + errorMessage);
        }
    };

    private void doFindSubway() {
        String sStationCode = reserveInfo[7];
        String eStationCode = reserveInfo[8];

        odsayService.requestSubwayPath("1000", sStationCode, eStationCode, "1", onResultCallbackListener);
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.putExtra("user", user.getEmail());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void myStartActivity2(Class c) {
        Intent intent2 = new Intent(this, c);
        intent2.putExtra("user", user.getEmail());

        try {
            intent2.putExtra("globalStartName", jsonObject.getJSONObject("result").getString("globalStartName"));
            intent2.putExtra("globalEndName", jsonObject.getJSONObject("result").getString("globalEndName"));
            intent2.putExtra("globalTravelTime", jsonObject.getJSONObject("result").getInt("globalTravelTime"));
            intent2.putExtra("globalDistance", jsonObject.getJSONObject("result").getInt("globalDistance"));
            intent2.putExtra("globalStationCount", jsonObject.getJSONObject("result").getInt("globalStationCount"));
            intent2.putExtra("fare", jsonObject.getJSONObject("result").getInt("fare"));
            intent2.putExtra("cashFare", jsonObject.getJSONObject("result").getInt("cashFare"));

            intent2.putExtra("driveInfoLength", jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").length());
            for (int i = 0; i < jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").length(); i++) {
                intent2.putExtra("driveInfoLaneID" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getString("laneID"));
                intent2.putExtra("driveInfoLaneName" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getString("laneName"));
                intent2.putExtra("driveInfoStartName" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getString("startName"));
                intent2.putExtra("driveInfoStationCount" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getInt("stationCount"));
                intent2.putExtra("driveInfoWayCode" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getInt("wayCode"));
                intent2.putExtra("driveInfoWayName" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getString("wayName"));
            }

            intent2.putExtra("stationsLength", jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").length());
            for (int i = 0; i < jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").length(); i++) {
                intent2.putExtra("stationsStartID" + i, jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").getJSONObject(i).getInt("startID"));
                intent2.putExtra("stationsStartName" + i, jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").getJSONObject(i).getString("startName"));
                intent2.putExtra("stationsEndSID" + i, jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").getJSONObject(i).getInt("endSID"));
                intent2.putExtra("stationsEndName" + i, jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").getJSONObject(i).getString("endName"));
                intent2.putExtra("stationsTravelTime" + i, jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").getJSONObject(i).getInt("travelTime"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            intent2.putExtra("exChangeInfoLength", jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").length());
            for (int i = 0; i < jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").length(); i++) {
                intent2.putExtra("exChangeInfoLaneName" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getString("laneName"));
                intent2.putExtra("exChangeInfoStartName" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getString("startName"));
                intent2.putExtra("exChangeInfoExName" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getString("exName"));
                intent2.putExtra("exChangeInfoExSID" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getInt("exSID"));
                intent2.putExtra("exChangeInfoFastTrain" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getInt("fastTrain"));
                intent2.putExtra("exChangeInfoFastDoor" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getInt("fastDoor"));
                intent2.putExtra("exChangeInfoExWalkTime" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getInt("exWalkTime"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
    }

    private void myStartActivity3(Class c) {
        Intent intent = new Intent(this, c);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
