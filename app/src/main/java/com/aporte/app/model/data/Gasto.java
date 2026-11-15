package com.aporte.app.model.data;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Gasto implements Serializable {

    @SerializedName("concepto")
    private String concepto;

    @SerializedName("monto")
    private double monto;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("comprobanteUrl")
    private String comprobanteUrl;

    @SerializedName("tipoArchivo")
    private String tipoArchivo;

    public Gasto() {}

    public String getConcepto() { return concepto; }
    public double getMonto() { return monto; }
    public String getFecha() { return fecha; }
    public String getComprobanteUrl() { return comprobanteUrl; }
    public String getTipoArchivo() { return tipoArchivo; }

    public void setConcepto(String concepto) { this.concepto = concepto; }
    public void setMonto(double monto) { this.monto = monto; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setComprobanteUrl(String comprobanteUrl) { this.comprobanteUrl = comprobanteUrl; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }
}