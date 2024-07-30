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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {

    private List<Publicacion> listaPublicaciones;
    private Context context;
    private FirebaseFirestore db;
    private String currentUserUid;
    private FirebaseAuth auth;
    private boolean isSavedList;


    public PublicacionAdapter(List<Publicacion> listaPublicaciones, Context context, FirebaseFirestore db, String currentUserUid, FirebaseAuth auth, boolean isSavedList ) {
        this.listaPublicaciones = listaPublicaciones;
        this.context = context;
        this.db = db;
        this.currentUserUid = currentUserUid;
        this.auth = auth;
        this.isSavedList = isSavedList;



    }

    public void setListaPublicaciones(List<Publicacion> listaPublicaciones) {
        this.listaPublicaciones = listaPublicaciones;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PublicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_publicacion, parent, false);
        return new PublicacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionViewHolder holder, int position) {

            Publicacion publicacion = listaPublicaciones.get(position);
            holder.textViewContenido.setText(publicacion.getContenido());

            if (publicacion.getMediaUrl() != null && !publicacion.getMediaUrl().isEmpty()) {
                Glide.with(context).load(publicacion.getMediaUrl()).into(holder.imageViewMedia);
                holder.imageViewMedia.setVisibility(View.VISIBLE);
            } else {
                holder.imageViewMedia.setVisibility(View.GONE);
            }

            cargarInformacionUsuario(publicacion.getUserId(), holder);

        // Configurar otros elementos visuales de la publicación (título, contenido, etc.)
        holder.textViewContenido.setText(publicacion.getContenido());

        // Verificar si el usuario actual ha dado like
        verificarLike(publicacion.getDocumentId(), holder.imageViewLike);


        DocumentReference likeRef = db.collection("publicaciones").document(publicacion.getDocumentId())
                .collection("likes").document(currentUserUid);

        likeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // El usuario ya ha dado like
                holder.likeButton.setImageResource(R.drawable.ic_like); // Cambia el ícono a "liked"
                holder.likeButton.setTag(true); // Marca el botón como "liked"
            } else {
                // El usuario no ha dado like
                holder.likeButton.setImageResource(R.drawable.ic_liker); // Cambia el ícono a "like"
                holder.likeButton.setTag(false); // Marca el botón como "no liked"
            }
        });

        holder.likeButton.setOnClickListener(v -> {
            boolean yaHaDadoLike = (boolean) holder.likeButton.getTag();
            ((MainActivity) context).actualizarLikeEnFirestore(publicacion.getDocumentId(), yaHaDadoLike);
            // Cambiar el estado del botón y el ícono
            if (yaHaDadoLike) {
                holder.likeButton.setImageResource(R.drawable.ic_liker); // Cambia el ícono a "like"
                holder.likeButton.setTag(false); // Marca el botón como "no liked"
            } else {
                holder.likeButton.setImageResource(R.drawable.ic_like); // Cambia el ícono a "liked"
                holder.likeButton.setTag(true); // Marca el botón como "liked"
            }
        });

        holder.saveButton.setOnClickListener(v -> {
            if (holder.saveButton.getTag() != null && (boolean) holder.saveButton.getTag()) {
                removerDeGuardados(publicacion, holder.saveButton);
            } else {
                agregarAGuardados(publicacion, holder.saveButton);
            }
        });
        if (isSavedList) {
            holder.saveButton.setImageResource(R.drawable.ic_guard_s);  // Siempre mostrar ícono de guardado
            holder.saveButton.setTag(true);  // Marca como guardado
        } else {
            verificarSiGuardado(publicacion, holder.saveButton);  // Verifica el estado de guardado
        }

    }

    private void cargarInformacionUsuario(String userId, PublicacionViewHolder holder) {
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

    private void verificarLike(String publicacionId, ImageView imageViewLike) {
        DocumentReference likeRef = db.collection("publicaciones").document(publicacionId)
                .collection("likes").document(currentUserUid);

        likeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // El usuario ya ha dado like
                    imageViewLike.setImageResource(R.drawable.ic_like); // Cambiar icono a "liked"
                } else {
                    // El usuario no ha dado like
                    imageViewLike.setImageResource(R.drawable.ic_liker); // Mantener icono como "like"
                }
            } else {
                Log.e("PublicacionAdapter", "Error al verificar like existente", task.getException());
            }
        });
    }
    private void agregarAGuardados(Publicacion publicacion, ImageView saveButton) {
        DocumentReference savedPublicationRef = db.collection("usuarios").document(currentUserUid)
                .collection("savedPublications").document(publicacion.getDocumentId());

        Map<String, Object> data = new HashMap<>();
        data.put("publicacionId", publicacion.getDocumentId());

        savedPublicationRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    saveButton.setImageResource(R.drawable.ic_guard_s);
                    saveButton.setTag(true);
                    Toast.makeText(context, "Publicación guardada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("PublicacionAdapter", "Error al guardar publicación", e);
                    Toast.makeText(context, "Error al guardar publicación", Toast.LENGTH_SHORT).show();
                });
    }

    private void removerDeGuardados(Publicacion publicacion, ImageView saveButton) {
        DocumentReference savedPublicationRef = db.collection("usuarios").document(currentUserUid)
                .collection("savedPublications").document(publicacion.getDocumentId());

        savedPublicationRef.delete()
                .addOnSuccessListener(aVoid -> {
                    saveButton.setImageResource(R.drawable.ic_guard_r);
                    saveButton.setTag(false);
                    Toast.makeText(context, "Publicación removida de guardados", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("PublicacionAdapter", "Error al remover publicación de guardados", e);
                    Toast.makeText(context, "Error al remover publicación de guardados", Toast.LENGTH_SHORT).show();
                });
    }

    private void verificarSiGuardado(Publicacion publicacion, ImageView saveButton) {
        DocumentReference savedPublicationRef = db.collection("usuarios").document(currentUserUid)
                .collection("savedPublications").document(publicacion.getDocumentId());

        savedPublicationRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                saveButton.setImageResource(R.drawable.ic_guard_s);
                saveButton.setTag(true);
            } else {
                saveButton.setImageResource(R.drawable.ic_guard_r);
                saveButton.setTag(false);
            }
        }).addOnFailureListener(e -> {
            Log.e("PublicacionAdapter", "Error al verificar si está guardado", e);
        });
    }

        @Override
    public int getItemCount() {
        return listaPublicaciones.size();
    }

    public static class PublicacionViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitulo, textViewContenido;
        ImageView imageViewLike;
        ImageView imageViewMedia, imageViewPerfil;
        TextView textViewNombrePerfil;
        ImageView likeButton, saveButton;


        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitulo = itemView.findViewById(R.id.textViewTitle);
            textViewContenido = itemView.findViewById(R.id.textViewContenido);
            imageViewLike = itemView.findViewById(R.id.likeButton);
            textViewContenido = itemView.findViewById(R.id.textViewContenido);
            imageViewMedia = itemView.findViewById(R.id.imageViewMedia);
            imageViewPerfil = itemView.findViewById(R.id.imagePerfil);
            textViewNombrePerfil = itemView.findViewById(R.id.textViewNombrePerfil);
            likeButton = itemView.findViewById(R.id.likeButton);
            saveButton = itemView.findViewById(R.id.saveButton);
        }

    }

}



