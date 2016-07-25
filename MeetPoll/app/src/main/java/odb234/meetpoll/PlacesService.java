package odb234.meetpoll;


public class PlacesService {

    private String API_KEY;
    private static final String TAG = "Inside PlacesService";

    public PlacesService(String apikey) {
        this.API_KEY = apikey;
    }


    public String makeUrl(double latitude, double longitude, String type, String keyword, int radius) {
        StringBuilder urlString = new StringBuilder(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?");


            urlString.append("&location=");
            urlString.append(Double.toString(latitude));
            urlString.append(",");
            urlString.append(Double.toString(longitude));
            urlString.append("&radius=" + radius);
            urlString.append("&type=" + type.toLowerCase());
            if(!keyword.equals("Any")) {
                keyword = keyword.replaceAll("\\s+", "%20").toLowerCase();
                urlString.append("&keyword=" + keyword);
            }
            urlString.append("&key=" + API_KEY);

        return urlString.toString();
    }

}