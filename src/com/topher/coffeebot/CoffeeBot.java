package com.topher.coffeebot;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;


public class CoffeeBot extends Activity implements SensorEventListener {
    private final String mClassName = "CoffeeBot";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    
    // Angle near 90deg - if the phone gets to this angle, it's up
    private final int mDetectionAngle = 8;
    // Save the last angle we saw the phone at
    private float mLastAngle;

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
            float current_angle = Math.abs(event.values[0]);
            // If we were tilted last time and now we're not, trigger
            if ((mLastAngle > mDetectionAngle) &&
                (current_angle < mDetectionAngle)) {
                sendTweet();
            }
            mLastAngle = current_angle;
        }
        else {
            Log.e(mClassName, "how did we get a different sensor???");
        }
    }
    
    public Boolean sendTweet() {
        Log.e(mClassName, "entering sendTweet()");
        
        HttpPost httpPost = new HttpPost(
                "http://api.supertweet.net/1/statuses/update.xml");
        httpPost.setHeader("content-type", "application/json");
        
        JSONObject json_data = new JSONObject();
        try {
            json_data.put("status", "Robots don't even like coffee!");
		} catch (JSONException e) {
			e.printStackTrace();
            return false;
		}
            
        
		try {
			StringEntity entity = new StringEntity(json_data.toString());
			httpPost.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
            return false;
		}
        
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(
                new AuthScope("supertweet", 80, AuthScope.ANY_REALM),
                new UsernamePasswordCredentials("FSCoffeeBot",
                                                "supertweetisawesome"));
        
        try {
            Log.e(mClassName, "sending tweet!");
			HttpResponse response = httpClient.execute(httpPost);
			Log.i(mClassName, "response code: " + response.getStatusLine());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            return false;
		}
        
        return true;
    }
}