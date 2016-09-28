package com.example.freddy.readacclemometer;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener {

    public Vibrator v;
    private float lastX, lastY, lastZ;
    private SensorManager sensorManager;
    private Sensor accelerometer,light;
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;
    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private boolean knowlight=false;
    private float vibrateThreshold = 0;
    private TextView currentX, currentY, currentZ, maxX, maxY, maxZ;
    private ArrayList<String> data= new ArrayList<String>();
    private Button stop_data;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        stop_data=(Button)findViewById(R.id.button);
        stop_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File root = new File(Environment.getExternalStorageDirectory(), "/Notes/");
                if(!root.exists()){
                    if(root.mkdir()){
                        Log.d(getApplicationContext().getClass().getName(), root.getAbsolutePath()+" directory created");
                    }
                }
                File gpxfile = new File(root, System.currentTimeMillis()+".csv");
                try {
                    FileWriter writer = new FileWriter(gpxfile);
                    writer.write("Timestamp,x,y,z\n");
                    for(String temp:data) {
                        writer.write(temp);
                    }
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            // fai! we dont have an accelerometer!
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!=null)
        {
            light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            sensorManager.registerListener(this,light,sensorManager.SENSOR_DELAY_NORMAL);

        }
        else
        {

        }
        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

    }

    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);

        maxX = (TextView) findViewById(R.id.maxX);
        maxY = (TextView) findViewById(R.id.maxY);
        maxZ = (TextView) findViewById(R.id.maxZ);

    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.accuracy==sensorManager.SENSOR_STATUS_UNRELIABLE)return;
        Sensor se=event.sensor;
        if(se.getType()==Sensor.TYPE_LIGHT)
        {
            float curlight=event.values[0];
            if(curlight<1)knowlight=true;
            else knowlight=false;

        }
        else if(se.getType()==Sensor.TYPE_ACCELEROMETER)
        {
            // clean current values
            displayCleanValues();
            // display the current x,y,z accelerometer values
            displayCurrentValues();
            // display the max x,y,z accelerometer values
            displayMaxValues();

            // get the change of the x,y,z values of the accelerometer
            deltaX = Math.abs(lastX - event.values[0]);
            deltaY = Math.abs(lastY - event.values[1]);
            deltaZ = Math.abs(lastZ - event.values[2]);
            if (deltaX < 2)
                deltaX = 0;
            if (deltaY < 2)
                deltaY = 0;
            if(knowlight) {
                StringBuilder getdata = new StringBuilder();
                getdata.append(System.currentTimeMillis() + ",");
                getdata.append(deltaX + ",");
                getdata.append(deltaY + ",");
                getdata.append(deltaZ + "\n");
                data.add(getdata.toString());
            }

            // if the change is below 2, it is just plain noise

        }

        /*if (deltaZ vibrateThreshold)||(deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)){
            v.vibrate(50);*/

    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(Float.toString(deltaXMax));
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            maxY.setText(Float.toString(deltaYMax));
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(Float.toString(deltaZMax));
        }
    }
}
