package com.example.subway_for_pregnant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

public class FindSubwayActivity extends AppCompatActivity {

    private static final String TAG = "FindSubwayActivity";

    private MenuItem itemHistory[];
    private DrawerLayout drawerLayout2;

    private EditText et_sid;
    private EditText et_eid;
    private Button bt_select;

    String user = "";
    String getReservationInfo;
    String reserveInfo[];
    String historyString;
    String history[];
    String historyStringNew = "";
    String historyTitle = "";

    String globalHistoryStart = "";
    String globalHistoryEnd = "";

    String globalStartStation = "";
    String globalEndStation = "";

    boolean isLoadComplete = false;

    private ODsayService odsayService;
    private JSONObject jsonObject;

    private static List<String[]> locationList = new ArrayList<String[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findsubway);
        Intent intent = getIntent();
        init();

        Toolbar toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        toolbar2.setTitle(R.string.app_name);
        setSupportActionBar(toolbar2);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.list);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
        View v = navigationView.getHeaderView(0);
        TextView tv_showID = (TextView) v.findViewById(R.id.textView_showID);
        Menu nv = navigationView.getMenu();

        drawerLayout2 = (DrawerLayout) findViewById(R.id.drawerLayout2);
        itemHistory = new MenuItem[4];
        itemHistory[0] = nv.findItem(R.id.history1);
        itemHistory[1] = nv.findItem(R.id.history2);
        itemHistory[2] = nv.findItem(R.id.history3);
        itemHistory[3] = nv.findItem(R.id.history4);

        user = intent.getExtras().getString("user");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        tv_showID.setText(user);
        db.collection("user").whereEqualTo("id", user)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                getReservationInfo = (String) document.getData().get("reservation_info");
                                historyString = (String) document.getData().get("history");
                                Log.d(TAG, historyString);

                                if (getReservationInfo.length() > 0) {
                                    reserveInfo = getReservationInfo.split(";");
                                    for (int i = 0; i < reserveInfo.length; i++) {
                                        Log.d(TAG, reserveInfo[i]);
                                    }
                                }
                                historyTitle = "";

                                if (historyString.length() > 0) {
                                    history = historyString.split(";");
                                    for (int i = 0; i < history.length; i++) {
                                        if (i % 2 == 0) {
                                            historyTitle += history[i];
                                        } else {
                                            historyTitle += "/";
                                            historyTitle += history[i];
                                            int j = i / 2;
                                            itemHistory[j].setTitle(historyTitle);
                                            historyTitle = "";
                                        }
                                        Log.d(TAG, history[i]);
                                    }
                                }
                                isLoadComplete = true;
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout2.closeDrawers();

                EditText et_startStat = (EditText) findViewById(R.id.editText_startStat);
                EditText et_endStat = (EditText) findViewById(R.id.editText_endStat);
                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                if (id == R.id.account) {
                    startToast(title + ": 계정 정보를 확인합니다.");
                } else if (id == R.id.callEmergency) {
                    if (isLoadComplete) {
                        Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:02120"));
                        startActivity(tel);
                    } else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.setting) {
                    startToast(title + ": 설정 정보를 확인합니다.");
                } else if (id == R.id.logout) {
                    if (isLoadComplete) {
                        FirebaseAuth.getInstance().signOut();
                        myStartActivity2(MainActivity.class);
                    } else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.history1) {
                    if (isLoadComplete) {
                        if (getReservationInfo.length() > 0) {
                            startToast("예약된 좌석이 존재합니다.");
                            myStartActivity2(MainActivity.class);
                        } else {
                            try {
                                et_startStat.setText(history[0]);
                                et_endStat.setText(history[1]);
                            }
                            catch (NullPointerException e) {
                                startToast("기록이 없습니다.");
                            }
                        }
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.history2) {
                    if (isLoadComplete) {
                        if (getReservationInfo.length() > 0) {
                            startToast("예약된 좌석이 존재합니다.");
                            myStartActivity2(MainActivity.class);
                        } else {
                            try {
                                et_startStat.setText(history[2]);
                                et_endStat.setText(history[3]);
                            }
                            catch (NullPointerException e) {
                                startToast("기록이 없습니다.");
                            }
                        }
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.history3) {
                    if (isLoadComplete) {
                        if (getReservationInfo.length() > 0) {
                            startToast("예약된 좌석이 존재합니다.");
                            myStartActivity2(MainActivity.class);
                        } else {
                            try {
                                et_startStat.setText(history[4]);
                                et_endStat.setText(history[5]);
                            }
                            catch (NullPointerException e) {
                                startToast("기록이 없습니다.");
                            }
                        }
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.history4) {
                    if (isLoadComplete) {
                        if (getReservationInfo.length() > 0) {
                            startToast("예약된 좌석이 존재합니다.");
                            myStartActivity2(MainActivity.class);
                        } else {
                            try {
                                et_startStat.setText(history[6]);
                                et_endStat.setText(history[7]);
                            }
                            catch (NullPointerException e) {
                                startToast("기록이 없습니다.");
                            }
                        }
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.deleteHistory) {
                    doDeleteHistory();
                }

                return true;
            }
        });
    }

    private void doDeleteHistory() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();

        data.put("history", "");
        history = null;

        itemHistory[0].setTitle("(히스토리가 없습니다.)");
        itemHistory[1].setTitle("(히스토리가 없습니다.)");
        itemHistory[2].setTitle("(히스토리가 없습니다.)");
        itemHistory[3].setTitle("(히스토리가 없습니다.)");

        db.collection("user").document(user)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startToast("기록이 삭제되었습니다.");
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 왼쪽 상단 버튼 눌렀을 때
                drawerLayout2.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        Intent intent = getIntent();

        String historyStart = intent.getExtras().getString("historyStart");
        String historyEnd = intent.getExtras().getString("historyEnd");
        user = intent.getExtras().getString("user");

        et_sid = findViewById(R.id.editText_startStat);
        et_eid = findViewById(R.id.editText_endStat);
        bt_select = findViewById(R.id.button_select_stat);

        if (historyStart != null) {
            et_sid.setText(historyStart);
        }
        if (historyEnd != null) {
            et_eid.setText(historyEnd);
        }

        odsayService = ODsayService.init(FindSubwayActivity.this, getString(R.string.odsay_key));
        odsayService.setReadTimeout(5000);
        odsayService.setConnectionTimeout(5000);

        loadFile();

        bt_select.setOnClickListener(onClickListener);
    }

    private void saveHistory() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").whereEqualTo("id", user)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                historyString = (String) document.getData().get("history");
                                historyStringNew = globalStartStation + ";" + globalEndStation;

                                if (historyString.length() > 0) {
                                    history = historyString.split(";");
                                    for (int i = 0; i < history.length; i++) {
                                        if (i >= 6) {
                                            break;
                                        }
                                        historyStringNew += ";";
                                        historyStringNew += history[i];
                                    }
                                }
                                myStartActivity(TrainActivity.class);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
        @Override
        public void onSuccess(ODsayData oDsayData, API api) {

            jsonObject = oDsayData.getJson();
            saveHistory();
        }

        @Override
        public void onError(int i, String errorMessage, API api) {
            startToast("API : " + api.name() + "\n" + errorMessage);
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String sStationName = et_sid.getText().toString();
            String eStationName = et_eid.getText().toString();
            globalStartStation = sStationName;
            globalEndStation = eStationName;

            String sStationCode = findStationCode(sStationName);
            String eStationCode = findStationCode(eStationName);
            Log.d(TAG, "sStationCode: " + sStationCode);
            Log.d(TAG, "eStationCode: " + eStationCode);

            if (sStationCode != null && eStationCode != null) {
                odsayService.requestSubwayPath("1000", sStationCode, eStationCode, "1", onResultCallbackListener);
            } else if (sStationCode == null && eStationCode == null) {
                startToast("출발역, 도착역 입력이 잘못되었습니다.");
            } else if (sStationCode == null) {
                startToast("출발역 입력이 잘못되었습니다.");
            } else {
                startToast("도착역 입력이 잘못되었습니다.");
            }
        }
    };

    private void myStartActivity(Class c){
        Intent intent = getIntent();
        Intent intent2 = new Intent(getApplicationContext(), c);
        intent2.putExtras(intent);
        intent2.putExtra("historyDB", historyStringNew);

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

    private void myStartActivity2(Class c) {
        Intent intent = new Intent(this, c);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private String findStationCode(String stationName) {

        for (int i = 1; i < locationList.size(); i++) {
            if (stationName.equals(locationList.get(i)[1])) {
                return locationList.get(i)[4];
            } else if (stationName.equals(locationList.get(i)[2])) {
                return locationList.get(i)[4];
            }
        }
        return null;
    }

    public void loadFile() {
        InputStream is = getResources().openRawResource(R.raw.subway_seoul);
        InputStreamReader stream = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(stream);

        try {
            String line;
            while ((line = in.readLine()) != null) {
                String[] arr = line.split(",");
                locationList.add(arr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        myStartActivity2(MainActivity.class);
    }
}