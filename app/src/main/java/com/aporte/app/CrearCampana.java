package com.aporte.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.aporte.app.data.api.RetrofitClient;
import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearCampana extends AppCompatActivity {

    private EditText etTitulo, etDescripcion, etMontoObjetivo, etFechaLimite;
    private Spinner spCategoria;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_campana);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etMontoObjetivo = findViewById(R.id.etMontoObjetivo);
        etFechaLimite = findViewById(R.id.etFechaLimite);
        spCategoria = findViewById(R.id.spCategoria);
        btnGuardar = findViewById(R.id.btnGuardar);

        String[] categorias = {"Deportista Individual", "Club", "Liga/Torneo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categorias);
        spCategoria.setAdapter(adapter);

        btnGuardar.setOnClickListener(v -> guardarCampana());
    }

    private void guardarCampana() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = spCategoria.getSelectedItem().toString();
        String montoStr = etMontoObjetivo.getText().toString().trim();
        String fechaLimite = etFechaLimite.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() || montoStr.isEmpty() || fechaLimite.isEmpty()) {
            Toast.makeText(this, "Por favor completá todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double montoObjetivo = Double.parseDouble(montoStr);

        Campana nuevaCampana = new Campana(titulo, descripcion, categoria, montoObjetivo, fechaLimite);

        btnGuardar.setEnabled(false);

        RetrofitClient.getApiService().crearCampana(nuevaCampana).enqueue(new Callback<ApiResponse<Campana>>() {
            @Override
            public void onResponse(Call<ApiResponse<Campana>> call, Response<ApiResponse<Campana>> response) {
                btnGuardar.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CrearCampana.this, "¡Campaña creada exitosamente!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CrearCampana.this, "Error al crear la campaña", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Campana>> call, Throwable t) {
                btnGuardar.setEnabled(true);
                Toast.makeText(CrearCampana.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}