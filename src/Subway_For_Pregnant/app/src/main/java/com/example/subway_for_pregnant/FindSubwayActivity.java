package com.example.subway_for_pregnant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

public class FindSubwayActivity extends AppCompatActivity {

    private TextView tv_data;
    private EditText et_sid;
    private EditText et_eid;

    private Button bt_select;

    private ODsayService odsayService;
    private JSONObject jsonObject;

    private static List<String[]> locationList = new ArrayList<String[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findsubway);

        init();
    }

    private void init() {
        tv_data = (TextView) findViewById(R.id.tv_data2);
        et_sid = findViewById(R.id.editText_startStat);
        et_eid = findViewById(R.id.editText_endStat);
        bt_select = findViewById(R.id.button_select_stat);

        odsayService = ODsayService.init(FindSubwayActivity.this, getString(R.string.odsay_key));
        odsayService.setReadTimeout(5000);
        odsayService.setConnectionTimeout(5000);

        loadFile();

        bt_select.setOnClickListener(onClickListener);
    }

    private OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
        @Override
        public void onSuccess(ODsayData oDsayData, API api) {

            jsonObject = oDsayData.getJson();
            myStartActivity(TrainActivity.class);
/*
            try {
                //tv_data.setText(jsonObject.toString());
                //tv_data.setText("" + );
                tv_data.setText(
                    "출발역 : " + jsonObject.getJSONObject("result").getString("globalStartName")
                        + "\n"
                        + "도착역 : " + jsonObject.getJSONObject("result").getString("globalEndName")
                        + "\n"
                        + "걸리는 시간 : " + jsonObject.getJSONObject("result").getInt("globalTravelTime") + "분"
                        + "\n"
                        + "총 거리: " + jsonObject.getJSONObject("result").getInt("globalDistance") + "km"
                        + "\n"
                        + "총 정거장 : " + jsonObject.getJSONObject("result").getInt("globalStationCount") + "정거장"
                        + "\n"
                        + "카드요금 : " + jsonObject.getJSONObject("result").getInt("fare") + "원"
                        + "\n"
                        + "현금 : " + jsonObject.getJSONObject("result").getInt("cashFare") + "원"
                );
            } catch (JSONException e) {
                tv_data.setText("ㅁㄴㅇㄻㄴㅇㄹ");
                e.printStackTrace();
            } */

// 혹시 모르니 파싱 데이터 내비둘것 2020.04.22

        }

        @Override
        public void onError(int i, String errorMessage, API api) {
            tv_data.setText("API : " + api.name() + "\n" + errorMessage);
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String sStationName = et_sid.getText().toString();
            String eStationName = et_eid.getText().toString();

            String sStationCode = findStationCode(sStationName);
            String eStationCode = findStationCode(eStationName);

            if (sStationCode != null && eStationCode != null) {
                odsayService.requestSubwayPath("1000", sStationCode, eStationCode, "1", onResultCallbackListener);
            } else if (sStationCode == null && eStationCode == null) {
                tv_data.setText("출발역, 도착역 입력이 잘못되었습니다.");
            } else if (sStationCode == null) {
                tv_data.setText("출발역 입력이 잘못되었습니다.");
            } else {
                tv_data.setText("도착역 입력이 잘못되었습니다.");
            }
        }
    };

    private void myStartActivity(Class c){
        Intent intent = new Intent(getApplicationContext(), c);

        try {
            intent.putExtra("globalStartName", jsonObject.getJSONObject("result").getString("globalStartName"));
            intent.putExtra("globalEndName", jsonObject.getJSONObject("result").getString("globalEndName"));
            intent.putExtra("globalTravelTime", jsonObject.getJSONObject("result").getInt("globalTravelTime"));
            intent.putExtra("globalDistance", jsonObject.getJSONObject("result").getInt("globalDistance"));
            intent.putExtra("globalStationCount", jsonObject.getJSONObject("result").getInt("globalStationCount"));
            intent.putExtra("fare", jsonObject.getJSONObject("result").getInt("fare"));
            intent.putExtra("cashFare", jsonObject.getJSONObject("result").getInt("cashFare"));

            intent.putExtra("driveInfoLength", jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").length());
            for (int i = 0; i < jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").length(); i++) {
                intent.putExtra("driveInfoLaneID" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getString("laneID"));
                intent.putExtra("driveInfoLaneName" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getString("laneName"));
                intent.putExtra("driveInfoStartName" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getString("startName"));
                intent.putExtra("driveInfoStationCount" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getInt("stationCount"));
                intent.putExtra("driveInfoWayCode" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getInt("wayCode"));
                intent.putExtra("driveInfoWayName" + i, jsonObject.getJSONObject("result").getJSONObject("driveInfoSet").getJSONArray("driveInfo").getJSONObject(i).getString("wayName"));
            }

            intent.putExtra("stationsLength", jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").length());
            for (int i = 0; i < jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").length(); i++) {
                intent.putExtra("stationsStartID" + i, jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").getJSONObject(i).getInt("startID"));
                intent.putExtra("stationsStartName" + i, jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").getJSONObject(i).getString("startName"));
                intent.putExtra("stationsEndSID" + i, jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").getJSONObject(i).getInt("endSID"));
                intent.putExtra("stationsEndName" + i, jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").getJSONObject(i).getString("endName"));
                intent.putExtra("stationsTravelTime" + i, jsonObject.getJSONObject("result").getJSONObject("stationSet").getJSONArray("stations").getJSONObject(i).getInt("travelTime"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            intent.putExtra("exChangeInfoLength", jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").length());
            for (int i = 0; i < jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").length(); i++) {
                intent.putExtra("exChangeInfoLaneName" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getString("laneName"));
                intent.putExtra("exChangeInfoStartName" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getString("startName"));
                intent.putExtra("exChangeInfoExName" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getString("exName"));
                intent.putExtra("exChangeInfoExSID" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getInt("exSID"));
                intent.putExtra("exChangeInfoFastTrain" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getInt("fastTrain"));
                intent.putExtra("exChangeInfoFastDoor" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getInt("fastDoor"));
                intent.putExtra("exChangeInfoExWalkTime" + i, jsonObject.getJSONObject("result").getJSONObject("exChangeInfoSet").getJSONArray("exChangeInfo").getJSONObject(i).getInt("exWalkTime"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
}