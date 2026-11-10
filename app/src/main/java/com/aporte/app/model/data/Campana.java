package com.aporte.app.model.data;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
public class Campana implements Serializable {
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

    public Campana(String titulo, String descripcion, String categoria, double montoObjetivo, String fechaLimite) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.montoObjetivo = montoObjetivo;
        this.fechaLimite = fechaLimite;
        this.recaudado = 0.0;
        this.estado = "activa";
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public double getMontoObjetivo() { return montoObjetivo; }
    public double getRecaudado() { return recaudado; }
    public String getFechaLimite() { return fechaLimite; }
    public String getEstado() { return estado; }
    public void setId(String id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setMontoObjetivo(double montoObjetivo) { this.montoObjetivo = montoObjetivo; }
    public void setRecaudado(double recaudado) { this.recaudado = recaudado; }
    public void setFechaLimite(String fechaLimite) { this.fechaLimite = fechaLimite; }
    public void setEstado(String estado) { this.estado = estado; }
}