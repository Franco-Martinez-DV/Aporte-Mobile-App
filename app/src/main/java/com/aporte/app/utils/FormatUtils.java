package com.aporte.app.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

    private static final Locale LOCALE_AR = new Locale("es", "AR");
    private static final String BASE_WEB_URL = "https://aporte.app/campana/";

    public static String formatearMoneda(double monto) {
        NumberFormat formato = NumberFormat.getNumberInstance(LOCALE_AR);
        formato.setMaximumFractionDigits(0);
        return "$" + formato.format((long) monto);
    }

    public static String formatearProgresoMontos(double recaudado, double objetivo) {
        return formatearMoneda(recaudado) + " de " + formatearMoneda(objetivo);
    }

    public static int calcularPorcentaje(double recaudado, double objetivo) {
        if (objetivo <= 0) {
            return 0;
        }
        int porcentaje = (int) Math.round((recaudado / objetivo) * 100);
        return Math.min(porcentaje, 100);
    }

    public static String formatearFechaLimite(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return "Sin fecha límite";
        }
        return "Límite: " + fecha.trim();
    }

    public static String obtenerFechaActualISO() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public static String generarUrlCampana(String idCampana) {
        return BASE_WEB_URL + (idCampana != null ? idCampana : "");
    }

    public static boolean esFechaValidaNoPasada(String fechaStr) {
        if (fechaStr == null || !fechaStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date fechaIngresada = sdf.parse(fechaStr);
            Calendar hoy = Calendar.getInstance();
            hoy.set(Calendar.HOUR_OF_DAY, 0);
            hoy.set(Calendar.MINUTE, 0);
            hoy.set(Calendar.SECOND, 0);
            hoy.set(Calendar.MILLISECOND, 0);
            return fechaIngresada != null && !fechaIngresada.before(hoy.getTime());
        } catch (ParseException e) {
            return false;
        }
    }
}