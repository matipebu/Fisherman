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

public class BuscarUsuarioAdapter extends RecyclerView.Adapter<BuscarUsuarioAdapter.PerfilViewHolder> {

    private List<Perfil> listaPerfiles;
    private OnItemClickListener onItemClickListener;

    public BuscarUsuarioAdapter(List<Perfil> listaPerfiles) {
        this.listaPerfiles = listaPerfiles;
    }

    @NonNull
    @Override
    public PerfilViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buscar_usuario, parent, false);
        return new PerfilViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PerfilViewHolder holder, int position) {
        Perfil perfil = listaPerfiles.get(position);
        holder.bind(perfil);
    }

    @Override
    public int getItemCount() {
        return listaPerfiles.size();
    }

    public void actualizarDatos(List<Perfil> nuevosPerfiles) {
        listaPerfiles.clear();
        listaPerfiles.addAll(nuevosPerfiles);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Perfil perfil);
    }

    public class PerfilViewHolder extends RecyclerView.ViewHolder {
        private ImageView imagenPerfil;
        private TextView textViewNombrePerfil;

        public PerfilViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenPerfil = itemView.findViewById(R.id.imagePerfil);
            textViewNombrePerfil = itemView.findViewById(R.id.textViewNombrePerfil);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClick(listaPerfiles.get(position));
                        }
                    }
                }
            });
        }

        public void bind(Perfil perfil) {
            // Actualizar la interfaz de usuario con la información del perfil
            textViewNombrePerfil.setText(perfil.getName());
            // Cargar la imagen de perfil con Picasso (o cualquier otra biblioteca de carga de imágenes)
            Picasso.get().load(perfil.getUrl()).into(imagenPerfil);
        }
    }
}
