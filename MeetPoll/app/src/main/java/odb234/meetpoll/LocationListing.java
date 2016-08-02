package odb234.meetpoll;

/**
 * Created by Otis on 7/28/2016.
 */
public class LocationListing {
    private String name;
    private String address;
    private String phone;
    private float rating;
    private boolean state;
    private double lat;
    private double lng;
    private String id;
    private int voteCount;

    public LocationListing(){}
    public LocationListing(String n, String a, String p, float r, boolean s, double latitude, double longitude, String i){
        name = n;
        address = a;
        phone = p;
        rating = r;
        state = s;
        lat = latitude;
        lng = longitude;
        id = i;
        voteCount = 0;
    }
    public LocationListing(String n, String a, int v){
        name = n;
        address = a;
        voteCount = v;
    }

    public void setName(String n){ name = n;}
    public void setAddress(String a){ address = a; }
    public void setPhone(String p){ phone = p; }
    public void setRating(float f){ rating = f; }
    public void setId(String i){ id = i; }
    public void setState(boolean b){ state = b; }
    public void setLat(double l){ lat = l; }
    public void setLng(double l){ lng = l; }
    public void setVoteCount() { voteCount++; }

    public String getName(){ return name; }
    public String getAddress(){ return address; }
    public String getPhone(){ return phone; }
    public float getRating(){ return rating; }
    public boolean getState(){ return state;}
    public double getLat(){ return lat;}
    public double getLng(){ return lng; }
    public String getId(){ return id; }
    public int getVoteCount(){ return voteCount; }
    public String toString(){
        return "Name: " + name + ", Address: " + address + ", Rating: " + rating + ", ID: " + id;
    }
}
