package com.example.subway_for_pregnant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.material.navigation.NavigationView;
import org.json.JSONObject;

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MenuItem itemHistory[];
    private DrawerLayout drawerLayout;
    private Context context = this;
    private ODsayService odsayService;
    private JSONObject jsonObject;

    FirebaseUser user;
    String getReservationInfo;
    String reserveInfo[];
    String historyString;
    String history[];
    String historyTitle = "";
    String globalHistoryStart = "";
    String globalHistoryEnd = "";

    boolean isLoadComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.list);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View v = navigationView.getHeaderView(0);
        TextView tv_showID = (TextView) v.findViewById(R.id.textView_showID);
        Menu nv = navigationView.getMenu();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        itemHistory = new MenuItem[4];
        itemHistory[0] = nv.findItem(R.id.history1);
        itemHistory[1] = nv.findItem(R.id.history2);
        itemHistory[2] = nv.findItem(R.id.history3);
        itemHistory[3] = nv.findItem(R.id.history4);

        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (user == null) {
            myStartActivity3(LoginActivity.class);
        } else {
            tv_showID.setText(user.getEmail());
            db.collection("user").whereEqualTo("id", user.getEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    getReservationInfo = (String) document.getData().get("reservation_info");
                                    historyString = (String) document.getData().get("history");

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
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                if (id == R.id.account) {
                    Toast.makeText(context, title + ": 계정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.callEmergency) {
                    if (isLoadComplete) {
                        Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:02120"));
                        startActivity(tel);
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.setting) {
                    Toast.makeText(context, title + ": 설정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.logout) {
                    if (isLoadComplete) {
                        FirebaseAuth.getInstance().signOut();
                        myStartActivity3(MainActivity.class);
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.history1) {
                    if (isLoadComplete) {
                        if (getReservationInfo.length() > 0) {
                            doFindSubway();
                        } else {
                            globalHistoryStart = history[0];
                            globalHistoryEnd = history[1];
                            myStartActivity(FindSubwayActivity.class);
                        }
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.history2) {
                    if (isLoadComplete) {
                        if (getReservationInfo.length() > 0) {
                            doFindSubway();
                        } else {
                            globalHistoryStart = history[2];
                            globalHistoryEnd = history[3];
                            myStartActivity(FindSubwayActivity.class);
                        }
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.history3) {
                    if (isLoadComplete) {
                        if (getReservationInfo.length() > 0) {
                            doFindSubway();
                        } else {
                            globalHistoryStart = history[4];
                            globalHistoryEnd = history[5];
                            myStartActivity(FindSubwayActivity.class);
                        }
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                } else if (id == R.id.history4) {
                    if (isLoadComplete) {
                        if (getReservationInfo.length() > 0) {
                            doFindSubway();
                        } else {
                            globalHistoryStart = history[6];
                            globalHistoryEnd = history[7];
                            myStartActivity(FindSubwayActivity.class);
                        }
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                }

                return true;
            }
        });
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
                    if (isLoadComplete) {
                        FirebaseAuth.getInstance().signOut();
                        myStartActivity3(MainActivity.class);
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                    break;
                case R.id.trainbutton:
                    if (isLoadComplete) {
                        if (getReservationInfo.length() > 0) {
                            doFindSubway();
                        } else {
                            myStartActivity(FindSubwayActivity.class);
                        }
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                    break;
                case R.id.bluetoothButton:
                    if (isLoadComplete) {
                        myStartActivity(SampleBluetoothActivity.class);
                    }
                    else {
                        startToast("유저 정보를 읽어오는 중입니다.");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 왼쪽 상단 버튼 눌렀을 때
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

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
        if (globalHistoryStart.length() > 0) {
            intent.putExtra("historyStart", globalHistoryStart);
        }
        if (globalHistoryEnd.length() > 0) {
            intent.putExtra("historyEnd", globalHistoryEnd);
        }
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

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
