package com.topher.coffeebot;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;


public class CoffeeBot extends Activity implements SensorEventListener {
  private final String mClassName = "CoffeeBot";
  private SensorManager mSensorManager;
  private Sensor mAccelerometer;
  private PowerManager.WakeLock mWakeLock;

  // Angle near 90deg - if the phone gets to this angle, it's up
  private final int mDetectionAngle = 8;

  List<String> mTweets = Arrays.asList(
      "Alert! Coffee detected!",
      "You know what you want right now? Coffee!",
      "I could be wrong... False. I'm a robot. I'm never wrong. Coffee!",
      "Someone's trying to steal the coffee pot! Or maybe they're " +
      "brewing...",
      "!eeffoC",
      "The coffee tastes like mud; it was ground a few minutes ago.",
      "\"As long as there was coffee in the world, how bad could things " +
      "be?\" - Cassandra Clare, City of Ashes",
      "\"Coffee is a way of stealing time that should by rights belong to " +
      "your older self.\" - Terry Prachet, Thud!",
      "\"Black as night, sweet as sin.\" - Neil Gaiman, Anansi Boys",
      "\"Coffee - the favorite drink of the civilized world.\" - Thomas " +
      "Jefferson",
      "\"I'd rather take coffee than compliments just now.\" - Louisa May " +
      "Alcott, Little Women",
      "\"Adventure in life is good; consistency in coffee even better.\" " +
      "- Justina Chen Headley, North of Beautiful",
      "\"No matter what historians claimed, BC really stood for \"" +
      "Before Coffee\".\" - Cherise Sinclair, Master of the Mountain",
      "\"Come on, don't you ever stop and smell the coffee?\" -  " +
      "Justina Chen Headley, North of Beautiful",
      "\"My couch is coffee-colored. I can thank Starbucks and " +
      "clumsiness for that.\" - Jarod Kintz, This Book is Not for Sale",
      "\"I like my coffee with cream and my literature with " +
      "optimism.\" - Abigail Reynolds, Pemberley by the Sea",
      "\"I like instant gratification. It’s like instant coffee, only " +
      "it won’t keep you up all night.\" - Jarod Kintz",
      "\"If this is coffee, then please bring me some tea. But if this " +
      "is tea, please bring me some coffee.\" - Abraham Lincoln",
      "\"Coffee is the best thing to douse the sunrise with.\" - Drew " +
      "Sirtors",
      "Sleep is a symptom of caffeine deprivation. Good thing there's " +
      "coffee brewing!",
      "\"Coffee, the finest organic suspension ever devised.\" - Star " +
      "Trek: Voyager",
      "\"Coffee smells like freshly ground heaven.\" - Jessi Lane Adams.",
      "Deja Brew: The feeling that you've had this coffee before.",
      "\"I orchestrate my mornings to the tune of coffee.\" - Harry Mahtar",
      "\"I never drink coffee at lunch. I find it keeps me awake for " +
      "the afternoon.\" - Ronald Reagan",
      "Man does not live by coffee alone. Have a danish.",
      "\"I had some dreams, they were clouds in my coffee.\" - Carly Simon",
      "\"In Seattle you haven't had enough coffee until you can thread " +
      "a sewing machine while it's running.\" - Jeff Bezos",
      "Everybody should believe in something. I believe I'll have " +
      "another coffee.",
      "\"I could smell myself awake with that coffee.\" - Jesse Tyler",
      "\"I have measured out my life with coffee spoons.\" - T.S. Eliot",
      "Retirement is one great big giant coffee break.",
      "Do I like my coffee black? There are other colors?",
      "\"Coffee makes us severe, and grave, and philosophical.\" - " +
      "Jonathan Swift",
      "\"A mathematician is a device for turning coffee into " +
      "theorems.\" - Alfred Renyi... NOT Paul Erdos.",
      "\"Good communication is just as stimulating as black coffee, and " +
      "just as hard to sleep after.\" - Anne Morrow Lindbergh",
      "\"The powers of a man's mind are directly proportioned to the " +
      "quantity of coffee he drinks.\" - Sir James Mackintosh",
      "\"I would rather suffer with coffee than be senseless.\" - " +
      "Napoleon Bonaparte",
      "\"The discovery of coffee has enlarged the realm of illusion and " +
      "given more promise to hope.\" - Isidore Bourdon",
      "\"If it wasn't for coffee, I'd have no discernible personality " +
      "at all.\" - David Letterman",
      "\"You make good coffee... You're a slob, but you make good " +
      "coffee.\" - Cher",
      "\"I never laugh until I've had my coffee.\" - Clark Gable",
      "\"Nescafe no es cafe.\" - Mexican saying",
      "\"Coffee? No thanks, one more cup and I'll jump to warp.\" - " +
      "Captain Janeway, Star Trek: Voyager",
      "\"Computer, coffee, hot. In a cup this time!\" - Captain " +
      "Janeway, Star Trek: Voyager",
      "\"I put instant coffee in a microwave and almost went back in " +
      "time.\" - Steven Wright",
      "\"Coffee has two virtues: it is wet and warm.\" - Dutch saying",
      "\"We usually don't notice until the coffee tastes funny.\" - " +
      "Anne Afdahl",
      "\"Coffee: creative lighter fluid.\" - Floyd Maxwell",
      "\"If I asked for a cup of coffee, someone would search for the " +
      "double meaning.\" - Mae West"
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

    PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
    mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                               mClassName);
    mWakeLock.acquire();

    mLastTweetTime = -1;
  }
    
  public void onResume() {
    Log.d(mClassName, "entering onResume()");
    super.onResume();

    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    mWakeLock.acquire();

    setFloaterText();
  }

  public void onPause() {
    Log.d(mClassName, "entering onPause()");
    super.onPause();

    mSensorManager.unregisterListener(this);
    mWakeLock.release();
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
        if (!sendTweet()) {
          Log.i(mClassName, "sendTweet() failed once, trying again");
          sendTweet();
        }
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
    tweet += " On " + getFormattedTimeString(System.currentTimeMillis());
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

    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      Log.w(mClassName, "sending tweet!");
      HttpResponse response = httpClient.execute(httpPost);
      Log.i(mClassName, "response code: " + response.getStatusLine());
      String responseBody = EntityUtils.toString(response.getEntity());
      Log.d(mClassName, "response body: " + responseBody);
      if (!response.getStatusLine().toString().contains("200")) {
        return false;
      }
    } catch (ClientProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }

    Log.w(mClassName, "tweet sent!");
    mLastTweetTime = currentTime;
    setFloaterText();

    return true;
  }

  private String getFormattedTimeString(long timeMillis) {
    SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm a");
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timeMillis);
    return sdf.format(calendar.getTime());
  }

  private void setFloaterText() {
    String text = new String("@FSCoffeeBot");
    if (mLastTweetTime != -1) {
      text += "\n";
      text += "Last brew:";
      text += "\n";
      text += getFormattedTimeString(mLastTweetTime);
    }
    TextView floater = (TextView)findViewById(R.id.FloatingTextView);
    floater.setText(text);
  }
}
