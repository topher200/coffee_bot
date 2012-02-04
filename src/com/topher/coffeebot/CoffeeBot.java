package com.topher.coffeebot;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;


public class CoffeeBot extends Activity implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final String mClassName = "CoffeeBot";
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create view
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.main);
        
        // Add sensor listener
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    
    public void onResume() {
        super.onResume();
        
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
  
    public void onPause() {
        super.onPause();
        
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            float ax = event.values[0];
            if (ax < -8) {
                Log.e(mClassName, "ax " + ax);
            }
        }
        else {
            Log.e(mClassName, "how did we get a different sensor???");
        }
    }
}