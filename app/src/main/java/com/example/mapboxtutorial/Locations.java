package com.example.mapboxtutorial;

public class Locations {

    private String Name;
    private String Type;
    private double longitude;
    private double latitude;
    private String website;

    public String getName() {
        return Name;
    }

    public String getType() {
        return Type;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getWebsite() {
        return website;
    }

    public Locations(String name, String type, double longitude, double latitude, String website) {
        Name = name;
        Type = type;
        this.longitude = longitude;
        this.latitude = latitude;
        this.website = website;
    }



}
