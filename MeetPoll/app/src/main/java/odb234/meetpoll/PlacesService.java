package odb234.meetpoll;


public class PlacesService {

    private String API_KEY;
    private static final String TAG = "Inside PlacesService";

    public PlacesService(String apikey) {
        this.API_KEY = apikey;
    }


    public String makeUrl(double latitude, double longitude, String type) {
        StringBuilder urlString = new StringBuilder(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?");

            urlString.append("&location=");
            urlString.append(Double.toString(latitude));
            urlString.append(",");
            urlString.append(Double.toString(longitude));
            urlString.append("&rankby=prominence");
            urlString.append("&radius=10000");
            urlString.append("&type=" + type.toLowerCase());
            urlString.append("&key=" + API_KEY);

        return urlString.toString();
    }

}