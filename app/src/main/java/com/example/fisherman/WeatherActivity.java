package com.example.fisherman;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.annotations.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeatherActivity extends AppCompatActivity {
    private RecyclerView rvHourlyForecast;
    private HourlyWeatherAdapter hourlyWeatherAdapter;
    private List<HourlyWeatherData> hourlyWeatherDataList = new ArrayList<>();
    private TextView tvLocation;
    private TextView tvDate;

    // WeatherActivity.java

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        tvLocation = findViewById(R.id.tvLocation);
        tvDate = findViewById(R.id.tvDate);
        rvHourlyForecast = findViewById(R.id.rvHourlyForecast);
        rvHourlyForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hourlyWeatherAdapter = new HourlyWeatherAdapter(hourlyWeatherDataList);
        rvHourlyForecast.setAdapter(hourlyWeatherAdapter);

        String weatherData = getIntent().getStringExtra("weatherData");
        String targetDate = getIntent().getStringExtra("targetDate");

        if (weatherData != null && targetDate != null) {
            try {
                JSONObject jsonObject = new JSONObject(weatherData);
                JSONArray list = jsonObject.getJSONArray("list");
                JSONObject cityObject = jsonObject.getJSONObject("city");
                String cityName = cityObject.getString("name");

                for (int i = 0; i < list.length(); i++) {
                    JSONObject entry = list.getJSONObject(i);
                    String dateTime = entry.getString("dt_txt");
                    String date = dateTime.split(" ")[0];
                    String time = dateTime.split(" ")[1];

                    if (date.equals(targetDate)) {
                        // Quitar los segundos de la hora
                        String timeWithoutSeconds = time.substring(0, 5);

                        double temp = entry.getJSONObject("main").getDouble("temp");
                        String description = entry.getJSONArray("weather").getJSONObject(0).getString("description");
                        String icon = entry.getJSONArray("weather").getJSONObject(0).getString("icon");

                        hourlyWeatherDataList.add(new HourlyWeatherData(timeWithoutSeconds, temp, description, icon));
                    }
                }

                hourlyWeatherAdapter.notifyDataSetChanged();
                tvLocation.setText(cityName);
                tvDate.setText(targetDate);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCurrentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.now().format(formatter);
    }
}
