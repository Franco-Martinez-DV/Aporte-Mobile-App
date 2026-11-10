package com.aporte.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.aporte.app.model.data.Campana;

public class DetalleCampana extends AppCompatActivity {

    private TextView tvCategoria, tvEstado, tvTitulo, tvFechaLimite, tvRecaudado, tvMeta, tvDescripcion;
    private ProgressBar pbProgreso;
    private Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_campana);

        tvCategoria = findViewById(R.id.tvDetalleCategoria);
        tvEstado = findViewById(R.id.tvDetalleEstado);
        tvTitulo = findViewById(R.id.tvDetalleTitulo);
        tvFechaLimite = findViewById(R.id.tvDetalleFechaLimite);
        tvRecaudado = findViewById(R.id.tvDetalleRecaudado);
        tvMeta = findViewById(R.id.tvDetalleMeta);
        tvDescripcion = findViewById(R.id.tvDetalleDescripcion);
        pbProgreso = findViewById(R.id.pbDetalleProgreso);
        btnVolver = findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(v -> finish());

        Campana campana = (Campana) getIntent().getSerializableExtra("campana_extra");

        if (campana != null) {
            mostrarDatosCampana(campana);
        }
    }

    private void mostrarDatosCampana(Campana c) {
        tvTitulo.setText(c.getTitulo());
        tvDescripcion.setText(c.getDescripcion());
        tvCategoria.setText(c.getCategoria() != null ? c.getCategoria().toUpperCase() : "GENERAL");
        tvEstado.setText(c.getEstado() != null ? c.getEstado().toUpperCase() : "ACTIVA");
        tvFechaLimite.setText("Fecha Límite: " + c.getFechaLimite());

        tvRecaudado.setText("$" + String.format("%,.0f", c.getRecaudado()));
        tvMeta.setText("Meta propuesta: $" + String.format("%,.0f", c.getMontoObjetivo()));

        int porcentaje = 0;
        if (c.getMontoObjetivo() > 0) {
            porcentaje = (int) ((c.getRecaudado() / c.getMontoObjetivo()) * 100);
            if (porcentaje > 100) porcentaje = 100;
        }
        pbProgreso.setProgress(porcentaje);
    }
}