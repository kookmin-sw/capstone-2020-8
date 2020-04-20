package com.example.subway_for_pregnant;

<<<<<<< HEAD
=======
import android.content.Context;
>>>>>>> 7cf7d934711a1b9db2593e72660d772e627680e9
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
<<<<<<< HEAD
import android.widget.TextView;

=======
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
>>>>>>> 7cf7d934711a1b9db2593e72660d772e627680e9
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
import java.util.Map;

<<<<<<< HEAD
=======
import org.json.simple.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

>>>>>>> 7cf7d934711a1b9db2593e72660d772e627680e9
public class FindSubwayActivity extends AppCompatActivity {

    private TextView tv_data;
    private EditText et_sid;
    private EditText et_eid;

    private Button bt_select;

    private ODsayService odsayService;
    private JSONObject jsonObject;
    private Map mapObject;
<<<<<<< HEAD
=======
    private Spinner sp_api;
    private RadioGroup rg_object_type;
    private RadioButton rb_json, rb_map;
    private Button bt_api_call;

    private Context context;
    private String spinnerSelectedName;

>>>>>>> 7cf7d934711a1b9db2593e72660d772e627680e9

    private static List<String[]> locationList = new ArrayList<String[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findsubway);

        init();
    }

    private void init() {
<<<<<<< HEAD
        tv_data = (TextView) findViewById(R.id.tv_data2);
        bt_select = (Button) findViewById(R.id.button_select_stat);
        et_sid = (EditText) findViewById(R.id.editText_startStat);
        et_eid = (EditText) findViewById(R.id.editText_endStat);
=======
        context = this;
        sp_api = (Spinner) findViewById(R.id.sp_api);
        rg_object_type = (RadioGroup) findViewById(R.id.rg_object_type);
        bt_api_call = (Button) findViewById(R.id.bt_api_call);
        rb_json = (RadioButton) findViewById(R.id.rb_json);
        rb_map = (RadioButton) findViewById(R.id.rb_map);
        tv_data = (TextView) findViewById(R.id.tv_data2);
        sp_api.setSelection(0);
>>>>>>> 7cf7d934711a1b9db2593e72660d772e627680e9

        odsayService = ODsayService.init(FindSubwayActivity.this, getString(R.string.odsay_key));
        odsayService.setReadTimeout(5000);
        odsayService.setConnectionTimeout(5000);

        loadFile();

<<<<<<< HEAD
        bt_select.setOnClickListener(onClickListener);
    }

    private OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
        @Override
        public void onSuccess(ODsayData oDsayData, API api) {
            jsonObject = oDsayData.getJson();
            mapObject = oDsayData.getMap();
            tv_data.setText(jsonObject.toString());
=======
        rg_object_type.setOnCheckedChangeListener(onCheckedChangeListener);
        bt_select.setOnClickListener(onClickListener);


    }

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
            if (rg_object_type.getCheckedRadioButtonId() == rb_json.getId()) {
                tv_data.setText(jsonObject.toString());
            } else if (rg_object_type.getCheckedRadioButtonId() == rb_map.getId()) {
                tv_data.setText(mapObject.toString());
            }
        }
    };

    private OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
        @Override
        public void onSuccess(ODsayData oDsayData, API api) {

            jsonObject = oDsayData.getJson();
            mapObject = oDsayData.getMap();

            try {

                if (rg_object_type.getCheckedRadioButtonId() == rb_json.getId()) {

                    //tv_data.setText(jsonObject.toString())

                    tv_data.setText(
                            "출발역 : " +jsonObject.getJSONObject("result").getString("globalStartName")
                                    +"\n"
                                    +"도착역 : " +jsonObject.getJSONObject("result").getString("globalEndName")
                                    +"\n"
                                    +"걸리는 시간 : " +jsonObject.getJSONObject("result").getInt("globalTravelTime") + "분"
                                    +"\n"
                                    +"총 거리: " +jsonObject.getJSONObject("result").getInt("globalDistance") + "km"
                                    +"\n"
                                    +"총 정거장 : " +jsonObject.getJSONObject("result").getInt("globalStationCount") + "정거장"
                                    +"\n"
                                    +"카드요금 : " +jsonObject.getJSONObject("result").getInt("fare") + "원"
                                    +"\n"
                                    +"현금 : " +jsonObject.getJSONObject("result").getInt("cashFare") + "원"
                    );

                } else if (rg_object_type.getCheckedRadioButtonId() == rb_map.getId()) {
                    tv_data.setText(mapObject.toString());
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
>>>>>>> 7cf7d934711a1b9db2593e72660d772e627680e9
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

<<<<<<< HEAD
=======

>>>>>>> 7cf7d934711a1b9db2593e72660d772e627680e9
            if (sStationCode != null && eStationCode != null) {
                odsayService.requestSubwayPath("1000", sStationCode, eStationCode, "1", onResultCallbackListener);
            }
            else if (sStationCode == null && eStationCode == null) {
                tv_data.setText("출발역, 도착역 입력이 잘못되었습니다.");
            }
            else if (sStationCode == null) {
                tv_data.setText("출발역 입력이 잘못되었습니다.");
            }
            else {
                tv_data.setText("도착역 입력이 잘못되었습니다.");
            }
        }
    };

    private String findStationCode(String stationName) {
        String[] stationCode = null;

        int check = 0;
        for (int i = 1; i < locationList.size(); i++){
            if (stationName.equals(locationList.get(i)[1])) {
                return locationList.get(i)[4];
            }
            else if (stationName.equals(locationList.get(i)[2])) {
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
            while((line = in.readLine()) != null) {
                String[] arr = line.split(",");
                locationList.add(arr);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

