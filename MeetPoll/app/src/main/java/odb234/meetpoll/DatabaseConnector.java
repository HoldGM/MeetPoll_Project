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
    private static final String DATABASE_NAME = "EventsDB001";
    private SQLiteDatabase database;
    private DatabaseOpenHelper databaseOpenHelper;

    private static final String[] query = new String[] {"_id", "host_name", "event_name", "event_location", "date", "time", "location_type", "location_subtype", "rating", "placeid_1", "placeid_2", "placeid_3", "placeid_4", "placeid_5", "placeid_6", "placeid_7", "placeid_8", "placeid_9", "placeid_10"};

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

    public void insertEvent(String hostName, String eventName, String eventLocation,
                            String date, String time, String locationType,
                            String locationSubtype, int rating, String[] ids){
        ContentValues newEvent = createEvent(hostName, eventLocation, date, time, locationType, locationSubtype, rating, ids);

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

    private ContentValues createEvent(String hostName, String eventLocation,
                                      String date, String time, String locationType,
                                      String locationSubtype, int rating, String[] ids){
        ContentValues result = new ContentValues();
        result.put("host_name", hostName);
        result.put("event_location", eventLocation);
        result.put("date", date);
        result.put("time", time);
        result.put("location_type", locationType);
        result.put("location_subtype", locationSubtype);
        result.put("rating", rating);
        for (int i = 0; i < ids.length; i++){
            String str = "placeid_" + (i + 1);
            Log.d(tag, str);
            result.put(str, ids[i]);
        }

        return result;
    }

    public void updateEvent(long id, String hostName, String eventName, String eventlocation,
                            String date, String time, String locationType,
                            String locationSubtype, int rating, String[] ids){
        ContentValues editEvent = createEvent(hostName, eventlocation, date, time, locationType, locationSubtype, rating, ids);
        try{
            open();
            database.update("events", editEvent, "_id=" + id, null);
//            close();
        }catch(SQLException e){
            Log.d(tag, "update event failed.");
        }
    }


    public Cursor getCursor() {
        try{
            this.open();
            return database.query(true, "events", query, null, null, null, null, null, null);
        }catch(Exception e){
            Log.d(tag, e.toString());
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
                    "date TEXT," +
                    "time TEXT," +
                    "location_type TEXT," +
                    "location_subtype TEXT," +
                    "rating INTEGER," +
                    "placeid_1 TEXT," +
                    "placeid_2 TEXT," +
                    "placeid_3 TEXT," +
                    "placeid_4 TEXT," +
                    "placeid_5 TEXT," +
                    "placeid_6 TEXT," +
                    "placeid_7 TEXT," +
                    "placeid_8 TEXT," +
                    "placeid_9 TEXT," +
                    "placeid_10 TEXT);";
            db.execSQL(createQuery);
        }
    }
}
