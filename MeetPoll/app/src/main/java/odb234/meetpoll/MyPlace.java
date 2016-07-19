package odb234.meetpoll;

import android.net.Uri;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyPlace implements Place {
    private String id;

    private String name;

    private Double latitude;
    private Double longitude;

    private double rating;

    public String getId() {
        return id;
    }

    @Override
    public List<Integer> getPlaceTypes() {
        return null;
    }

    @Override
    public CharSequence getAddress() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String getName() {
        return name;
    }

    @Override
    public LatLng getLatLng() {
        LatLng latLng = new LatLng(latitude, longitude);
        return latLng;
    }

    @Override
    public LatLngBounds getViewport() {
        return null;
    }

    @Override
    public Uri getWebsiteUri() {
        return null;
    }

    @Override
    public CharSequence getPhoneNumber() {
        return null;
    }

    @Override
    public float getRating() {
        return 0;
    }

    @Override
    public int getPriceLevel() {
        return 0;
    }

    @Override
    public CharSequence getAttributions() {
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }


    static MyPlace jsonToPontoReferencia(JSONObject pontoReferencia) {
        try {
            MyPlace result = new MyPlace();
            JSONObject geometry = (JSONObject) pontoReferencia.get("geometry");
            JSONObject location = (JSONObject) geometry.get("location");
            result.setLatitude((Double) location.get("lat"));
            result.setLongitude((Double) location.get("lng"));
            result.setName(pontoReferencia.getString("name"));
            result.setId(pontoReferencia.getString("id"));
            return result;
        } catch (JSONException ex) {
            Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Place{" + "id=" + id + ", name=" + name + ", latitude=" + latitude + ", longitude=" + longitude + '}';
    }

    @Override
    public Place freeze() {
        return null;
    }

    @Override
    public boolean isDataValid() {
        return false;
    }
}