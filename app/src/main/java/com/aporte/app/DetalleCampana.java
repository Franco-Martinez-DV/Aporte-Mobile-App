package com.aporte.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.aporte.app.data.api.RetrofitClient;
import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;
import com.aporte.app.model.data.Gasto;
import com.aporte.app.model.data.IngresoExterno;
import com.aporte.app.model.data.SlotIndumentaria;
import com.aporte.app.utils.AuthDialogHelper;
import com.aporte.app.utils.FormatUtils;
import com.aporte.app.utils.ImageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    private Button btnVolver, btnAportar, btnEditarCampana, btnEliminarCampana;
    private Campana campanaActual;
    private EditText etConceptoIngresoExterno, etMontoIngresoExterno;
    private Button btnGuardarIngresoExterno;

    private CardView cardSimularAporte, cardCamisetaVirtual, cardIngresoExterno;
    private TextView tvEstadoSlots;
    private Button btnVerCamiseta;
    private Button btnCompartirQR;

    private TextView tvResumenTransparencia;
    private Button btnVerTransparencia;

    private ImageView ivDetallePortada;
    private WebView webViewFolleto;

    private Button btnRendirGasto;
    private Uri uriFacturaSeleccionada = null;
    private TextView tvNombreArchivoDialogo = null;

    private final ActivityResultLauncher<String[]> selectorFactura =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    uriFacturaSeleccionada = uri;
                    if (tvNombreArchivoDialogo != null) {
                        tvNombreArchivoDialogo.setText("Archivo listo: " + uri.getLastPathSegment());
                        tvNombreArchivoDialogo.setTextColor(Color.parseColor("#16A34A"));
                    }
                }
            });

    private final ActivityResultLauncher<Intent> launcherEditarCampana =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Campana actualizada = (Campana) result.getData().getSerializableExtra("campana_actualizada");
                    if (actualizada != null) {
                        campanaActual = actualizada;
                        mostrarDatosCampana(campanaActual);
                    }
                }
            });

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
        btnEditarCampana = findViewById(R.id.btnEditarCampana);
        btnEliminarCampana = findViewById(R.id.btnEliminarCampana);

        cardSimularAporte = findViewById(R.id.cardSimularAporte);
        cardCamisetaVirtual = findViewById(R.id.cardCamisetaVirtual);
        cardIngresoExterno = findViewById(R.id.cardIngresoExterno);

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
        btnEditarCampana.setOnClickListener(v -> abrirEdicionCampana());
        btnEliminarCampana.setOnClickListener(v -> confirmarEliminacionCampana());
        btnVolver.setOnClickListener(v -> finish());
        btnGuardarIngresoExterno.setOnClickListener(v -> realizarIngresoExterno());

        ivDetallePortada = findViewById(R.id.ivDetallePortada);

        btnRendirGasto = findViewById(R.id.btnRendirGasto);
        btnRendirGasto.setOnClickListener(v -> abrirDialogoRendirGasto());

        campanaActual = (Campana) getIntent().getSerializableExtra("campana_extra");

        if (campanaActual != null) {
            mostrarDatosCampana(campanaActual);
            btnAportar.setOnClickListener(v -> realizarAporte());
        }
    }

    private void abrirEdicionCampana() {
        if (campanaActual == null) return;
        Intent intent = new Intent(DetalleCampana.this, CrearCampana.class);
        intent.putExtra("campana_editar", campanaActual);
        launcherEditarCampana.launch(intent);
    }

    private boolean esCreadorDeCampana(Campana c) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        return usuario != null
                && c != null
                && c.getCreadorId() != null
                && c.getCreadorId().equals(usuario.getUid());
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

        boolean esCreador = esCreadorDeCampana(c);

        if (esCreador) {
            cardSimularAporte.setVisibility(View.GONE);
            cardCamisetaVirtual.setVisibility(View.GONE);
            cardIngresoExterno.setVisibility(View.VISIBLE);
            btnRendirGasto.setVisibility(View.VISIBLE);
            btnEditarCampana.setVisibility(View.VISIBLE);
            btnEliminarCampana.setVisibility(View.VISIBLE);
        } else {
            cardSimularAporte.setVisibility(View.VISIBLE);
            cardIngresoExterno.setVisibility(View.GONE);
            btnRendirGasto.setVisibility(View.GONE);
            btnEditarCampana.setVisibility(View.GONE);
            btnEliminarCampana.setVisibility(View.GONE);

            if ("patrocinio".equalsIgnoreCase(c.getTipo())) {
                cardCamisetaVirtual.setVisibility(View.VISIBLE);
                actualizarResumenSlots(c);
                btnVerCamiseta.setOnClickListener(v -> abrirDialogoCamiseta(c));
            } else {
                cardCamisetaVirtual.setVisibility(View.GONE);
            }
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
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            AuthDialogHelper.mostrarDialogoAuth(this, () -> mostrarDatosCampana(campanaActual));
            return;
        }

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
                    Toast.makeText(DetalleCampana.this, "No se pudo procesar el aporte", Toast.LENGTH_SHORT).show();
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
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            AuthDialogHelper.mostrarDialogoAuth(this, () -> mostrarDatosCampana(campanaActual));
            return;
        }

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
                .setMessage("Escaneá el código QR, compartí el enlace o generá el folleto imprimible en PDF.")
                .setView(layout)
                .setPositiveButton("Compartir Enlace", (d, which) -> ejecutarCompartirNativo(urlCampana))
                .setNeutralButton("Folleto PDF", (d, which) -> imprimirFolletoPDF())
                .setNegativeButton("Cerrar", null)
                .create();

        dialog.show();

        int colorAzul = Color.parseColor("#0077FF");
        int colorGris = Color.parseColor("#64748B");

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(colorAzul);
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEUTRAL) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(colorAzul);
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(colorGris);
        }
    }

    private String escaparHtml(String texto) {
        return texto == null ? "" : TextUtils.htmlEncode(texto);
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
        int cantGastos = c.getGastos() != null ? c.getGastos().size() : 0;
        tvResumenTransparencia.setText(
                "Sponsors: " + cantSponsors + "  •  Ing. externos: " + cantExternos + "  •  Gastos rendidos: " + cantGastos
        );
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
            for (IngresoExterno ing : campanaActual.getIngresosExternos()) {
                totalExternos += ing.getMonto();
                sbExternos.append("• ").append(ing.getConcepto())
                        .append(" (").append(ing.getFecha() != null ? ing.getFecha() : "-").append("): ")
                        .append(FormatUtils.formatearMoneda(ing.getMonto())).append("\n");
            }
        }

        double totalGastos = 0;
        StringBuilder sbGastos = new StringBuilder();
        if (campanaActual.getGastos() != null) {
            for (Gasto g : campanaActual.getGastos()) {
                totalGastos += g.getMonto();
                sbGastos.append("• ").append(g.getConcepto())
                        .append(" (").append(g.getFecha() != null ? g.getFecha() : "-").append("): -")
                        .append(FormatUtils.formatearMoneda(g.getMonto())).append("\n");
            }
        }

        double aportesDirectos = Math.max(0, campanaActual.getRecaudado() - totalSponsors - totalExternos);
        double saldoDisponible = Math.max(0, campanaActual.getRecaudado() - totalGastos);

        StringBuilder detalle = new StringBuilder();
        detalle.append("BALANCE GENERAL\n");
        detalle.append("• Aportes en plataforma: ").append(FormatUtils.formatearMoneda(aportesDirectos)).append("\n");
        detalle.append("• Patrocinios camiseta: ").append(FormatUtils.formatearMoneda(totalSponsors)).append("\n");
        detalle.append("• Ingresos externos: ").append(FormatUtils.formatearMoneda(totalExternos)).append("\n");
        detalle.append("• TOTAL RECAUDADO: ").append(FormatUtils.formatearMoneda(campanaActual.getRecaudado())).append("\n");
        detalle.append("• Gastos rendidos: -").append(FormatUtils.formatearMoneda(totalGastos)).append("\n");
        detalle.append("• SALDO DISPONIBLE: ").append(FormatUtils.formatearMoneda(saldoDisponible)).append("\n\n");

        detalle.append("GASTOS RENDIDOS Y FACTURAS\n");
        detalle.append(sbGastos.length() > 0 ? sbGastos.toString() : "Sin gastos rendidos aún.\n");
        detalle.append("\n");

        detalle.append("SPONSORS EN CAMISETA\n");
        detalle.append(sbSponsors.length() > 0 ? sbSponsors.toString() : "Sin sponsors registrados aún.\n");
        detalle.append("\n");

        detalle.append("INGRESOS EXTERNOS DECLARADOS\n");
        detalle.append(sbExternos.length() > 0 ? sbExternos.toString() : "Sin ingresos externos declarados aún.");

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Transparencia y Movimientos")
                .setMessage(detalle.toString())
                .setPositiveButton("Cerrar", null);

        if (campanaActual.getGastos() != null && !campanaActual.getGastos().isEmpty()) {
            builder.setNeutralButton("Ver Facturas", (d, w) -> abrirSelectorFacturas());
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        int colorAzul = Color.parseColor("#0077FF");
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(colorAzul);
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEUTRAL) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(colorAzul);
        }
    }

    private void abrirSelectorFacturas() {
        List<Gasto> gastos = campanaActual.getGastos();
        if (gastos == null || gastos.isEmpty()) return;

        String[] items = new String[gastos.size()];
        for (int i = 0; i < gastos.size(); i++) {
            Gasto g = gastos.get(i);
            items[i] = g.getConcepto() + " (" + FormatUtils.formatearMoneda(g.getMonto()) + ")";
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Abrir Comprobante / Factura")
                .setItems(items, (d, which) -> {
                    String url = gastos.get(which).getComprobanteUrl();
                    if (url != null && !url.isEmpty()) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        } catch (Exception e) {
                            Toast.makeText(this, "No se encontró una app para abrir el comprobante", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Este gasto no tiene archivo adjunto", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cerrar", null)
                .create();

        dialog.show();

        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#64748B"));
        }
    }

    private void abrirDialogoRendirGasto() {
        if (campanaActual == null) return;

        uriFacturaSeleccionada = null;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(56, 32, 56, 16);

        EditText etConcepto = new EditText(this);
        etConcepto.setHint("Concepto (ej: Compra de pasajes)");
        layout.addView(etConcepto);

        EditText etMonto = new EditText(this);
        etMonto.setHint("Importe ($)");
        etMonto.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etMonto);

        tvNombreArchivoDialogo = new TextView(this);
        tvNombreArchivoDialogo.setText("Ningún comprobante seleccionado");
        tvNombreArchivoDialogo.setTextColor(Color.parseColor("#64748B"));
        tvNombreArchivoDialogo.setPadding(0, 20, 0, 12);
        layout.addView(tvNombreArchivoDialogo);

        Button btnElegirArchivo = new Button(this);
        btnElegirArchivo.setText("Adjuntar Factura (PDF o Imagen)");
        btnElegirArchivo.setBackgroundColor(Color.parseColor("#EFF6FF"));
        btnElegirArchivo.setTextColor(Color.parseColor("#0077FF"));
        btnElegirArchivo.setOnClickListener(v ->
                selectorFactura.launch(new String[]{"application/pdf", "image/*"})
        );
        layout.addView(btnElegirArchivo);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Rendir Gasto")
                .setMessage("Subí la factura comercial que justifica el uso de los fondos.")
                .setView(layout)
                .setPositiveButton("Registrar Gasto", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        Button btnPositivo = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnPositivo.setTextColor(Color.parseColor("#0077FF"));

        btnPositivo.setOnClickListener(v -> {
            String concepto = etConcepto.getText().toString().trim();
            String montoStr = etMonto.getText().toString().trim();

            if (concepto.isEmpty() || montoStr.isEmpty()) {
                Toast.makeText(this, "Completá el concepto y el importe", Toast.LENGTH_SHORT).show();
                return;
            }
            if (uriFacturaSeleccionada == null) {
                Toast.makeText(this, "Adjuntá una factura en PDF o imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            double monto;
            try {
                monto = Double.parseDouble(montoStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Importe inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            btnPositivo.setEnabled(false);
            btnPositivo.setText("Subiendo...");

            String mime = getContentResolver().getType(uriFacturaSeleccionada);
            String tipoArchivo = (mime != null && mime.startsWith("image/")) ? "imagen" : "pdf";

            ImageHelper.subirComprobanteCloudinary(this, uriFacturaSeleccionada, new ImageHelper.UploadCallback() {
                @Override
                public void onSuccess(String secureUrl) {
                    Map<String, Object> body = new HashMap<>();
                    body.put("concepto", concepto);
                    body.put("monto", monto);
                    body.put("comprobanteUrl", secureUrl);
                    body.put("tipoArchivo", tipoArchivo);

                    RetrofitClient.getApiService().registrarGasto(campanaActual.getId(), body).enqueue(new Callback<ApiResponse<Campana>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Campana>> call, Response<ApiResponse<Campana>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                campanaActual = response.body().getData();
                                mostrarDatosCampana(campanaActual);
                                Toast.makeText(DetalleCampana.this, "Gasto rendido correctamente", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                btnPositivo.setEnabled(true);
                                btnPositivo.setText("Registrar Gasto");
                                Toast.makeText(DetalleCampana.this, "No se pudo registrar (verificá el saldo disponible)", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Campana>> call, Throwable t) {
                            btnPositivo.setEnabled(true);
                            btnPositivo.setText("Registrar Gasto");
                            Toast.makeText(DetalleCampana.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String mensaje) {
                    btnPositivo.setEnabled(true);
                    btnPositivo.setText("Registrar Gasto");
                    Toast.makeText(DetalleCampana.this, mensaje, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void imprimirFolletoPDF() {
        if (campanaActual == null) return;

        Toast.makeText(this, "Preparando folleto imprimible...", Toast.LENGTH_SHORT).show();

        String urlCampana = FormatUtils.generarUrlCampana(campanaActual.getId());
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=320x320&data=" + urlCampana;

        String bloqueImagen = "";
        if (campanaActual.getImagenUrl() != null && !campanaActual.getImagenUrl().trim().isEmpty()) {
            bloqueImagen = "<div class='portada-wrapper'>" +
                    "<img src='" + escaparHtml(campanaActual.getImagenUrl()) + "' class='portada' />" +
                    "</div>";
        }

        String fechaLimiteTexto = (campanaActual.getFechaLimite() != null && !campanaActual.getFechaLimite().isEmpty())
                ? escaparHtml(campanaActual.getFechaLimite())
                : "Sin fecha límite";

        String htmlFolleto = "<!DOCTYPE html>" +
                "<html lang='es'><head><meta charset='UTF-8'/>" +
                "<style>" +
                "* { box-sizing: border-box; margin: 0; padding: 0; }" +
                "body { font-family: sans-serif; color: #0F172A; background: #FFFFFF; padding: 12px; }" +
                ".hoja { border: 2px solid #E2E8F0; border-radius: 14px; overflow: hidden; }" +
                ".header { background: #0077FF; color: #FFFFFF; padding: 20px 24px; display: flex; justify-content: space-between; align-items: center; }" +
                ".marca { font-size: 24px; font-weight: 800; }" +
                ".submarca { font-size: 11px; opacity: 0.9; }" +
                ".badge { background: rgba(255,255,255,0.2); padding: 5px 10px; border-radius: 999px; font-size: 11px; font-weight: 700; text-transform: uppercase; }" +
                ".portada-wrapper { width: 100%; height: 210px; overflow: hidden; background: #E2E8F0; }" +
                ".portada { width: 100%; height: 100%; object-fit: cover; }" +
                ".contenido { padding: 24px; }" +
                ".titulo { font-size: 24px; font-weight: 800; margin-bottom: 10px; line-height: 1.2; }" +
                ".descripcion { font-size: 14px; color: #334155; line-height: 1.5; margin-bottom: 20px; }" +
                ".datos-grid { display: flex; gap: 12px; margin-bottom: 22px; }" +
                ".dato-box { flex: 1; background: #F8FAFC; border: 1px solid #CBD5E1; border-radius: 10px; padding: 12px 16px; }" +
                ".dato-label { font-size: 10px; font-weight: 700; text-transform: uppercase; color: #64748B; margin-bottom: 4px; }" +
                ".dato-valor { font-size: 18px; font-weight: 800; color: #0077FF; }" +
                ".qr-seccion { border: 2px dashed #0077FF; background: #EFF6FF; border-radius: 12px; padding: 18px; display: flex; align-items: center; gap: 18px; }" +
                ".qr-img { width: 155px; height: 155px; background: #FFFFFF; padding: 6px; border-radius: 8px; border: 1px solid #BFDBFE; }" +
                ".qr-texto h2 { font-size: 17px; font-weight: 800; margin-bottom: 6px; }" +
                ".qr-texto p { font-size: 12px; color: #334155; line-height: 1.4; margin-bottom: 8px; }" +
                ".url-box { display: inline-block; background: #FFFFFF; border: 1px solid #93C5FD; color: #0077FF; font-family: monospace; font-size: 11px; font-weight: 700; padding: 5px 8px; border-radius: 5px; word-break: break-all; }" +
                ".footer { text-align: center; font-size: 10px; color: #64748B; padding: 12px; border-top: 1px solid #E2E8F0; background: #F8FAFC; }" +
                "</style></head><body>" +
                "<div class='hoja'>" +
                "<div class='header'><div><div class='marca'>APORTE</div><div class='submarca'>Financiamiento Colectivo y Patrocinio Deportivo</div></div>" +
                "<span class='badge'>" + escaparHtml(campanaActual.getCategoria()) + "</span></div>" +
                bloqueImagen +
                "<div class='contenido'>" +
                "<h1 class='titulo'>" + escaparHtml(campanaActual.getTitulo()) + "</h1>" +
                "<p class='descripcion'>" + escaparHtml(campanaActual.getDescripcion()) + "</p>" +
                "<div class='datos-grid'>" +
                "<div class='dato-box'><div class='dato-label'>Meta Económica Propuesta</div><div class='dato-valor'>" + FormatUtils.formatearMoneda(campanaActual.getMontoObjetivo()) + "</div></div>" +
                "<div class='dato-box'><div class='dato-label'>Fecha Límite</div><div class='dato-valor' style='color:#0F172A;'>" + fechaLimiteTexto + "</div></div>" +
                "</div>" +
                "<div class='qr-seccion'>" +
                "<img src='" + qrUrl + "' class='qr-img'/>" +
                "<div class='qr-texto'><h2>¡Sumá tu apoyo a este proyecto!</h2>" +
                "<p>Escaneá este código QR con la cámara de tu celular para donar o patrocinar un espacio en nuestra indumentaria oficial.</p>" +
                "<div class='url-box'>" + escaparHtml(urlCampana) + "</div></div>" +
                "</div></div>" +
                "<div class='footer'>Campaña verificada en plataforma Aporte • ID: " + escaparHtml(campanaActual.getId()) + "</div>" +
                "</div></body></html>";

        WebView webView = new WebView(this);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                    if (printManager != null) {
                        String nombreTrabajo = "Folleto_Aporte_" + escaparHtml(campanaActual.getTitulo());
                        PrintDocumentAdapter adapter = view.createPrintDocumentAdapter(nombreTrabajo);
                        PrintAttributes atributos = new PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .build();
                        printManager.print(nombreTrabajo, adapter, atributos);
                    }
                    webViewFolleto = null;
                }, 400);
            }
        });

        webViewFolleto = webView;
        webView.loadDataWithBaseURL("https://aporte.app", htmlFolleto, "text/html", "UTF-8", null);
    }
}