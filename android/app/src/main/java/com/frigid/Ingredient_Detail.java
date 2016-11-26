package com.frigid;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class Ingredient_Detail extends AppCompatActivity {
    TextView upc;
    EditText short_name;
    TextView description;
    Button home;
    Ingredient i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_ingredient_layout);

        upc = (TextView) findViewById(R.id.upc);
        short_name = (EditText) findViewById(R.id.short_name);
        description = (TextView) findViewById(R.id.desc);
        home = (Button) findViewById(R.id.home);
        i = Main_Activity.ingForDetail;
        if(i != null){
            upc.setText(i.upc);
            short_name.setText(i.shortName);
            description.setText(i.longName);
        }
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        if(!short_name.getText().toString().isEmpty()) {
            i.shortName = short_name.getText().toString();
            File file = new File(getFilesDir(), "ingredients.json");
            FileOutputStream outputStream;
            try {
                if (!file.exists()) {
                    outputStream = openFileOutput("ingredients.json", Context.MODE_PRIVATE);
                    outputStream.write("{}".getBytes());
                    outputStream.flush();
                    outputStream.close();
                }

                FileInputStream fis = openFileInput("ingredients.json");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                String myJson = sb.toString();
                System.out.println(myJson);
                JSONObject object = new JSONObject(myJson);
                object.put(i.upc, i.toJSON());
                String newJSON = object.toString();
                System.out.println(newJSON);
                outputStream = openFileOutput("ingredients.json", Context.MODE_PRIVATE);
                outputStream.write(newJSON.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onBackPressed();
        finish();
        Main_Activity.refreshAdapters();
    }
}
