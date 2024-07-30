package com.example.fisherman;

public class Port {
    private String id;
    private String code;
    private String puerto;
    private double latitude;
    private double longitude;

    public Port(String id, String code, String puerto, double latitude, double longitude) {
        this.id = id;
        this.code = code;
        this.puerto = puerto;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters y setters
    public String getId() { return id; }
    public String getCode() { return code; }
    public String getPuerto() { return puerto; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
