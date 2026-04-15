package com.example.movementtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    ListView historyList;
    DatabaseHelper dbHelper;
    ArrayList<String> locationHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyList = findViewById(R.id.historyList);
        dbHelper = new DatabaseHelper(this);

        loadHistory();
    }

    private void loadHistory() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM location_history", null);

        locationHistory = new ArrayList<>();

        while (cursor.moveToNext()) {

            String location = cursor.getString(1);
            String start = cursor.getString(2);
            String end = cursor.getString(3);

            String record = "📍 Location: " + location +
                    "\n⏱ From: " + start +
                    "\n⏱ To: " + end;

            locationHistory.add(record);
        }

        cursor.close();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        locationHistory);

        historyList.setAdapter(adapter);
    }
}
