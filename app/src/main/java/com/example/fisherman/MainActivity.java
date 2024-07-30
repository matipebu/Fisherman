package com.example.fisherman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private RecyclerView recyclerViewPublicaciones;
    private PublicacionAdapter publicacionAdapter;
    private List<Publicacion> listaPublicaciones;
    private FirebaseFirestore db;
    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar RecyclerView y lista de publicaciones
        recyclerViewPublicaciones = findViewById(R.id.recyclerViewPublicaciones);
        recyclerViewPublicaciones.setLayoutManager(new LinearLayoutManager(this));
        listaPublicaciones = new ArrayList<>();
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        ImageView viewSavedPublications = findViewById(R.id.goSaved);
        viewSavedPublications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SavedPublicationsActivity.class));
            }
        });
        ImageView mapButton = findViewById(R.id.goMap);
        mapButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MapActivity.class)));



        // Crear la instancia de PublicacionAdapter y establecerla en el RecyclerView
        publicacionAdapter = new PublicacionAdapter(listaPublicaciones, this, db, currentUserUid, auth,false);



        recyclerViewPublicaciones.setAdapter(publicacionAdapter);

        // Llamada a obtenerPublicaciones
        obtenerPublicaciones();

        ImageView addPublicacionImageView = findViewById(R.id.addPublicacion);
        addPublicacionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirigir a la actividad Posts
                startActivity(new Intent(MainActivity.this, Posts.class));
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.profilebottom) {
                    // Ir a la pantalla CrearPerfil
                    startActivity(new Intent(MainActivity.this, CrearPerfil.class));
                    return true;
                } else if (item.getItemId() == R.id.logout) {
                    logout();
                    return true;
                } else if (item.getItemId() == R.id.search){
                    startActivity(new Intent(MainActivity.this, BuscarPerfilActivity.class));
                    return true;
                }return false;
            }
        });
    }

    private void obtenerPublicaciones() {
        listaPublicaciones.clear();

        db.collection("publicaciones")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Publicacion publicacion = document.toObject(Publicacion.class);

                            if (document.getId() != null && !document.getId().isEmpty()) {
                                publicacion.setDocumentId(document.getId());
                                listaPublicaciones.add(publicacion);
                            } else {
                                Log.e("MainActivity", "Documento sin documentId encontrado");
                            }
                        }
                        publicacionAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "Error al obtener las publicaciones", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onLikeButtonClick(View view) {
        // Obtener la posición del elemento en el RecyclerView
        int position = recyclerViewPublicaciones.getChildLayoutPosition((View) view.getParent().getParent());

        // Verificar si la posición obtenida es válida
        if (position != RecyclerView.NO_POSITION) {
            // Obtener la publicación correspondiente a la posición
            Publicacion publicacion = listaPublicaciones.get(position);
            String publicacionId = publicacion.getDocumentId();

            // Verificar si el usuario ya ha dado like
            DocumentReference likeRef = db.collection("publicaciones").document(publicacionId)
                    .collection("likes").document(currentUserUid);

            likeRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean yaHaDadoLike = task.getResult().exists();
                    // Llamar a actualizarLikeEnFirestore con los argumentos adecuados
                    actualizarLikeEnFirestore(publicacionId, yaHaDadoLike);
                } else {
                    Log.e("MainActivity", "Error al verificar like existente", task.getException());
                    Toast.makeText(MainActivity.this, "Error al verificar like existente", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("MainActivity", "Error: No se pudo obtener la posición del elemento en el RecyclerView");
        }
    }


    public void actualizarLikeEnFirestore(String publicacionId, boolean yaHaDadoLike) {
        DocumentReference likeRef = db.collection("publicaciones").document(publicacionId)
                .collection("likes").document(currentUserUid);

        if (yaHaDadoLike) {
            // Eliminar el like
            likeRef.delete().addOnSuccessListener(aVoid -> {
                decrementarContadorDeLikes(publicacionId);
                Toast.makeText(MainActivity.this, "¡Like eliminado!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Log.e("MainActivity", "Error al eliminar like", e);
                Toast.makeText(MainActivity.this, "Error al eliminar like", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Agregar el like
            Map<String, Object> likeData = new HashMap<>();
            likeData.put("userId", currentUserUid);

            likeRef.set(likeData).addOnSuccessListener(aVoid -> {
                incrementarContadorDeLikes(publicacionId);
                Toast.makeText(MainActivity.this, "¡Like agregado!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Log.e("MainActivity", "Error al agregar like", e);
                Toast.makeText(MainActivity.this, "Error al agregar like", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void decrementarContadorDeLikes(String publicacionId) {
        DocumentReference publicacionRef = db.collection("publicaciones").document(publicacionId);
        publicacionRef.update("likes", FieldValue.increment(-1))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "¡Like eliminado!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error al decrementar contador de likes", e);
                    Toast.makeText(MainActivity.this, "Error al decrementar contador de likes", Toast.LENGTH_SHORT).show();
                });
    }
    private void incrementarContadorDeLikes(String publicacionId) {
        DocumentReference publicacionRef = db.collection("publicaciones").document(publicacionId);
        publicacionRef.update("likes", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "¡Like agregado!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error al incrementar contador de likes", e);
                    Toast.makeText(MainActivity.this, "Error al incrementar contador de likes", Toast.LENGTH_SHORT).show();
                });
    }
    public void onSaveButtonClick(View view) {
        int position = recyclerViewPublicaciones.getChildLayoutPosition((View) view.getParent().getParent());
        if (position != RecyclerView.NO_POSITION) {
            Publicacion publicacion = listaPublicaciones.get(position);
            String publicacionId = publicacion.getDocumentId();
            guardarPublicacionEnFirestore(publicacionId);
        } else {
            Log.e("MainActivity", "Error: No se pudo obtener la posición del elemento en el RecyclerView");
        }
    }

    private void guardarPublicacionEnFirestore(String publicacionId) {
        Map<String, Object> saveData = new HashMap<>();
        saveData.put("publicacionId", publicacionId);

        db.collection("usuarios").document(currentUserUid)
                .collection("savedPublications").document(publicacionId)
                .set(saveData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "¡Publicación guardada!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error al guardar publicación", e);
                    Toast.makeText(MainActivity.this, "Error al guardar publicación", Toast.LENGTH_SHORT).show();
                });
    }




    public void logout() {
        auth.signOut();
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
    }

}
