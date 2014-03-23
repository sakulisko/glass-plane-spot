package com.google.android.glass.sample.compass.model;

import android.location.Location;

/**
 * Created by lukas on 23.3.14.
 */
public class Flight implements Comparable<Flight> {
   private double mLatitude;
   private double mLongitude;
   private double mAltitude;
   private String mFlightName;
   private String mDestination;
   private String mOrigin;
   private Location mLocation;
   private double mDistance;

   public Flight(String id, double lat, double lon, String type,
                 String registartion, String from, String to, String fl, Location currentLocation) {
      super();
      mFlightName = id;
      mLongitude = lon;
      mLatitude = lat;
      mOrigin = from;
      mDestination = to;
      mAltitude = Double.parseDouble(fl);
      mLocation = new Location("");
      mLocation.setLatitude(mLatitude);
      mLocation.setLongitude(mLongitude);

      mDistance = mLocation.distanceTo(currentLocation);
   }


   public double getLatitude() {
      return mLatitude;
   }

   public void setLatitude(double mLatitude) {
      this.mLatitude = mLatitude;
   }

   public double getLongitude() {
      return mLongitude;
   }

   public void setLongitude(double mLongitude) {
      this.mLongitude = mLongitude;
   }

   public double getAltitude() {
      return mAltitude;
   }

   public void setAltitude(double mAltitude) {
      this.mAltitude = mAltitude;
   }

   public String getFlightName() {
      return mFlightName;
   }

   public void setFlightName(String mFlightName) {
      this.mFlightName = mFlightName;
   }

   public String getDestination() {
      return mDestination;
   }

   public void setDestination(String mDestination) {
      this.mDestination = mDestination;
   }

   public String getOrigin() {
      return mOrigin;
   }

   public void setOrigin(String mOrigin) {
      this.mOrigin = mOrigin;
   }

   public Location getLocation() {
      return mLocation;
   }

   public double getDistance() {return mDistance;}

   public String getPath() {
      return getOrigin() + " → " + getDestination();
   }

   @Override
   public int compareTo(Flight flight) {
      return (int)(mDistance - flight.mDistance);
   }
}
