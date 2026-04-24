package com.example.movementtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    ListView historyList;
    DatabaseHelper dbHelper;
    ArrayList<String> dateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyList = findViewById(R.id.historyList);
        dbHelper = new DatabaseHelper(this);

        loadDates();
    }

    private void loadDates() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT date FROM location_history ORDER BY date DESC LIMIT 3",
                null
        );

        dateList = new ArrayList<>();

        while (cursor.moveToNext()) {
            String date = cursor.getString(0);
            dateList.add("📁 " + date);
        }

        cursor.close();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        dateList);

        historyList.setAdapter(adapter);


        historyList.setOnItemClickListener((parent, view, position, id) -> {

            String selectedDate = dateList.get(position).replace("📁 ", "");

            Intent intent = new Intent(HistoryActivity.this, DateHistoryActivity.class);
            intent.putExtra("date", selectedDate);
            startActivity(intent);
        });
    }
}
