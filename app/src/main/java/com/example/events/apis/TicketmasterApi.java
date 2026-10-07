package com.example.events.apis;

import com.example.events.models.NbaGame;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicketmasterApi {

    public interface ApiCallback {
        void onSuccess(List<NbaGame> games);
        void onError(Exception e);
    }

    public static void fetchNbaGames(String apiKey, ApiCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // חישוב תאריכים: מעכשיו ועד עוד 6 חודשים בפורמט ISO 8601 UTC
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                Calendar calendar = Calendar.getInstance();
                String startDate = isoFormat.format(calendar.getTime());

                calendar.add(Calendar.MONTH, 6);
                String endDate = isoFormat.format(calendar.getTime());

                // בניית ה-URL
                String urlString = "https://app.ticketmaster.com/discovery/v2/events.json?" +
                        "apikey=" + apiKey +
                        "&keyword=NBA" +
                        "&startDateTime=" + startDate +
                        "&endDateTime=" + endDate +
                        "&sort=date,asc" +
                        "&size=50";

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // פירוס ה-JSON
                    List<NbaGame> gamesList = parseGamesJson(response.toString());
                    callback.onSuccess(gamesList);
                } else {
                    callback.onError(new Exception("HTTP Error: " + responseCode));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    private static List<NbaGame> parseGamesJson(String jsonResponse) throws Exception {
        List<NbaGame> games = new ArrayList<>();
        JSONObject root = new JSONObject(jsonResponse);

        if (root.has("_embedded")) {
            JSONObject embedded = root.getJSONObject("_embedded");
            JSONArray events = embedded.getJSONArray("events");

            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                String name = event.optString("name", "Unknown Game");

                String localDate = "No Date";
                if (event.has("dates")) {
                    JSONObject dates = event.getJSONObject("dates");
                    if (dates.has("start")) {
                        JSONObject start = dates.getJSONObject("start");
                        localDate = start.optString("localDate", "No Date");
                    }
                }

                games.add(new NbaGame(name, localDate));
            }
        }
        return games;
    }
}