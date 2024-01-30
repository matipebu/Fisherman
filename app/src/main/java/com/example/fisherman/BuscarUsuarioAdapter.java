package com.example.fisherman;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.List;

public class BuscarUsuarioAdapter extends RecyclerView.Adapter<BuscarUsuarioAdapter.PerfilViewHolder> {

    private List<Perfil> perfilList;


    // Constructor y métodos del adaptador
    public BuscarUsuarioAdapter(List<Perfil> perfiles) {
        this.perfilList = perfiles;
    }

    @NonNull
    @Override
    public PerfilViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buscar_usuario, parent, false);
        return new PerfilViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PerfilViewHolder holder, int position) {
        Perfil perfil = perfilList.get(position);

        // Configurar datos del perfil en la vista
        holder.textViewNombrePerfil.setText(perfil.getName());

        // Cargar la imagen de perfil con Glide directamente en CircleImageView
        if (perfil.getUrl() != null && !perfil.getUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(perfil.getUrl())
                    .placeholder(R.drawable.person_2_24) // Imagen de respaldo si no hay URL válida
                    .into(holder.imagePerfil);
        } else {
            // Si no hay URL de imagen, establecer una imagen de respaldo
            holder.imagePerfil.setImageResource(R.drawable.person_2_24);
        }
    }

    public void actualizarDatos(List<Perfil> nuevaLista) {
        perfilList.clear();
        perfilList.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return perfilList.size();
    }

    // Clase interna ViewHolder
    static class PerfilViewHolder extends RecyclerView.ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView imagePerfil;
        TextView textViewNombrePerfil;

        PerfilViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePerfil = itemView.findViewById(R.id.imagePerfil); // Asegúrate de usar el ID correcto
            textViewNombrePerfil = itemView.findViewById(R.id.textViewNombrePerfil);
        }
    }
}
