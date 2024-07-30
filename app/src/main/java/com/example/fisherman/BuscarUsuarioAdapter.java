package com.example.fisherman;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
/**
 * La clase `BuscarUsuarioAdapter` es un adaptador personalizado para la lista de perfiles en la actividad de búsqueda de usuarios.
 */
public class BuscarUsuarioAdapter extends RecyclerView.Adapter<BuscarUsuarioAdapter.PerfilViewHolder> {

    private List<Perfil> listaPerfiles;
    private OnItemClickListener onItemClickListener;
    private Context context;

    /**
     * Constructor que inicializa el adaptador con una lista de perfiles.
     * @param listaPerfiles Lista de perfiles a mostrar.
     */
    public BuscarUsuarioAdapter(List<Perfil> listaPerfiles) {
        this.listaPerfiles = listaPerfiles;
    }

    @NonNull
    @Override
    public PerfilViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el diseño de la vista del elemento de la lista
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buscar_usuario, parent, false);
        return new PerfilViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PerfilViewHolder holder, int position) {
        // Obtener el perfil en la posición dada y vincularlo a la vista del elemento de la lista
        Perfil perfil = listaPerfiles.get(position);
        holder.bind(perfil);

        // Configurar el clic en el elemento para abrir la actividad de chat
        holder.itemView.setOnClickListener(v -> {
            // Obtener el otherUserId y pasarlo a la actividad de chat
            String otherUserId = perfil.getUid();

            // Llamar al método onItemClick del listener
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(perfil, otherUserId);
            }
        });
    }


    @Override
    public int getItemCount() {
        return listaPerfiles.size();
    }
    /**
     * Método para actualizar la lista de perfiles con nuevos datos.
     * @param nuevosPerfiles Lista de nuevos perfiles a mostrar.
     */
    public void actualizarDatos(List<Perfil> nuevosPerfiles) {
        listaPerfiles.clear();
        listaPerfiles.addAll(nuevosPerfiles);
        notifyDataSetChanged();
    }
    /**
     * Establecer un objeto de escucha para manejar eventos de clic en los elementos de la lista.
     * @param listener Objeto de escucha de clic en elementos de la lista.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Perfil perfil, String otherUserId);
    }

    /**
     * Clase interna que representa la vista de un elemento de la lista.
     */
    public class PerfilViewHolder extends RecyclerView.ViewHolder {
        private ImageView imagenPerfil;
        private TextView textViewNombrePerfil;

        /**
         * Constructor que inicializa los elementos de la vista del elemento de la lista.
         * @param itemView Vista del elemento de la lista.
         */
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
                            onItemClickListener.onItemClick(listaPerfiles.get(position), ""); // Puedes dejar el segundo parámetro en blanco o null
                        }
                    }
                }
            });
        }

        /**
         * Método para actualizar la vista del elemento de la lista con la información del perfil.
         * @param perfil Objeto Perfil que contiene la información del perfil a mostrar.
         */
        public void bind(Perfil perfil) {
            // Actualizar la interfaz de usuario con la información del perfil
            textViewNombrePerfil.setText(perfil.getName());
            // Cargar la imagen de perfil con Picasso (o cualquier otra biblioteca de carga de imágenes)
            Picasso.get().load(perfil.getUrl()).into(imagenPerfil);
        }
    }
}