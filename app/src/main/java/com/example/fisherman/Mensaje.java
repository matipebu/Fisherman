package com.example.fisherman;

public class Mensaje {
    private String contenido;
    private String remitenteId;  // ID del remitente
    private long timestamp;  // Marca de tiempo del mensaje

    // Constructor vacío requerido para Firestore
    public Mensaje() {
    }

    // Constructor con parámetros
    public Mensaje(String contenido, String remitenteId, long timestamp) {
        this.contenido = contenido;
        this.remitenteId = remitenteId;
        this.timestamp = timestamp;
    }

    // Getters y setters

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(String remitenteId) {
        this.remitenteId = remitenteId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


}
