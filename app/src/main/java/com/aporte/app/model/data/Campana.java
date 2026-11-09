package com.aporte.app.model.data;

import com.google.gson.annotations.SerializedName;

public class Campana {
    @SerializedName("id")
    private String id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("montoObjetivo")
    private double montoObjetivo;

    @SerializedName("recaudado")
    private double recaudado;

    @SerializedName("fechaLimite")
    private String fechaLimite;

    @SerializedName("estado")
    private String estado;

    public Campana() {}

    // Getters
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public double getMontoObjetivo() { return montoObjetivo; }
    public double getRecaudado() { return recaudado; }
    public String getFechaLimite() { return fechaLimite; }
    public String getEstado() { return estado; }
}

