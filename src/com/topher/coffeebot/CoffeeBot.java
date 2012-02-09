package com.topher.coffeebot;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class CoffeeBot extends Activity implements SensorEventListener {
    private final String mClassName = "CoffeeBot";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    
    // Angle near 90deg - if the phone gets to this angle, it's up
    private final int mDetectionAngle = 8;

    List<String> mTweets = Arrays.asList(
        "Alert! Coffee detected!",
        "You know what you want right now? Coffee!",
        "I could be wrong... False. I'm a robot. I'm never wrong. Coffee!",
        "Someone's trying to steal the coffee pot! Or maybe they're brewing...",
        "!eeffoC",
        "The coffee tastes like mud; it was ground a few minutes ago"
                                         );
    
    // Save the last angle we saw the phone at
    private float mLastAngle;
    // The last time we sent a tweet (in millis)
    private long mLastTweetTime;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(mClassName, "entering onCreate()");
        super.onCreate(savedInstanceState);

        // Create view
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.main);
        
        // Add sensor listener
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mLastTweetTime = -1;
    }
    
    public void onResume() {
        Log.d(mClassName, "entering onResume()");
        super.onResume();
        
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        setFloaterText();
    }
  
    public void onPause() {
        Log.d(mClassName, "entering onPause()");
        super.onPause();
        
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    public void onSensorChanged(SensorEvent event) {
        Log.d(mClassName, "entering onSensorChanged()");
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            float currentAngle = Math.abs(event.values[0]);
            // If we were tilted last time and now we're not, trigger
            if ((mLastAngle > mDetectionAngle) &&
                (currentAngle < mDetectionAngle)) {
                sendTweet();
            }
            mLastAngle = currentAngle;
        }
        else {
            Log.e(mClassName, "how did we get a different sensor???");
        }
    }
    
    public Boolean sendTweet() {
        Log.d(mClassName, "entering sendTweet()");

        // Don't send a tweet if we sent one in the past 5 minutes
        long currentTime = System.currentTimeMillis();
        if ((mLastTweetTime != -1) &&  // uninitialized
            (currentTime < (mLastTweetTime + 1000*60*5))) {
            Log.i(mClassName,
                  "not sending a tweet - sent one less than 5 minutes ago");
            return false;
        }
        
        HttpPost httpPost = new HttpPost(
                "http://api.supertweet.net/1/statuses/update.json");
        httpPost.addHeader(BasicScheme.authenticate(
                                   new UsernamePasswordCredentials(
                                           "FSCoffeeBot",
                                           "supertweetisawesome"),
                                   "US-ASCII", false));
                                                            
        Random random = new Random();
        int tweetNumber = random.nextInt(mTweets.size());
        String tweet = mTweets.get(tweetNumber);
        Log.i(mClassName, "tweet: " + tweet);
                
        ArrayList<NameValuePair> nameValuePairs =
                new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("status", tweet));
            
		try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
            return false;
		}
        
        // Just for debugging
        Log.e(mClassName, "debugging - not sending tweet");
        /*
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            Log.e(mClassName, "sending tweet!");
			HttpResponse response = httpClient.execute(httpPost);
			Log.i(mClassName, "response code: " + response.getStatusLine());
            String responseBody = EntityUtils.toString(response.getEntity());
			Log.d(mClassName, "response body: " + responseBody);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            return false;
		}
		*/
        
        mLastTweetTime = currentTime;
        setFloaterText();
        
        return true;
    }

    private void setFloaterText() {
        String text = new String("@FSCoffeeBot");
        if (mLastTweetTime != -1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mLastTweetTime);
            text += "\n";
            text += "Last brew:";
            text += "\n";
            SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mma");
            text += sdf.format(calendar.getTime());
        }
        TextView floater = (TextView)findViewById(R.id.FloatingTextView);
        floater.setText(text);
    }
}
