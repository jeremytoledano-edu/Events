package com.example.events.models;

public class NbaGame {
    private String name;
    private String date;

    public NbaGame(String name, String date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }
}