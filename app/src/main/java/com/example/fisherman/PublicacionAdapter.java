package com.example.fisherman;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.ViewHolder> {

    private List<Publicacion> listaPublicaciones;
    private Context context;

    private FirebaseFirestore db;
    private String currentUserUid;
    private OnItemClickListener listener;

    private FirebaseAuth auth;

    public PublicacionAdapter(List<Publicacion> listaPublicaciones, Context context, FirebaseFirestore db, String currentUserUid, FirebaseAuth auth, OnItemClickListener listener) {
        this.auth = auth;
        this.listaPublicaciones = listaPublicaciones;
        this.context = context;
        this.db = db;
        this.currentUserUid = currentUserUid;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onLikeClick(int position);

        void onSaveClick(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publicacion, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Publicacion publicacion = listaPublicaciones.get(position);
        holder.textViewContenido.setText(publicacion.getContenido());

        // Si hay una URL de imagen en la publicación, la mostramos
        if (publicacion.getMediaUrl() != null && !publicacion.getMediaUrl().isEmpty()) {
            Glide.with(context).load(publicacion.getMediaUrl()).into(holder.imageViewMedia);
            holder.imageViewMedia.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewMedia.setVisibility(View.GONE);
        }

        cargarInformacionUsuario(publicacion.getUserId(), holder);
    }

    public void actualizarLikes(int position) {
        Publicacion publicacion = listaPublicaciones.get(position);
        String documentId = publicacion.getDocumentId();

        if (documentId != null && !documentId.isEmpty()) {
            DocumentReference publicacionRef = db.collection("publicaciones").document(documentId);

            // Actualizar el campo "likes" usando FieldValue.increment(1)
            publicacionRef.update("likes", FieldValue.increment(1))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                notifyDataSetChanged();
                            } else {
                                Log.e("PublicacionAdapter", "Error al actualizar likes", task.getException());
                            }
                        }
                    });
        } else {
            Log.e("PublicacionAdapter", "documentId es nulo o vacío para la posición " + position);
        }
    }




    private void cargarInformacionUsuario(String userId, ViewHolder holder) {
        // Obtener la referencia del documento del usuario en Firestore
        DocumentReference userRef = db.collection("usuarios").document(userId);

        // Leer datos del usuario desde Firestore
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Obtener datos del documento y llenar los campos
                        Perfil perfilUsuario = document.toObject(Perfil.class);
                        if (perfilUsuario != null) {
                            // Cargar la imagen del perfil del usuario en el círculo
                            if (perfilUsuario.getUrl() != null && !perfilUsuario.getUrl().isEmpty()) {
                                Glide.with(context).load(perfilUsuario.getUrl()).into(holder.imageViewPerfil);
                            }

                            // Mostrar el nombre del perfil del usuario
                            holder.textViewNombrePerfil.setText(perfilUsuario.getName());
                        }
                    }
                } else {
                    Log.e("PublicacionAdapter", "Error al cargar datos del usuario", task.getException());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaPublicaciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewContenido;
        ImageView imageViewMedia, imageViewPerfil;
        TextView textViewNombrePerfil;
        ImageView likeButton, saveButton;
        OnItemClickListener listener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            textViewContenido = itemView.findViewById(R.id.textViewContenido);
            imageViewMedia = itemView.findViewById(R.id.imageViewMedia);
            imageViewPerfil = itemView.findViewById(R.id.imagePerfil);
            textViewNombrePerfil = itemView.findViewById(R.id.textViewNombrePerfil);
            likeButton = itemView.findViewById(R.id.likeButton);
            saveButton = itemView.findViewById(R.id.saveButton);
            this.listener = listener;

            likeButton.setOnClickListener(this);
            saveButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (listener != null) {
                if (view.getId() == R.id.likeButton) {
                    listener.onLikeClick(position);
                } else if (view.getId() == R.id.saveButton) {
                    listener.onSaveClick(position);
                }
            }
        }
    }
}


