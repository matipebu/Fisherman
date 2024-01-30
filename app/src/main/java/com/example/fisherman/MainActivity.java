package com.example.fisherman;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private RecyclerView recyclerViewPublicaciones;
    private PublicacionAdapter publicacionAdapter;
    private List<Publicacion> listaPublicaciones;
    private FirebaseFirestore db;

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
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Crear la instancia de PublicacionAdapter y establecerla en el RecyclerView
        publicacionAdapter = new PublicacionAdapter(listaPublicaciones, this, db, currentUserUid, auth, new PublicacionAdapter.OnItemClickListener() {
            @Override
            public void onLikeClick(int position) {
                publicacionAdapter.actualizarLikes(position);            }

            @Override
            public void onSaveClick(int position) {
               // publicacionAdapter.guardarPublicacion(position);
            }
        });
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
        ImageView chat = findViewById(R.id.goChat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirigir a la actividad Posts
                startActivity(new Intent(MainActivity.this, ChatsActivity.class));
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
        // Limpiar la lista actual antes de cargar nuevas publicaciones
        listaPublicaciones.clear();

        db.collection("publicaciones")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Publicacion publicacion = document.toObject(Publicacion.class);
                            listaPublicaciones.add(publicacion);
                        }
                        // Notificar al adaptador sobre los cambios en los datos
                        publicacionAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "Error al obtener las publicaciones", Toast.LENGTH_SHORT).show();
                    }
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
