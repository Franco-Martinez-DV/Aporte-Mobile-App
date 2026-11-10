package com.aporte.app.model.data;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class SlotIndumentaria implements Serializable {

    @SerializedName("idSlot")
    private String idSlot;

    @SerializedName("posicion")
    private String posicion;

    @SerializedName("precio")
    private double precio;

    @SerializedName("estado")
    private String estado; // "disponible" | "reservado"

    @SerializedName("sponsor")
    private Sponsor sponsor;

    public static class Sponsor implements Serializable {
        @SerializedName("nombreEmpresa")
        private String nombreEmpresa;

        @SerializedName("cuit")
        private String cuit;

        public String getNombreEmpresa() { return nombreEmpresa; }
        public String getCuit() { return cuit; }
    }

    public String getIdSlot() { return idSlot; }
    public String getPosicion() { return posicion; }
    public double getPrecio() { return precio; }
    public String getEstado() { return estado; }
    public Sponsor getSponsor() { return sponsor; }
}