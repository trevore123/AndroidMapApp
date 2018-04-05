package com.example.mapapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.EditText;
import android.view.View;
import android.widget.Spinner;

import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public final static String LOCATION = "com.example.mapapp.LOCATION";
    public final static String MODE = "com.example.mapapp.MODE";
    private String travel_mode = "driving";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = (Spinner) findViewById(R.id.travel_spinner);
        spinner.setOnItemSelectedListener(this);
    }

    public void sendLocation(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        EditText editText = (EditText) findViewById(R.id.location_text);
        String message = editText.getText().toString();
        intent.putExtra(LOCATION, message);
        intent.putExtra(MODE, travel_mode);
        startActivity(intent);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        String selection = parent.getItemAtPosition(pos).toString();
        if(selection.equals("Drive")) travel_mode = "driving";
        else if(selection.equals("Bike")) travel_mode = "bicycling";
        else if(selection.equals("Walk")) travel_mode = "walking";
        else travel_mode = "transit";
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
