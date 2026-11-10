package com.aporte.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aporte.app.data.api.RetrofitClient;
import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;
import com.aporte.app.ui.adapter.CampanaAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private EditText etBuscarCampana;
    private Spinner spFiltroCategoria;

    private List<Campana> listaCompletaCampanas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewCampanas);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabCrearCampana = findViewById(R.id.fabCrearCampana);
        etBuscarCampana = findViewById(R.id.etBuscarCampana);
        spFiltroCategoria = findViewById(R.id.spFiltroCategoria);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CampanaAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        String[] categoriasFiltro = {"Todas las categorías", "Deportista Individual", "Club", "Liga/Torneo"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoriasFiltro);
        spFiltroCategoria.setAdapter(spinnerAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::cargarCampanasDesdeApi);

        fabCrearCampana.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearCampana.class);
            startActivity(intent);
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

        spFiltroCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCampanasDesdeApi();
    }

    private void cargarCampanasDesdeApi() {
        RetrofitClient.getApiService().obtenerCampanas().enqueue(new Callback<ApiResponse<List<Campana>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Campana>>> call, Response<ApiResponse<List<Campana>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaCompletaCampanas = response.body().getData();
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
        String categoriaSeleccionada = spFiltroCategoria.getSelectedItem().toString();

        List<Campana> listaFiltrada = new ArrayList<>();

        for (Campana c : listaCompletaCampanas) {
            boolean coincideTexto = c.getTitulo().toLowerCase().contains(texto) ||
                    (c.getDescripcion() != null && c.getDescripcion().toLowerCase().contains(texto));

            boolean coincideCategoria = categoriaSeleccionada.equals("Todas las categorías") ||
                    (c.getCategoria() != null && c.getCategoria().equalsIgnoreCase(categoriaSeleccionada));

            if (coincideTexto && coincideCategoria) {
                listaFiltrada.add(c);
            }
        }

        adapter.actualizarLista(listaFiltrada);
    }
}