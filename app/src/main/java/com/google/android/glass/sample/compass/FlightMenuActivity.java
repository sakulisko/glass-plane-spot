package com.google.android.glass.sample.compass;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by lukas on 23.3.14.
 */
public class FlightMenuActivity extends Activity {

   @Override
   public void onAttachedToWindow() {
      super.onAttachedToWindow();
      openOptionsMenu();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection.
      switch (item.getItemId()) {
         case R.id.stop:
            stopService(new Intent(this, CompassService.class));
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void onOptionsMenuClosed(Menu menu) {
      // Nothing else to do, closing the activity.
      finish();
   }
}
