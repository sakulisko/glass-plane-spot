package com.google.android.glass.sample.compass;

import android.app.Application;
import android.content.Context;

import com.google.android.glass.sample.compass.model.Flight;

import java.util.ArrayList;

/**
 * Created by lukas on 23.3.14.
 */
public class App extends Application {

   private static App instance;

   private static ArrayList<Flight> mFlights;

   public App() {
      instance = this;
   }

   public static Context get() {
      return instance;
   }

   public static ArrayList<Flight> getFlights() {
      return mFlights;
   }

   public static void setFlights(ArrayList<Flight> flights) {
      mFlights = flights;
   }
}
