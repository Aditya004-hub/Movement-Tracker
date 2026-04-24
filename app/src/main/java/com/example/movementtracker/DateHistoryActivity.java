package com.example.movementtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class DateHistoryActivity extends AppCompatActivity {

    ListView listView;
    DatabaseHelper dbHelper;
    ArrayList<String> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_history);

        listView = findViewById(R.id.dateHistoryList);
        dbHelper = new DatabaseHelper(this);

        String selectedDate = getIntent().getStringExtra("date");

        loadHistory(selectedDate);
    }

    private void loadHistory(String date) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM location_history WHERE date=?",
                new String[]{date}
        );

        historyList = new ArrayList<>();

        while (cursor.moveToNext()) {

            String location = cursor.getString(1);
            String lat = cursor.getString(2);
            String lon = cursor.getString(3);
            String start = cursor.getString(5);
            String end = cursor.getString(6);

            String record = "📍 " + location +
                    "\n🌐 " + lat + ", " + lon +
                    "\n⏱ " + start + " - " + end;

            historyList.add(record);
        }

        cursor.close();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        historyList);

        listView.setAdapter(adapter);
    }
}
