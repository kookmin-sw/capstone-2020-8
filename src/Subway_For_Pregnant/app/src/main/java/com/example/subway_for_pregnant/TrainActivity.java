package com.example.subway_for_pregnant;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TrainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample);

        final ListView listView = findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                new String[]{"4556번","4557번","4558번","4559번","4560번","4561번"}));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), position+" 번째 값 : " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
            }
        });

    }

}

