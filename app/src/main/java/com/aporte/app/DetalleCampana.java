package com.aporte.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.aporte.app.data.api.RetrofitClient;
import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleCampana extends AppCompatActivity {

    private TextView tvCategoria, tvEstado, tvTitulo, tvFechaLimite, tvRecaudado, tvMeta, tvDescripcion;
    private ProgressBar pbProgreso;
    private EditText etMontoAporte;
    private Button btnVolver, btnAportar;
    private Campana campanaActual;
    private EditText etConceptoIngresoExterno, etMontoIngresoExterno;
    private Button btnGuardarIngresoExterno;

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
        etMontoAporte = findViewById(R.id.etMontoAporte);
        btnAportar = findViewById(R.id.btnAportar);
        btnVolver = findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(v -> finish());

        etConceptoIngresoExterno = findViewById(R.id.etConceptoIngresoExterno);
        etMontoIngresoExterno = findViewById(R.id.etMontoIngresoExterno);
        btnGuardarIngresoExterno = findViewById(R.id.btnGuardarIngresoExterno);

        btnGuardarIngresoExterno.setOnClickListener(v -> realizarIngresoExterno());

        campanaActual = (Campana) getIntent().getSerializableExtra("campana_extra");

        if (campanaActual != null) {
            mostrarDatosCampana(campanaActual);
            btnAportar.setOnClickListener(v -> realizarAporte());
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

    private void realizarAporte() {
        String montoStr = etMontoAporte.getText().toString().trim();

        if (montoStr.isEmpty()) {
            Toast.makeText(this, "Ingresá un monto a aportar", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto = Double.parseDouble(montoStr);
        if (monto <= 0) {
            Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (campanaActual.getId() == null) {
            Toast.makeText(this, "Error: Campaña sin ID válido", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAportar.setEnabled(false);

        Map<String, Object> body = new HashMap<>();
        body.put("monto", monto);

        RetrofitClient.getApiService().registrarAporte(campanaActual.getId(), body).enqueue(new Callback<ApiResponse<Campana>>() {
            @Override
            public void onResponse(Call<ApiResponse<Campana>> call, Response<ApiResponse<Campana>> response) {
                btnAportar.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Campana campanaActualizada = response.body().getData();
                    campanaActual = campanaActualizada;

                    mostrarDatosCampana(campanaActualizada);
                    Toast.makeText(DetalleCampana.this, "¡Gracias por tu aporte de $" + (int)monto + "!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DetalleCampana.this, "Error al procesar el aporte", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Campana>> call, Throwable t) {
                btnAportar.setEnabled(true);
                Toast.makeText(DetalleCampana.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void realizarIngresoExterno() {
        String concepto = etConceptoIngresoExterno.getText().toString().trim();
        String montoStr = etMontoIngresoExterno.getText().toString().trim();

        if (concepto.isEmpty() || montoStr.isEmpty()) {
            Toast.makeText(this, "Completá el concepto y el monto del ingreso", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto = Double.parseDouble(montoStr);
        if (monto <= 0) {
            Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardarIngresoExterno.setEnabled(false);

        Map<String, Object> body = new HashMap<>();
        body.put("concepto", concepto);
        body.put("monto", monto);
        body.put("fecha", new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date()));

        RetrofitClient.getApiService().registrarIngresoExterno(campanaActual.getId(), body).enqueue(new Callback<ApiResponse<Campana>>() {
            @Override
            public void onResponse(Call<ApiResponse<Campana>> call, Response<ApiResponse<Campana>> response) {
                btnGuardarIngresoExterno.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Campana campanaActualizada = response.body().getData();
                    campanaActual = campanaActualizada;

                    mostrarDatosCampana(campanaActualizada);
                    etConceptoIngresoExterno.setText("");
                    etMontoIngresoExterno.setText("");
                    Toast.makeText(DetalleCampana.this, "¡Ingreso externo de $" + (int)monto + " registrado!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DetalleCampana.this, "Error al registrar el ingreso", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Campana>> call, Throwable t) {
                btnGuardarIngresoExterno.setEnabled(true);
                Toast.makeText(DetalleCampana.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}