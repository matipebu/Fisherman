package com.example.fisherman;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SavedPublicationsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private RecyclerView recyclerViewSavedPublications;
    private PublicacionAdapter publicacionAdapter;
    private List<Publicacion> listaPublicaciones;
    private FirebaseFirestore db;
    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_publications);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerViewSavedPublications = findViewById(R.id.recyclerViewPublicaciones);
        recyclerViewSavedPublications.setLayoutManager(new LinearLayoutManager(this));
        listaPublicaciones = new ArrayList<>();

        publicacionAdapter = new PublicacionAdapter(listaPublicaciones, this, db, currentUserUid,auth,true);
        recyclerViewSavedPublications.setAdapter(publicacionAdapter);

        obtenerPublicacionesGuardadas();
    }

    private void obtenerPublicacionesGuardadas() {
        listaPublicaciones.clear();

        db.collection("usuarios").document(currentUserUid)
                .collection("savedPublications")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String publicacionId = document.getString("publicacionId");
                            db.collection("publicaciones").document(publicacionId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        Publicacion publicacion = documentSnapshot.toObject(Publicacion.class);
                                        if (publicacion != null) {
                                            publicacion.setDocumentId(publicacionId);
                                            listaPublicaciones.add(publicacion);
                                            publicacionAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SavedPublicationsActivity.this, "Error al obtener las publicaciones guardadas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarPublicacion(String publicacionId) {
        db.collection("publicaciones").document(publicacionId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Publicacion publicacion = documentSnapshot.toObject(Publicacion.class);
                    if (publicacion != null) {
                        publicacion.setDocumentId(publicacionId);
                        listaPublicaciones.add(publicacion);
                        publicacionAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SavedPublicationsActivity", "Error al cargar publicación", e);
                    Toast.makeText(SavedPublicationsActivity.this, "Error al cargar publicación", Toast.LENGTH_SHORT).show();
                });
    }
}
