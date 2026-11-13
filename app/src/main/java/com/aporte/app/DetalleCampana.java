package com.aporte.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.aporte.app.data.api.RetrofitClient;
import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;
import com.aporte.app.model.data.SlotIndumentaria;
import com.aporte.app.utils.FormatUtils;
import com.aporte.app.utils.ImageHelper;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleCampana extends AppCompatActivity {

    private TextView tvCategoria, tvEstado, tvTitulo, tvFechaLimite, tvRecaudado, tvMeta, tvDescripcion;
    private ProgressBar pbProgreso;
    private EditText etMontoAporte;
    private Button btnVolver, btnAportar, btnEliminarCampana;
    private Campana campanaActual;
    private EditText etConceptoIngresoExterno, etMontoIngresoExterno;
    private Button btnGuardarIngresoExterno;

    private CardView cardCamisetaVirtual;
    private TextView tvEstadoSlots;
    private Button btnVerCamiseta;
    private Button btnCompartirQR;

    private TextView tvResumenTransparencia;
    private Button btnVerTransparencia;

    private ImageView ivDetallePortada;

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
        btnEliminarCampana = findViewById(R.id.btnEliminarCampana);
        cardCamisetaVirtual = findViewById(R.id.cardCamisetaVirtual);
        tvEstadoSlots = findViewById(R.id.tvEstadoSlots);
        btnVerCamiseta = findViewById(R.id.btnVerCamiseta);
        btnCompartirQR = findViewById(R.id.btnCompartirQR);
        tvResumenTransparencia = findViewById(R.id.tvResumenTransparencia);
        btnVerTransparencia = findViewById(R.id.btnVerTransparencia);
        btnVerTransparencia.setOnClickListener(v -> abrirDialogoTransparencia());

        etConceptoIngresoExterno = findViewById(R.id.etConceptoIngresoExterno);
        etMontoIngresoExterno = findViewById(R.id.etMontoIngresoExterno);
        btnGuardarIngresoExterno = findViewById(R.id.btnGuardarIngresoExterno);

        btnCompartirQR.setOnClickListener(v -> abrirDialogoCompartirQR());
        btnEliminarCampana.setOnClickListener(v -> confirmarEliminacionCampana());
        btnVolver.setOnClickListener(v -> finish());
        btnGuardarIngresoExterno.setOnClickListener(v -> realizarIngresoExterno());

        ivDetallePortada = findViewById(R.id.ivDetallePortada);

        campanaActual = (Campana) getIntent().getSerializableExtra("campana_extra");

        if (campanaActual != null) {
            mostrarDatosCampana(campanaActual);
            btnAportar.setOnClickListener(v -> realizarAporte());
        }
    }

    private void mostrarDatosCampana(Campana c) {
        ImageHelper.cargarImagen(c.getImagenUrl(), ivDetallePortada);
        tvTitulo.setText(c.getTitulo());
        tvDescripcion.setText(c.getDescripcion());
        tvCategoria.setText(c.getCategoria() != null ? c.getCategoria().toUpperCase() : "GENERAL");
        tvEstado.setText(c.getEstado() != null ? c.getEstado().toUpperCase() : "ACTIVA");
        tvFechaLimite.setText(FormatUtils.formatearFechaLimite(c.getFechaLimite()));

        tvRecaudado.setText(FormatUtils.formatearMoneda(c.getRecaudado()));
        tvMeta.setText("Meta propuesta: " + FormatUtils.formatearMoneda(c.getMontoObjetivo()));
        pbProgreso.setProgress(FormatUtils.calcularPorcentaje(c.getRecaudado(), c.getMontoObjetivo()));

        if ("patrocinio".equalsIgnoreCase(c.getTipo())) {
            cardCamisetaVirtual.setVisibility(View.VISIBLE);
            actualizarResumenSlots(c);
            btnVerCamiseta.setOnClickListener(v -> abrirDialogoCamiseta(c));
        } else {
            cardCamisetaVirtual.setVisibility(View.GONE);
        }

        actualizarResumenTransparencia(c);
    }

    private void confirmarEliminacionCampana() {
        if (campanaActual == null || campanaActual.getId() == null) return;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Eliminar Campaña")
                .setMessage("¿Estás seguro de que querés eliminar \"" + campanaActual.getTitulo() + "\"? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (d, which) -> ejecutarEliminacionCampana())
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#CC0000"));
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#64748B"));
        }
    }

    private void ejecutarEliminacionCampana() {
        btnEliminarCampana.setEnabled(false);

        RetrofitClient.getApiService().eliminarCampana(campanaActual.getId()).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                btnEliminarCampana.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(DetalleCampana.this, "Campaña eliminada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DetalleCampana.this, "No se pudo eliminar la campaña", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                btnEliminarCampana.setEnabled(true);
                Toast.makeText(DetalleCampana.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void realizarAporte() {
        String montoStr = etMontoAporte.getText().toString().trim();

        if (montoStr.isEmpty()) {
            Toast.makeText(this, "Ingresá un monto a aportar", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresá un monto numérico válido", Toast.LENGTH_SHORT).show();
            return;
        }

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
                    campanaActual = response.body().getData();
                    mostrarDatosCampana(campanaActual);
                    etMontoAporte.setText("");
                    Toast.makeText(DetalleCampana.this, "¡Gracias por tu aporte de " + FormatUtils.formatearMoneda(monto) + "!", Toast.LENGTH_LONG).show();
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

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresá un monto numérico válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (monto <= 0) {
            Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardarIngresoExterno.setEnabled(false);

        Map<String, Object> body = new HashMap<>();
        body.put("concepto", concepto);
        body.put("monto", monto);
        body.put("fecha", FormatUtils.obtenerFechaActualISO());

        RetrofitClient.getApiService().registrarIngresoExterno(campanaActual.getId(), body).enqueue(new Callback<ApiResponse<Campana>>() {
            @Override
            public void onResponse(Call<ApiResponse<Campana>> call, Response<ApiResponse<Campana>> response) {
                btnGuardarIngresoExterno.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    campanaActual = response.body().getData();
                    mostrarDatosCampana(campanaActual);
                    etConceptoIngresoExterno.setText("");
                    etMontoIngresoExterno.setText("");
                    Toast.makeText(DetalleCampana.this, "¡Ingreso externo de " + FormatUtils.formatearMoneda(monto) + " registrado!", Toast.LENGTH_LONG).show();
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
                opciones[i] = s.getPosicion() + " - " + FormatUtils.formatearMoneda(s.getPrecio());
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
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

        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#0077FF"));
        }
    }

    private void mostrarFormularioSponsor(SlotIndumentaria slot) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etEmpresa = new EditText(this);
        etEmpresa.setHint("Nombre / Razón Social del Sponsor");
        layout.addView(etEmpresa);

        final EditText etCuit = new EditText(this);
        etCuit.setHint("CUIT (Opcional)");
        layout.addView(etCuit);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Patrocinar: " + slot.getPosicion())
                .setMessage("Monto a abonar: " + FormatUtils.formatearMoneda(slot.getPrecio()))
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

        int colorAzul = Color.parseColor("#0077FF");
        int colorGris = Color.parseColor("#64748B");

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(colorAzul);
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(colorGris);
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

        String urlCampana = FormatUtils.generarUrlCampana(campanaActual.getId());
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + urlCampana;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView ivQr = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(400, 400);
        ivQr.setLayoutParams(params);
        ivQr.setContentDescription("Código QR de la Campaña");
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
        tvUrl.setTextColor(Color.parseColor("#0077FF"));
        tvUrl.setPadding(0, 20, 0, 10);
        tvUrl.setGravity(Gravity.CENTER);
        layout.addView(tvUrl);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Material de Difusión")
                .setMessage("Escaneá el código QR o compartí el enlace oficial con posibles sponsors y donantes.")
                .setView(layout)
                .setPositiveButton("Compartir Enlace", (d, which) -> ejecutarCompartirNativo(urlCampana))
                .setNegativeButton("Cerrar", null)
                .create();

        dialog.show();

        int colorAzul = Color.parseColor("#0077FF");
        int colorGris = Color.parseColor("#64748B");

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(colorAzul);
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(colorGris);
        }
    }

    private void ejecutarCompartirNativo(String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String mensaje = "Apoyá la campaña deportiva: " + campanaActual.getTitulo() + "\n\nIngresá al siguiente enlace para colaborar: " + url;
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);
        startActivity(Intent.createChooser(intent, "Compartir campaña vía"));
    }

    private void actualizarResumenTransparencia(Campana c) {
        int cantSponsors = 0;
        if (c.getSlotsIndumentaria() != null) {
            for (SlotIndumentaria s : c.getSlotsIndumentaria()) {
                if ("reservado".equalsIgnoreCase(s.getEstado())) cantSponsors++;
            }
        }
        int cantExternos = c.getIngresosExternos() != null ? c.getIngresosExternos().size() : 0;
        tvResumenTransparencia.setText("Sponsors confirmados: " + cantSponsors + "  •  Ingresos externos: " + cantExternos);
    }

    private void abrirDialogoTransparencia() {
        if (campanaActual == null) return;

        double totalSponsors = 0;
        StringBuilder sbSponsors = new StringBuilder();
        if (campanaActual.getSlotsIndumentaria() != null) {
            for (SlotIndumentaria s : campanaActual.getSlotsIndumentaria()) {
                if ("reservado".equalsIgnoreCase(s.getEstado())) {
                    totalSponsors += s.getPrecio();
                    String empresa = s.getSponsor() != null ? s.getSponsor().getNombreEmpresa() : "Sponsor confirmado";
                    sbSponsors.append("• ").append(empresa)
                            .append(" (").append(s.getPosicion()).append("): ")
                            .append(FormatUtils.formatearMoneda(s.getPrecio())).append("\n");
                }
            }
        }

        double totalExternos = 0;
        StringBuilder sbExternos = new StringBuilder();
        if (campanaActual.getIngresosExternos() != null) {
            for (com.aporte.app.model.data.IngresoExterno ing : campanaActual.getIngresosExternos()) {
                totalExternos += ing.getMonto();
                sbExternos.append("• ").append(ing.getConcepto())
                        .append(" (").append(ing.getFecha() != null ? ing.getFecha() : "-").append("): ")
                        .append(FormatUtils.formatearMoneda(ing.getMonto())).append("\n");
            }
        }

        double aportesDirectos = Math.max(0, campanaActual.getRecaudado() - totalSponsors - totalExternos);

        StringBuilder detalle = new StringBuilder();
        detalle.append("RESUMEN DE FONDOS\n");
        detalle.append("• Aportes en plataforma: ").append(FormatUtils.formatearMoneda(aportesDirectos)).append("\n");
        detalle.append("• Patrocinios camiseta: ").append(FormatUtils.formatearMoneda(totalSponsors)).append("\n");
        detalle.append("• Ingresos externos: ").append(FormatUtils.formatearMoneda(totalExternos)).append("\n");
        detalle.append("TOTAL RECAUDADO: ").append(FormatUtils.formatearMoneda(campanaActual.getRecaudado())).append("\n\n");

        detalle.append("SPONSORS EN CAMISETA\n");
        detalle.append(sbSponsors.length() > 0 ? sbSponsors.toString() : "Sin sponsors registrados aún.\n");
        detalle.append("\n");

        detalle.append("INGRESOS EXTERNOS DECLARADOS\n");
        detalle.append(sbExternos.length() > 0 ? sbExternos.toString() : "Sin ingresos externos declarados aún.");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Transparencia y Movimientos")
                .setMessage(detalle.toString())
                .setPositiveButton("Cerrar", null)
                .create();

        dialog.show();

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0077FF"));
        }
    }
}