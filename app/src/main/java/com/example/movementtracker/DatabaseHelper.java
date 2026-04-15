package com.example.movementtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LocationDB";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE location_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "location TEXT," +
                "start_time TEXT," +
                "end_time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS location_history");
        onCreate(db);
    }

    public void insertLocation(String location, String start, String end) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("location", location);
        values.put("start_time", start);
        values.put("end_time", end);

        db.insert("location_history", null, values);
    }
}
