package com.example.fisherman;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fisherman.Perfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * La clase `BuscarPerfilActivity` permite a los usuarios buscar perfiles por nombre.
 */
// BuscarPerfilActivity.java
public class BuscarPerfilActivity extends AppCompatActivity implements BuscarUsuarioAdapter.OnItemClickListener {

    private EditText buscarEditText;
    private RecyclerView recyclerViewBuscar;
    private BuscarUsuarioAdapter perfilAdapter;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_usuario);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        buscarEditText = findViewById(R.id.edittext_buscarUsuario);
        recyclerViewBuscar = findViewById(R.id.recyclerViewBuscar);

        // Configurar el RecyclerView y su adaptador
        perfilAdapter = new BuscarUsuarioAdapter(new ArrayList<>());
        recyclerViewBuscar.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBuscar.setAdapter(perfilAdapter);

        // Configurar el listener en el adaptador
        perfilAdapter.setOnItemClickListener(this);

        // Añadir un TextWatcher al EditText para realizar la búsqueda mientras escribes
        buscarEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No necesitas implementar nada aquí
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Realizar la búsqueda cada vez que el texto cambie
                buscarPerfil(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No necesitas implementar nada aquí
            }
        });
    }

    /**
     * Método privado para buscar perfiles en la base de datos según el nombre proporcionado.
     *
     * @param nombre El nombre para realizar la búsqueda.
     */
    private void buscarPerfil(String nombre) {
        // Realizar la búsqueda en la base de datos
        Query query = db.collection("usuarios")
                .orderBy("name")
                .startAt(nombre.toUpperCase())
                .endAt(nombre.toLowerCase() + "\uf8ff");

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Perfil> resultados = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Perfil perfilEncontrado = document.toObject(Perfil.class);
                    resultados.add(perfilEncontrado);
                }
                actualizarLista(resultados);
            } else {
                Toast.makeText(BuscarPerfilActivity.this, "Error al buscar perfiles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método privado para actualizar la lista de resultados en el adaptador.
     *
     * @param resultados Lista de perfiles encontrados.
     */
    private void actualizarLista(List<Perfil> resultados) {
        perfilAdapter.actualizarDatos(resultados);
    }

    @Override
    public void onItemClick(Perfil perfil, String otherUserId) {

    }
}

