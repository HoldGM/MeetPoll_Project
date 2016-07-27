package odb234.meetpoll;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by odb234 on 7/26/16.
 */
public class Event {

    public String hostName;
    public String eventName;
    public String eventLocation;
    public String eventDate;
    public String eventTime;
    public double rating;
    public String locationType;
    public String locationSubtype;
    public ArrayList<String> ids;
    public ArrayList<ContactsListActivity.Contact> inviteList;

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
}
