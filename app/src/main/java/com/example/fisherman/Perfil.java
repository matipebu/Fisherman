package com.example.fisherman;

public class Perfil {
    private String name;
    private String uid;
    private String bio;
    private String email;
    private String caña;
    private String carrete;
    private String otros;
    private String tpesca;
    private String url;  // URL de la imagen de perfil

    public Perfil() {
        // Constructor vacío requerido para Firebase
    }

    // Getters y Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCaña() {
        return caña;
    }

    public void setCaña(String caña) {
        this.caña = caña;
    }

    public String getCarrete() {
        return carrete;
    }

    public void setCarrete(String carrete) {
        this.carrete = carrete;
    }

    public String getOtros() {
        return otros;
    }

    public void setOtros(String otros) {
        this.otros = otros;
    }

    public String getTpesca() {
        return tpesca;
    }

    public void setTpesca(String tpesca) {
        this.tpesca = tpesca;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
