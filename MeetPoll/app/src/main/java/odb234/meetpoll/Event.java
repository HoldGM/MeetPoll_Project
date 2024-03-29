package odb234.meetpoll;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by odb234 on 7/26/16.
 */
public class Event {

    private String hostName;
    private String hostPhone;
    private String eventName;
    private String eventLocation;
    private String eventDateTime;
    private double rating;
    private String locationType;
    private String locationSubtype;
    private long key;
    private ArrayList<LocationListing> places;
    private ArrayList<Contact> inviteList;

    public Event(){}

    public Event(String hn, String hp, String en, String el, String ed, double r, long k, String lt, String lst, ArrayList<LocationListing> pl, ArrayList<Contact> il){
        hostName = hn;
        hostPhone = hp;
        eventName = en;
        eventLocation = el;
        eventDateTime = ed;
        rating = r;
        key = k;
        locationType = lt;
        locationSubtype = lst;
        places = pl;
        inviteList =il;
    }

    public String getHostName(){
        return hostName;
    }
    public String getEventName(){
        return eventName;
    }
    public String getEventLocation(){
        return eventLocation;
    }
    public String getEventDateTime(){
        return eventDateTime;
    }
    public double getRating(){
        return rating;
    }
    public String getLocationType(){
        return locationType;
    }
    public String getLocationSubtype(){
        return locationSubtype;
    }
    public long getKey(){ return key; }
    public ArrayList<LocationListing> getPlaces(){
        return places;
    }
    public ArrayList<Contact> getInviteList(){
        return inviteList;
    }
    public String getHostPhone(){ return hostPhone; }
}
