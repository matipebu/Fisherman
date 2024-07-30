package com.example.fisherman;

import com.google.firebase.firestore.ServerTimestamp;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Publicacion {
    private String documentId;
    private String contenido;
    private String userId;
    private String mediaUrl;
    @ServerTimestamp
    private Date timestamp;

    private int likes;



    // Constructor con todos los campos, incluido documentId
    public Publicacion(String documentId, String contenido, String userId, String mediaUrl, Date timestamp, int likes) {
        this.documentId = documentId;
        this.contenido = contenido;
        this.userId = userId;
        this.mediaUrl = mediaUrl;
        this.likes = likes;
        this.timestamp = timestamp;


    }
    public Publicacion() {
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
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