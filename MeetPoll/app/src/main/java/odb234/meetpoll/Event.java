package odb234.meetpoll;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by odb234 on 7/26/16.
 */
public class Event {

    private String hostName;
    private String eventName;
    private String eventLocation;
    private String eventDate;
    private String eventTime;
    private double rating;
    private String locationType;
    private String locationSubtype;
    private ArrayList<String> ids;
    private ArrayList<ContactsListActivity.Contact> inviteList;

    public Event(String hn, String en, String el, String ed, String et, double r, String lt, String lst, ArrayList<String> list, ArrayList<ContactsListActivity.Contact> il){
        hostName = hn;
        eventName = en;
        eventLocation = el;
        eventDate = ed;
        eventTime = et;
        rating = r;
        locationType = lt;
        locationSubtype = lst;
        ids = list;
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
    public String getEventDate(){
        return eventDate;
    }
    public String getEventTime(){
        return eventTime;
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
    public ArrayList<String> getIds(){
        return ids;
    }
    public ArrayList<ContactsListActivity.Contact> getInviteList(){
        return inviteList;
    }
}
