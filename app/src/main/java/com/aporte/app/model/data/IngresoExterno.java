package com.aporte.app.model.data;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class IngresoExterno implements Serializable {

    @SerializedName("concepto")
    private String concepto;

    @SerializedName("monto")
    private double monto;

    @SerializedName("fecha")
    private String fecha;

    public IngresoExterno() {}

    public String getConcepto() { return concepto; }
    public double getMonto() { return monto; }
    public String getFecha() { return fecha; }

    public void setConcepto(String concepto) { this.concepto = concepto; }
    public void setMonto(double monto) { this.monto = monto; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}