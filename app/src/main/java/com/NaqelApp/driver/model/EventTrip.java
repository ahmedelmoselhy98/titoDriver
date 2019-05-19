package com.NaqelApp.driver.model;

public class EventTrip {
    private String tripId= "";


    public EventTrip(String tripId) {
        this.tripId = tripId;
    }


    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
