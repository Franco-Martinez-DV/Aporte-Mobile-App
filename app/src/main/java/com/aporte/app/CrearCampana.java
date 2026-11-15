package com.aporte.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.aporte.app.data.api.RetrofitClient;
import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;
import com.aporte.app.utils.FormatUtils;
import com.aporte.app.utils.ImageHelper;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearCampana extends AppCompatActivity {

    private TextView tvTituloFormulario;
    private EditText etTitulo, etDescripcion, etMontoObjetivo, etFechaLimite;
    private Spinner spCategoria;
    private Button btnGuardar, btnCancelarCrear, btnSeleccionarImagen, btnQuitarImagen;
    private ImageView ivPreviewPortada;

    private final String[] categorias = {"Deportista Individual", "Club", "Liga/Torneo"};
    private String imagenUrlSubida = "";
    private boolean subiendoImagen = false;

    private boolean modoEdicion = false;
    private Campana campanaEditar = null;

    private final ActivityResultLauncher<String> selectorGaleria =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::procesarImagenSeleccionada);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_campana);

        tvTituloFormulario = findViewById(R.id.tvTituloFormulario);
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etMontoObjetivo = findViewById(R.id.etMontoObjetivo);
        etFechaLimite = findViewById(R.id.etFechaLimite);
        spCategoria = findViewById(R.id.spCategoria);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelarCrear = findViewById(R.id.btnCancelarCrear);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnQuitarImagen = findViewById(R.id.btnQuitarImagen);
        ivPreviewPortada = findViewById(R.id.ivPreviewPortada);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categorias);
        spCategoria.setAdapter(adapter);

        etFechaLimite.setFocusable(false);
        etFechaLimite.setClickable(true);
        etFechaLimite.setOnClickListener(v -> abrirSelectorFecha());

        btnSeleccionarImagen.setOnClickListener(v -> selectorGaleria.launch("image/*"));
        btnQuitarImagen.setOnClickListener(v -> quitarImagen());
        btnCancelarCrear.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarCampana());

        campanaEditar = (Campana) getIntent().getSerializableExtra("campana_editar");
        if (campanaEditar != null) {
            modoEdicion = true;
            cargarDatosEdicion(campanaEditar);
        }
    }

    private void cargarDatosEdicion(Campana c) {
        tvTituloFormulario.setText("Editar Campaña Deportiva");
        btnGuardar.setText("Guardar Cambios");

        etTitulo.setText(c.getTitulo());
        etDescripcion.setText(c.getDescripcion());
        etMontoObjetivo.setText(String.format(Locale.US, "%.0f", c.getMontoObjetivo()));
        etFechaLimite.setText(c.getFechaLimite());

        if (c.getCategoria() != null) {
            for (int i = 0; i < categorias.length; i++) {
                if (categorias[i].equalsIgnoreCase(c.getCategoria())) {
                    spCategoria.setSelection(i);
                    break;
                }
            }
        }

        if (c.getImagenUrl() != null && !c.getImagenUrl().trim().isEmpty()) {
            imagenUrlSubida = c.getImagenUrl();
            ImageHelper.cargarImagen(imagenUrlSubida, ivPreviewPortada);
            ivPreviewPortada.setVisibility(View.VISIBLE);
            btnQuitarImagen.setVisibility(View.VISIBLE);
        }
    }

    private void procesarImagenSeleccionada(Uri uri) {
        if (uri == null) return;

        ivPreviewPortada.setImageURI(uri);
        ivPreviewPortada.setVisibility(View.VISIBLE);
        btnQuitarImagen.setVisibility(View.VISIBLE);

        subiendoImagen = true;
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Subiendo imagen...");
        btnSeleccionarImagen.setEnabled(false);

        ImageHelper.subirImagenCloudinary(this, uri, new ImageHelper.UploadCallback() {
            @Override
            public void onSuccess(String secureUrl) {
                imagenUrlSubida = secureUrl;
                subiendoImagen = false;
                btnGuardar.setEnabled(true);
                btnGuardar.setText(modoEdicion ? "Guardar Cambios" : "Publicar Campaña");
                btnSeleccionarImagen.setEnabled(true);
                Toast.makeText(CrearCampana.this, "Imagen cargada correctamente", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String mensaje) {
                subiendoImagen = false;
                btnGuardar.setEnabled(true);
                btnGuardar.setText(modoEdicion ? "Guardar Cambios" : "Publicar Campaña");
                btnSeleccionarImagen.setEnabled(true);
                quitarImagen();
                Toast.makeText(CrearCampana.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void quitarImagen() {
        imagenUrlSubida = "";
        ivPreviewPortada.setImageDrawable(null);
        ivPreviewPortada.setVisibility(View.GONE);
        btnQuitarImagen.setVisibility(View.GONE);
    }

    private void abrirSelectorFecha() {
        Calendar calendario = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String fechaFormateada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etFechaLimite.setText(fechaFormateada);
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private void guardarCampana() {
        if (subiendoImagen) return;

        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = spCategoria.getSelectedItem().toString();
        String montoStr = etMontoObjetivo.getText().toString().trim();
        String fechaLimite = etFechaLimite.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() || montoStr.isEmpty() || fechaLimite.isEmpty()) {
            Toast.makeText(this, "Por favor completá todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double montoObjetivo;
        try {
            montoObjetivo = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresá un monto objetivo numérico válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (montoObjetivo < 10000) {
            Toast.makeText(this, "El monto objetivo mínimo es de $10.000", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!FormatUtils.esFechaValidaNoPasada(fechaLimite)) {
            Toast.makeText(this, "La fecha límite debe ser hoy o una fecha futura (AAAA-MM-DD)", Toast.LENGTH_SHORT).show();
            return;
        }

        Campana datosCampana = new Campana(titulo, descripcion, categoria, montoObjetivo, fechaLimite);
        datosCampana.setImagenUrl(imagenUrlSubida);

        btnGuardar.setEnabled(false);

        if (modoEdicion && campanaEditar != null) {
            RetrofitClient.getApiService().actualizarCampana(campanaEditar.getId(), datosCampana)
                    .enqueue(new Callback<ApiResponse<Campana>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Campana>> call, Response<ApiResponse<Campana>> response) {
                            btnGuardar.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(CrearCampana.this, "¡Campaña actualizada correctamente!", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("campana_actualizada", response.body().getData());
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                Toast.makeText(CrearCampana.this, "Error al actualizar la campaña", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Campana>> call, Throwable t) {
                            btnGuardar.setEnabled(true);
                            Toast.makeText(CrearCampana.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            RetrofitClient.getApiService().crearCampana(datosCampana)
                    .enqueue(new Callback<ApiResponse<Campana>>() {
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
}