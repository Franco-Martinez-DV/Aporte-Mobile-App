package com.aporte.app.model.data;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

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

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("modalidadPatrocinio")
    private String modalidadPatrocinio;

    @SerializedName("slotsIndumentaria")
    private List<SlotIndumentaria> slotsIndumentaria;

    @SerializedName("ingresosExternos")
    private List<IngresoExterno> ingresosExternos;

    @SerializedName("imagenUrl")
    private String imagenUrl;

    @SerializedName("gastos")
    private List<Gasto> gastos;

    @SerializedName("creadorId")
    private String creadorId;

    @SerializedName("creadorEmail")
    private String creadorEmail;

    public Campana() {}

    public Campana(String titulo, String descripcion, String categoria, double montoObjetivo, String fechaLimite) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.montoObjetivo = montoObjetivo;
        this.fechaLimite = fechaLimite;
        this.recaudado = 0.0;
        this.estado = "activa";
        this.tipo = "donacion";
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public double getMontoObjetivo() { return montoObjetivo; }
    public double getRecaudado() { return recaudado; }
    public String getFechaLimite() { return fechaLimite; }
    public String getEstado() { return estado; }
    public String getTipo() { return tipo; }
    public String getModalidadPatrocinio() { return modalidadPatrocinio; }

    public void setId(String id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setMontoObjetivo(double montoObjetivo) { this.montoObjetivo = montoObjetivo; }
    public void setRecaudado(double recaudado) { this.recaudado = recaudado; }
    public void setFechaLimite(String fechaLimite) { this.fechaLimite = fechaLimite; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setModalidadPatrocinio(String modalidadPatrocinio) { this.modalidadPatrocinio = modalidadPatrocinio; }

    public List<IngresoExterno> getIngresosExternos() { return ingresosExternos; }
    public void setIngresosExternos(List<IngresoExterno> ingresosExternos) {
        this.ingresosExternos = ingresosExternos;
    }

    public List<SlotIndumentaria> getSlotsIndumentaria() { return slotsIndumentaria; }
    public void setSlotsIndumentaria(List<SlotIndumentaria> slotsIndumentaria) {
        this.slotsIndumentaria = slotsIndumentaria;
    }

    public List<Gasto> getGastos() { return gastos; }
    public void setGastos(List<Gasto> gastos) { this.gastos = gastos; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getCreadorId() { return creadorId; }
    public void setCreadorId(String creadorId) { this.creadorId = creadorId; }

    public String getCreadorEmail() { return creadorEmail; }
    public void setCreadorEmail(String creadorEmail) { this.creadorEmail = creadorEmail; }
}