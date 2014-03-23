package com.google.android.glass.sample.compass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.glass.app.Card;
import com.google.android.glass.sample.compass.model.Flight;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends Activity {

   private static final String API_ADDRESS = "http://www.flightradar24.com/PlaneFeed.json";

   private List<Card> mCards;
   private ArrayList<Flight> mFlights;
   private CardScrollView mCardScrollView;

   public int mSelectedFlightIndex;

   private OrientationManager mOrientationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



       SensorManager sensorManager =
               (SensorManager) getSystemService(Context.SENSOR_SERVICE);
       LocationManager locationManager =
               (LocationManager) getSystemService(Context.LOCATION_SERVICE);

       mOrientationManager = new OrientationManager(sensorManager, locationManager);

        mFlights = new ArrayList<Flight>();
        mCards = new ArrayList<Card>();
        mCardScrollView = new CardScrollView(this);
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
              mSelectedFlightIndex = index;
              openOptionsMenu();
           }
        });
        FlightsScrollAdapter adapter = new FlightsScrollAdapter();
        mCardScrollView.setAdapter(adapter);
        TextView tw = new TextView(this);
        tw.setText("Loading...");
//        mCardScrollView.setEmptyView(tw);
        mCardScrollView.activate();
        setContentView(mCardScrollView);

        new Downloader().execute();
    }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater menuInflater = getMenuInflater();
      menuInflater.inflate(R.menu.menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      Intent i = null;
      switch (item.getItemId()) {
         case R.id.menu_compas:
            i = new Intent(this, CompassService.class);
            startService(i);
            break;
         case R.id.menu_web:
            i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("http://www.flightradar24.com/data/flights/" + mFlights.get(mSelectedFlightIndex).getFlightName()));
            startActivity(i);
            break;
      }
      return true;
   }

   private void createCards() {
        Card card;
        Collections.sort(mFlights);
        int i = 0;
        for (Flight fl : mFlights) {
           card = new Card(this);
           int imgDrawable = R.drawable.aero1;
           switch (i++ % 5) {
              case 0:
                 imgDrawable = R.drawable.aero1;
                 break;
              case 1:
                 imgDrawable = R.drawable.aero2;
                 break;

              case 2:
                 imgDrawable = R.drawable.aero3;
                 break;

              case 3:
                 imgDrawable = R.drawable.aero4;
                 break;

              case 4:
                 imgDrawable = R.drawable.aero5;
                 break;
           }
           card.addImage(imgDrawable);
           card.setImageLayout(Card.ImageLayout.LEFT);
           card.setText(fl.getPath() + "\n" + fl.getFlightName());
           card.setFootnote("Distance: " + Math.round(fl.getDistance()/1000) + " km");
           mCards.add(card);
        }
    }

   private class FlightsScrollAdapter extends CardScrollAdapter {

      @Override
      public int findIdPosition(Object id) {
         return -1;
      }

      @Override
      public int findItemPosition(Object item) {
         return mCards.indexOf(item);
      }

      @Override
      public int getCount() {
         return mCards.size();
      }

      @Override
      public Object getItem(int position) {
         return mCards.get(position);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         return mCards.get(position).toView();
      }
   }

   private class Downloader extends AsyncTask<Void, Void, Void> {
      private Location mLocation;

      @Override
      protected Void doInBackground(Void... voids) {
         PowerManager pm = (PowerManager)App.get().getSystemService(Context.POWER_SERVICE);
         PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "app");
         wl.acquire();
         mLocation = OrientationManager.getLastLocation();
         Log.d("app", " " + mLocation.getLongitude());

         String url = API_ADDRESS;
         HttpClient httpclient = new DefaultHttpClient();
         try {

            HttpGet method = new HttpGet(new URI(url));
            HttpResponse response = httpclient.execute(method);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
               HttpEntity entity = response.getEntity();

               // a simple JSON response read
               InputStream instream = entity.getContent();
               String result = convertStreamToString(instream);

               // a simple JSONObject creation
               JSONObject json = null;
               try {
                  json = new JSONObject(result);
               } catch (JSONException e) {
                  e.printStackTrace();
               }

               // closing the input stream will trigger connection release
               instream.close();

               Iterator<?> keys = json.keys();

               while (keys.hasNext()) {
                  String key = (String) keys.next();
                  try {
                     if (json.get(key) instanceof JSONArray) {
                        //loading the data from JSONArray to objects
                        JSONArray current = (JSONArray) json.get(key);

                        if (Math.abs(mLocation.getLongitude() - current.getDouble(2)) > 1 ) continue;
                        if (Math.abs(mLocation.getLatitude() - current.getDouble(1)) > 1 ) continue;
                        if (current.getString(11).isEmpty() || current.getString(12).isEmpty()) continue;
                        Flight fl = new Flight(current.getString(13), current.getDouble(1), current.getDouble(2), current.getString(8), current.getString(9), current.getString(11), current.getString(12), current.getString(4), mLocation);
                        mFlights.add(fl);
                     }
                  } catch (JSONException e) {
                     e.printStackTrace();
                  }
               }
               Log.e("app", "Finished parsing");
               return null;
            } else if (statusLine.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
               return null;
            }

         } catch (ClientProtocolException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } catch (URISyntaxException e1) {
            e1.printStackTrace();
         } finally {
            createCards();
            wl.release();
         }

         return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
         super.onPostExecute(aVoid);
         App.setFlights(mFlights);
         mCardScrollView.getAdapter().notifyDataSetChanged();
      }

      /**
       * method for converting stream to string
       *
       * @param is
       * @return
       */
      private String convertStreamToString(InputStream is) {
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder sb = new StringBuilder();

         String line = null;
         try {
            while ((line = reader.readLine()) != null) {
               sb.append(line + "\n");
            }
         } catch (IOException e) {
            e.printStackTrace();
         } finally {
            try {
               is.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
         return sb.toString();
      }
   }

}
