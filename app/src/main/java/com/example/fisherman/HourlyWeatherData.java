package com.example.fisherman;

public class HourlyWeatherData {
    private String time;
    private double temperature;
    private String description;
    private String icon;

    public HourlyWeatherData(String time, double temperature, String description, String icon) {
        this.time = time;
        this.temperature = temperature;
        this.description = description;
        this.icon = icon;
    }

    // Getters
    public String getTime() { return time; }
    public double getTemperature() { return temperature; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}
