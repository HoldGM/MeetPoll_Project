package odb234.meetpoll;


import com.google.android.gms.maps.model.LatLng;

/**
 * Created by odb234 on 7/21/16.
 */
public class MapMarker {
    private LatLng latLng;
    private String address;
    private String id;
    private double rating;
    private String name;

    public MapMarker(LatLng ll, String add, String newId, double r, String n){
        latLng = ll;
        address = add;
        id = newId;
        rating = r;
        name = n;
    }

    public LatLng getLatLng(){
        return latLng;
    }
    public String getAddress(){
        return address;
    }
    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public double getRating(){
        return rating;
    }
    public void setLatLng(LatLng ll){
        this.latLng = ll;
    }
    public void setAddress(String add){
        this.address = add;
    }
    public void setId(String id){
        this.id = id;
    }
    public void setRating(double r){
        this.rating = r;
    }
    public void setName(String n){
        this.name = n;
    }
}
