package com.example.fisherman;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fisherman.databinding.ActivityChatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChats;
    private ChatAdapter chatAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Inicializar vistas
        recyclerViewChats = findViewById(R.id.recyclerViewChats);

        // Configurar el RecyclerView y su adaptador
        chatAdapter = new ChatAdapter(new ArrayList<>());
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChats.setAdapter(chatAdapter);

        // Obtener la lista de chats recientes
        obtenerChatsRecientes();
    }

    private void obtenerChatsRecientes() {
        // Obtener la lista de todos los chats del usuario actual
        db.collection("chats")
                .document(auth.getCurrentUser().getUid())
                .collection("recientes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(ChatsActivity.this, "Error al obtener chats recientes", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<Chats> chats = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Chats chat = document.toObject(Chats.class);
                            chats.add(chat);
                        }

                        // Aquí puedes seleccionar el primer chat (o el deseado) y obtener sus mensajes
                        if (!chats.isEmpty()) {
                            Chats chatSeleccionado = chats.get(0);  // Puedes ajustar la lógica para seleccionar el chat que desees
                            //chatSeleccionado.setMensajes(chatSeleccionado.getMensajes());  // Asegúrate de que este método está implementado en tu clase Chats
                            chatAdapter.actualizarChats(Collections.singletonList(chatSeleccionado));
                        }
                    }
                });
    }

}
