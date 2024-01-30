package com.example.fisherman;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;

public class Chat extends AppCompatActivity {

    private EditText mensajeEditText;
    private Button enviarButton;
    private RecyclerView recyclerViewMensajes;
    private MensajeAdapter mensajeAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String receptorId;  // ID del usuario con el que estás chateando

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Obtener el ID del usuario receptor de la actividad anterior o de algún lugar
        receptorId = getIntent().getStringExtra("receptorId");

        // Inicializar vistas
        mensajeEditText = findViewById(R.id.editTextMensaje);
        enviarButton = findViewById(R.id.buttonEnviar);
        recyclerViewMensajes = findViewById(R.id.recyclerViewMensajes);

        // Configurar el RecyclerView y su adaptador
        mensajeAdapter = new MensajeAdapter(new ArrayList<>());
        recyclerViewMensajes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMensajes.setAdapter(mensajeAdapter);

        // Lógica para enviar mensajes
        enviarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensaje();
            }
        });

        // Escuchar mensajes existentes
        escucharMensajes();
    }


    private void escucharMensajes() {
        db.collection("chats")
                .document(auth.getCurrentUser().getUid())
                .collection(receptorId)  // Accede a la colección específica del usuario receptor
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(Chat.this, "Error al escuchar mensajes", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mensajeAdapter.limpiarMensajes();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Mensaje mensaje = document.toObject(Mensaje.class);
                            mensajeAdapter.agregarMensaje(mensaje);
                        }

                        // Desplazarse al último mensaje
                        recyclerViewMensajes.scrollToPosition(mensajeAdapter.getItemCount() - 1);
                    }
                });
    }


    // Nueva función para enviar mensajes
    private void enviarMensaje() {
        String contenidoMensaje = mensajeEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(contenidoMensaje)) {
            // Crear un nuevo objeto Mensaje
            Mensaje mensaje = new Mensaje();
            mensaje.setContenido(contenidoMensaje);
            mensaje.setRemitenteId(auth.getCurrentUser().getUid());
            mensaje.setTimestamp(System.currentTimeMillis());

            // Guardar el mensaje en Firestore
            db.collection("chats")
                    .document(auth.getCurrentUser().getUid())
                    .collection(receptorId)
                    .add(mensaje)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            // Limpiar el campo de texto después de enviar el mensaje
                            mensajeEditText.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Chat.this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Actualizar el último mensaje en la lista de chats recientes
            actualizarUltimoMensajeEnChatsRecientes(contenidoMensaje);
        }
    }

    // Nueva función para actualizar el último mensaje en la lista de chats recientes
    private void actualizarUltimoMensajeEnChatsRecientes(String contenidoMensaje) {
        // Obtener la referencia al documento del chat reciente
        DocumentReference chatRecienteRef = db.collection("chats")
                .document(auth.getCurrentUser().getUid())
                .collection("recientes")
                .document(receptorId);

        // Actualizar el último mensaje y la marca de tiempo
        chatRecienteRef.update("lastMessage", contenidoMensaje);
        chatRecienteRef.update("timestamp", System.currentTimeMillis());
    }
}
