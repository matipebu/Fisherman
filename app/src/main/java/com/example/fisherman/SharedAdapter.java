package com.example.fisherman;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SharedAdapter extends RecyclerView.Adapter<SharedAdapter.ViewHolder> {

    private List<Publicacion> savedPublications;
    private Context context;
    private FirebaseFirestore db;

    public SharedAdapter(List<Publicacion> savedPublications, Context context, FirebaseFirestore db) {
        this.savedPublications = savedPublications;
        this.context = context;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shared, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Publicacion savedPublication = savedPublications.get(position);

        holder.textViewContenido.setText(savedPublication.getContenido());

        if (savedPublication.getMediaUrl() != null && !savedPublication.getMediaUrl().isEmpty()) {
            Glide.with(context).load(savedPublication.getMediaUrl()).into(holder.imageViewMedia);
            holder.imageViewMedia.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewMedia.setVisibility(View.GONE);
        }

        // Puedes personalizar la visualización de otros campos de la publicación según tus necesidades.
    }

    @Override
    public int getItemCount() {
        return savedPublications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewContenido;
        ImageView imageViewMedia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContenido = itemView.findViewById(R.id.textViewContenidoSaved);
            imageViewMedia = itemView.findViewById(R.id.imageViewMediaSaved);
        }
    }
}
