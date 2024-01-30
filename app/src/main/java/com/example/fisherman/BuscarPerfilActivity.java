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

public class BuscarPerfilActivity extends AppCompatActivity {

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

    private void actualizarLista(List<Perfil> resultados) {
        perfilAdapter.actualizarDatos(resultados);
        perfilAdapter.setOnItemClickListener(new BuscarUsuarioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Perfil perfil) {
                iniciarChatConUsuarioSeleccionado(perfil);
            }
        });
    }

    private void iniciarChatConUsuarioSeleccionado(Perfil usuarioSeleccionado) {
        // Agregar usuarios a la lista de chats recientes
        db.collection("chats")
                .document(auth.getCurrentUser().getUid())
                .collection("recientes")
                .document(usuarioSeleccionado.getUid())
                .set(new Chats(usuarioSeleccionado.getUid(), "Nuevo chat", System.currentTimeMillis()));

        db.collection("chats")
                .document(usuarioSeleccionado.getUid())
                .collection("recientes")
                .document(auth.getCurrentUser().getUid())
                .set(new Chats(auth.getCurrentUser().getUid(), "Nuevo chat", System.currentTimeMillis()));

        // Abrir la actividad de chat
        Intent intent = new Intent(BuscarPerfilActivity.this, Chat.class);
        intent.putExtra("receptorId", usuarioSeleccionado.getUid());
        startActivity(intent);
    }
}