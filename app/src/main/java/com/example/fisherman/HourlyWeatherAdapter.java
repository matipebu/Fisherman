package com.example.fisherman;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class HourlyWeatherAdapter extends RecyclerView.Adapter<HourlyWeatherAdapter.ViewHolder> {
    private List<HourlyWeatherData> hourlyWeatherDataList;

    public HourlyWeatherAdapter(List<HourlyWeatherData> hourlyWeatherDataList) {
        this.hourlyWeatherDataList = hourlyWeatherDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourlyWeatherData data = hourlyWeatherDataList.get(position);
        holder.tvTime.setText(data.getTime());
        holder.tvTemperature.setText(String.format("%.1f°C", data.getTemperature()));
        holder.tvDescription.setText(data.getDescription());

        // Usa Picasso para cargar la imagen
        String iconUrl = "http://openweathermap.org/img/wn/" + data.getIcon() + "@2x.png";
        Picasso.get()
                .load(iconUrl)
                .resize(48, 48)  // Ajusta el tamaño de la imagen aquí
                .centerInside()  // Ajusta la imagen dentro de los límites de ImageView
                .into(holder.ivIcon);
    }

    @Override
    public int getItemCount() {
        return hourlyWeatherDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvTemperature;
        TextView tvDescription;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
