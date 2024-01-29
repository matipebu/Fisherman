package com.example.fisherman;

import com.google.firebase.firestore.ServerTimestamp;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Date;

public class Publicacion {
    private String documentId;
    private String contenido;
    private String userId;
    private String mediaUrl;
    private int likes;
    private int saves;
    @ServerTimestamp
    private Date timestamp;


    // Constructor vacío requerido para Firestore
    public Publicacion() {
    }

    // Constructor con todos los campos, incluido documentId
    public Publicacion(String documentId, String contenido, String userId, String mediaUrl, int likes, int saves, Date timestamp) {

        this.contenido = contenido;
        this.userId = userId;
        this.mediaUrl = mediaUrl;
        this.likes = likes;
        this.saves = saves;
        this.timestamp = timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getSaves() {
        return saves;
    }

    public void setSaves(int saves) {
        this.saves = saves;
    }
    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }


}
