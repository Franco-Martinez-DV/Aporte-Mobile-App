package com.aporte.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aporte.app.data.api.RetrofitClient;
import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;
import com.aporte.app.ui.adapter.CampanaAdapter;
import com.aporte.app.utils.AuthDialogHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "APORTE_MOBILE";
    private RecyclerView recyclerView;
    private CampanaAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabCrearCampana;
    private Button btnSesion;

    private EditText etBuscarCampana;
    private ChipGroup chipGroupCategorias;
    private TextView tvSinResultados;

    private String categoriaSeleccionada = "Todas";
    private List<Campana> listaCompletaCampanas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewCampanas);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabCrearCampana = findViewById(R.id.fabCrearCampana);
        btnSesion = findViewById(R.id.btnSesion);
        etBuscarCampana = findViewById(R.id.etBuscarCampana);
        chipGroupCategorias = findViewById(R.id.chipGroupCategorias);
        tvSinResultados = findViewById(R.id.tvSinResultados);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CampanaAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::cargarCampanasDesdeApi);

        btnSesion.setOnClickListener(v -> manejarClickSesion());

        fabCrearCampana.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                AuthDialogHelper.mostrarDialogoAuth(this, () -> {
                    actualizarEstadoSesionUI();
                    startActivity(new Intent(MainActivity.this, CrearCampana.class));
                });
                return;
            }
            startActivity(new Intent(MainActivity.this, CrearCampana.class));
        });

        etBuscarCampana.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupCategorias.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chipSeleccionado = findViewById(checkedIds.get(0));
                if (chipSeleccionado != null) {
                    categoriaSeleccionada = chipSeleccionado.getText().toString();
                    aplicarFiltros();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarEstadoSesionUI();
        cargarCampanasDesdeApi();
    }

    private void actualizarEstadoSesionUI() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario != null && usuario.getEmail() != null) {
            String correoCorto = usuario.getEmail().split("@")[0];
            btnSesion.setText(correoCorto + " (Salir)");
            btnSesion.setBackgroundColor(Color.parseColor("#1E293B"));
        } else {
            btnSesion.setText("Iniciar Sesión");
            btnSesion.setBackgroundColor(Color.parseColor("#0077FF"));
        }
    }

    private void manejarClickSesion() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            AuthDialogHelper.mostrarDialogoAuth(this, this::actualizarEstadoSesionUI);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Sesión Activa")
                    .setMessage("Conectado como: " + usuario.getEmail())
                    .setPositiveButton("Cerrar Sesión", (d, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        actualizarEstadoSesionUI();
                        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    }

    private void cargarCampanasDesdeApi() {
        RetrofitClient.getApiService().obtenerCampanas().enqueue(new Callback<ApiResponse<List<Campana>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Campana>>> call, Response<ApiResponse<List<Campana>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaCompletaCampanas = response.body().getData() != null
                            ? response.body().getData()
                            : new ArrayList<>();
                    aplicarFiltros();
                } else {
                    Log.e(TAG, "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Campana>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Error al conectar con la API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void aplicarFiltros() {
        String texto = etBuscarCampana.getText().toString().trim().toLowerCase();
        List<Campana> listaFiltrada = new ArrayList<>();

        for (Campana c : listaCompletaCampanas) {
            boolean coincideTexto = (c.getTitulo() != null && c.getTitulo().toLowerCase().contains(texto)) ||
                    (c.getDescripcion() != null && c.getDescripcion().toLowerCase().contains(texto));

            boolean coincideCategoria = categoriaSeleccionada.equals("Todas") ||
                    (c.getCategoria() != null && c.getCategoria().equalsIgnoreCase(categoriaSeleccionada));

            if (coincideTexto && coincideCategoria) {
                listaFiltrada.add(c);
            }
        }

        adapter.actualizarLista(listaFiltrada);
        tvSinResultados.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
    }
}