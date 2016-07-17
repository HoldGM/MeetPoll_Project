package odb234.meetpoll;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLDataException;
import java.sql.SQLException;

/**
 * Created by odb234 on 7/12/16.
 */
public class DatabaseConnector {
    private static final String tag = "DATABASE PROBLEM:";
    private static final String DATABASE_NAME = "EventsDB";
    private SQLiteDatabase database;
    private DatabaseOpenHelper databaseOpenHelper;

    public DatabaseConnector(Context context){
        databaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
        database = databaseOpenHelper.getWritableDatabase();
    }

    public void open() throws SQLException{
//        create or open database
        database = databaseOpenHelper.getWritableDatabase();
        Log.d(tag, database.toString());
    }

    public void close(){
        if(database != null)
            database.close();
    }

    public void insertEvent(String hostName, String eventName, String eventLocation, int radius,
                            String date, String time, String locationType,
                            String locationSubtype, String price, int rating){
        ContentValues newEvent = createEvent(hostName, eventLocation, radius, date, time, locationType, locationSubtype, price, rating);

        newEvent.put("event_name", eventName);
//        try{
//            open();
            database.insert("events", null, newEvent);
//            close();
//            Log.d(tag, "Event insert completed.");
//        }catch(SQLException e){
//            Log.d(tag, "Event insert failed.");
//        }
    }

    private ContentValues createEvent(String hostName, String eventLocation, int radius,
                                      String date, String time, String locationType,
                                      String locationSubtype, String price, int rating){
        ContentValues result = new ContentValues();
        result.put("host_name", hostName);
        result.put("event_location", eventLocation);
        result.put("radius", radius);
        result.put("date", date);
        result.put("time", time);
        result.put("location_type", locationType);
        result.put("location_subtype", locationSubtype);
        result.put("price", price);
        result.put("rating", rating);

        return result;
    }

    public void updateEvent(long id, String hostName, String eventName, String eventlocation,
                            int radius, String date, String time, String locationType,
                            String locationSubtype, String price, int rating){
        ContentValues editEvent = createEvent(hostName, eventlocation, radius, date, time, locationType, locationSubtype, price, rating);
        try{
            open();
            database.update("events", editEvent, "_id=" + id, null);
//            close();
        }catch(SQLException e){
            Log.d(tag, "update event failed.");
        }
    }

    public String[] getAllEvents() {
        Cursor c = database.query(true, "events", new String[]{"host_name", "event_name", "event_location", "rating"}, null, null, null, null, null, null);

        if (c.getCount() > 0) {
            String[] tempList = new String[c.getCount()];
            if (c != null) {
                c.moveToFirst();
                tempList[0] = c.getString(c.getColumnIndex("host_name")) + " ";
                tempList[0] += c.getString(c.getColumnIndex("event_name")) + " ";
                tempList[0] += c.getString(c.getColumnIndex("event_location")) + " ";
                tempList[0] += c.getString(c.getColumnIndex("rating")) + " ";

            }

            int i = 1;
            while (c.moveToNext()) {
                tempList[i] = c.getString(c.getColumnIndex("host_name")) + " ";
                tempList[i] += c.getString(c.getColumnIndex("event_name")) + " ";
                tempList[i] += c.getString(c.getColumnIndex("event_location")) + " ";
                tempList[i] += c.getString(c.getColumnIndex("rating"));
                i++;
            }
            return tempList;
        }
        return null;
    }

    public Cursor getOneEvent(long id){
        return database.query("events", null, "_id=" + id, null, null, null, null);
    }

    public void deleteEvent(long id){
        try{
            open();
            database.delete("events", "_id=" + id, null);
            close();
        }catch(SQLException e){
            Log.d(tag, "Event delete failed");
        }
    }

    public void deleteAllEvents(){
//        try{
//            open();
            database.delete("events", null, null);
//            close();
//        }catch(SQLException e){
//            Log.d(tag, "Things happens to the database.");
//        }
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper {


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){}

        public DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
            super(context, name, factory, version);
        }
        @Override
        public void onCreate(SQLiteDatabase db){

            Log.d(tag, "database oncreate here");
            String createQuery = "CREATE TABLE events" + "(_id INTEGER PRIMARY KEY autoincrement," +
                    "host_name TEXT," +
                    "event_name TEXT," +
                    "event_location TEXT," +
                    "radius INTEGER," +
                    "date TEXT," +
                    "time TEXT," +
                    "location_type TEXT," +
                    "location_subtype TEXT," +
                    "price TEXT," +
                    "rating INTEGER);";
            db.execSQL(createQuery);
        }
    }
}
