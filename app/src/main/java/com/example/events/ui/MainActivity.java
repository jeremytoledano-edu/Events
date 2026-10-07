package com.example.events.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.events.R;
import com.example.events.apis.TicketmasterApi;
import com.example.events.models.NbaGame;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "6K07bvQDEGTA8tZfS9mDaUU447MbONrN"; // הכנס את ה-API Key שלך כאן
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // קריאה לפונקציה הסטטית
        TicketmasterApi.fetchNbaGames(API_KEY, new TicketmasterApi.ApiCallback() {
            @Override
            public void onSuccess(List<NbaGame> games) {
                // עדכון ה-UI חייב להתבצע על ה-Main Thread
                runOnUiThread(() -> {
                    NbaAdapter adapter = new NbaAdapter(games);
                    recyclerView.setAdapter(adapter);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}