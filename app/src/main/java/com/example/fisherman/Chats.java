package com.example.fisherman;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Chats {
    private String userId;  // ID del otro usuario en el chat
    private String lastMessage;  // Último mensaje enviado en el chat
    private long timestamp;  // Marca de tiempo del último mensaje

    private List<Mensaje> mensajes;
    public Chats() {
    }

    // Constructor con parámetros
    public Chats(String userId, String lastMessage, long timestamp) {
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public void getMensajes(OnMessagesLoadedListener listener) {
        // Obtener la referencia a la colección de mensajes
        CollectionReference mensajesRef = FirebaseFirestore.getInstance()
                .collection("mensajes")
                .document(userId)  // Asegúrate de ajustar el nombre del documento según tu caso
                .collection("mensajes");

        // Obtener los mensajes y convertirlos a una lista de objetos Mensaje
        mensajesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Mensaje> mensajes = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Mensaje mensaje = document.toObject(Mensaje.class);
                mensajes.add(mensaje);
            }
            listener.onMessagesLoaded(mensajes); // Llamar al callback cuando los mensajes se carguen
        }).addOnFailureListener(e -> {
            // Manejar el error si es necesario
        });
    }

    public void setMensajes(List<Mensaje> mensajes) {

        this.mensajes = mensajes;
    }

}


