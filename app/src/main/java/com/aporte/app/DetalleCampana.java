package com.aporte.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.aporte.app.data.api.RetrofitClient;
import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;
import com.aporte.app.model.data.SlotIndumentaria;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executors;

public class DetalleCampana extends AppCompatActivity {

    private TextView tvCategoria, tvEstado, tvTitulo, tvFechaLimite, tvRecaudado, tvMeta, tvDescripcion;
    private ProgressBar pbProgreso;
    private EditText etMontoAporte;
    private Button btnVolver, btnAportar;
    private Campana campanaActual;
    private EditText etConceptoIngresoExterno, etMontoIngresoExterno;
    private Button btnGuardarIngresoExterno;

    private CardView cardCamisetaVirtual;
    private TextView tvEstadoSlots;
    private Button btnVerCamiseta;

    private Button btnCompartirQR;

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
        cardCamisetaVirtual = findViewById(R.id.cardCamisetaVirtual);
        tvEstadoSlots = findViewById(R.id.tvEstadoSlots);
        btnVerCamiseta = findViewById(R.id.btnVerCamiseta);
        btnCompartirQR = findViewById(R.id.btnCompartirQR);
        btnCompartirQR.setOnClickListener(v -> abrirDialogoCompartirQR());

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

        if ("patrocinio".equalsIgnoreCase(c.getTipo())) {
            cardCamisetaVirtual.setVisibility(View.VISIBLE);
            actualizarResumenSlots(c);
            btnVerCamiseta.setOnClickListener(v -> abrirDialogoCamiseta(c));
        } else {
            cardCamisetaVirtual.setVisibility(View.GONE);
        }
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

    private void actualizarResumenSlots(Campana c) {
        if (c.getSlotsIndumentaria() != null) {
            int libres = 0;
            for (SlotIndumentaria s : c.getSlotsIndumentaria()) {
                if ("disponible".equalsIgnoreCase(s.getEstado())) libres++;
            }
            tvEstadoSlots.setText("Espacios publicitarios libres: " + libres + " de " + c.getSlotsIndumentaria().size());
        }
    }

    private void abrirDialogoCamiseta(Campana c) {
        if (c.getSlotsIndumentaria() == null || c.getSlotsIndumentaria().isEmpty()) {
            Toast.makeText(this, "Esta campaña no posee grilla de indumentaria configurada", Toast.LENGTH_SHORT).show();
            return;
        }

        List<SlotIndumentaria> slots = c.getSlotsIndumentaria();
        String[] opciones = new String[slots.size()];

        for (int i = 0; i < slots.size(); i++) {
            SlotIndumentaria s = slots.get(i);
            if ("reservado".equalsIgnoreCase(s.getEstado())) {
                opciones[i] = "[OCUPADO] " + s.getPosicion() + " - (" +
                        (s.getSponsor() != null ? s.getSponsor().getNombreEmpresa() : "Sponsor") + ")";
            } else {
                opciones[i] = s.getPosicion() + " - $" + String.format("%,.0f", s.getPrecio());
            }
        }

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Seleccionar Espacio Publicitario")
                .setItems(opciones, (d, which) -> {
                    SlotIndumentaria slotElegido = slots.get(which);
                    if ("reservado".equalsIgnoreCase(slotElegido.getEstado())) {
                        Toast.makeText(this, "Este espacio ya se encuentra ocupado", Toast.LENGTH_SHORT).show();
                    } else {
                        mostrarFormularioSponsor(slotElegido);
                    }
                })
                .setNegativeButton("Cerrar", null)
                .create();

        dialog.show();

        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.parseColor("#0077FF"));
        }
    }

    private void mostrarFormularioSponsor(SlotIndumentaria slot) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etEmpresa = new EditText(this);
        etEmpresa.setHint("Nombre / Razón Social del Sponsor");
        layout.addView(etEmpresa);

        final EditText etCuit = new EditText(this);
        etCuit.setHint("CUIT (Opcional)");
        layout.addView(etCuit);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Patrocinar: " + slot.getPosicion())
                .setMessage("Monto a abonar: $" + String.format("%,.0f", slot.getPrecio()))
                .setView(layout)
                .setPositiveButton("Confirmar Patrocinio", (d, which) -> {
                    String empresa = etEmpresa.getText().toString().trim();
                    String cuit = etCuit.getText().toString().trim();

                    if (empresa.isEmpty()) {
                        Toast.makeText(this, "Debes ingresar el nombre de la empresa", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ejecutarReservaSlot(slot.getIdSlot(), empresa, cuit);
                })
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        int colorAzul = android.graphics.Color.parseColor("#0077FF");
        int colorGris = android.graphics.Color.parseColor("#64748B");

        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(colorAzul);
        }
        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(colorGris);
        }
    }

    private void ejecutarReservaSlot(String idSlot, String empresa, String cuit) {
        Map<String, Object> body = new HashMap<>();
        body.put("idSlot", idSlot);
        body.put("nombreEmpresa", empresa);
        body.put("cuit", cuit);

        RetrofitClient.getApiService().reservarSlot(campanaActual.getId(), body).enqueue(new Callback<ApiResponse<Campana>>() {
            @Override
            public void onResponse(Call<ApiResponse<Campana>> call, Response<ApiResponse<Campana>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    campanaActual = response.body().getData();
                    mostrarDatosCampana(campanaActual);
                    Toast.makeText(DetalleCampana.this, "¡Espacio publicitario reservado!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DetalleCampana.this, "Error al reservar el espacio", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Campana>> call, Throwable t) {
                Toast.makeText(DetalleCampana.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirDialogoCompartirQR() {
        if (campanaActual == null) return;

        String urlCampana = "https://aporte.app/campana/" + campanaActual.getId();
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + urlCampana;

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);
        layout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        ImageView ivQr = new ImageView(this);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(400, 400);
        ivQr.setLayoutParams(params);
        ivQr.setContentDescription("Codigo QR de la Campana");
        layout.addView(ivQr);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream in = new URL(qrUrl).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                new Handler(Looper.getMainLooper()).post(() -> ivQr.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        TextView tvUrl = new TextView(this);
        tvUrl.setText(urlCampana);
        tvUrl.setTextSize(12);
        tvUrl.setTextColor(android.graphics.Color.parseColor("#0284C7"));
        tvUrl.setPadding(0, 20, 0, 10);
        tvUrl.setGravity(android.view.Gravity.CENTER);
        layout.addView(tvUrl);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Material de Difusion")
                .setMessage("Escanea el codigo QR o comparte el enlace oficial con posibles sponsors y donantes.")
                .setView(layout)
                .setPositiveButton("Compartir Enlace", (d, which) -> ejecutarCompartirNativo(urlCampana))
                .setNegativeButton("Cerrar", null)
                .create();

        dialog.show();

        int colorAzul = android.graphics.Color.parseColor("#0077FF");
        int colorGris = android.graphics.Color.parseColor("#64748B");

        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(colorAzul);
        }
        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(colorGris);
        }
    }

    private void ejecutarCompartirNativo(String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String mensaje = "Apoya la campaña deportiva: " + campanaActual.getTitulo() + "\n\nIngresa al siguiente enlace para colaborar: " + url;
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);
        startActivity(Intent.createChooser(intent, "Compartir campaña via"));
    }
}