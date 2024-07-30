package com.example.fisherman;

public class TideStation {
    private String id;
    private String code;
    private String puerto;
    private String lat;
    private String lon;

    // Constructor
    public TideStation(String id, String code, String puerto, String lat, String lon) {
        this.id = id;
        this.code = code;
        this.puerto = puerto;
        this.lat = lat;
        this.lon = lon;
    }

    // Getters
    public String getId() { return id; }
    public String getCode() { return code; }
    public String getPuerto() { return puerto; }
    public String getLat() { return lat; }
    public String getLon() { return lon; }

    @Override
    public String toString() {
        return "TideStation{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", puerto='" + puerto + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                '}';
    }
}
