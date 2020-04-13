package com.example.subway_for_pregnant;

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
import java.util.Map;

public class FindSubwayActivity extends AppCompatActivity {

    private TextView tv_data;
    private EditText et_sid;
    private EditText et_eid;

    private Button bt_select;

    private ODsayService odsayService;
    private JSONObject jsonObject;
    private Map mapObject;

    private static List<String[]> locationList = new ArrayList<String[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findsubway);

        init();
    }

    private void init() {
        tv_data = (TextView) findViewById(R.id.tv_data2);
        bt_select = (Button) findViewById(R.id.button_select_stat);
        et_sid = (EditText) findViewById(R.id.editText_startStat);
        et_eid = (EditText) findViewById(R.id.editText_endStat);

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
            mapObject = oDsayData.getMap();
            tv_data.setText(jsonObject.toString());
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

