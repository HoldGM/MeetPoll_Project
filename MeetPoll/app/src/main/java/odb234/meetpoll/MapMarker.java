package odb234.meetpoll;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

/**
 * Created by odb234 on 7/21/16.
 */
public class MapMarker{
    private LatLng latLng;
    private String address;
    private String id;
    private float rating;
    private String name;

    public MapMarker(LatLng ll, String add, String newId, float r, String n){
        latLng = ll;
        address = add;
        id = newId;
        rating = r;
        name = n;
    }

    public MapMarker(LatLng ll, String n){
        name = n;
        latLng = ll;
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
    public float getRating(){
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
    public void setRating(float r){
        this.rating = r;
    }
    public void setName(String n){
        this.name = n;
    }
}
