package com.aporte.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewCampanas);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabCrearCampana = findViewById(R.id.fabCrearCampana);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CampanaAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::cargarCampanasDesdeApi);

        fabCrearCampana.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearCampana.class);
            startActivity(intent);
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
                    List<Campana> listaCampanas = response.body().getData();
                    adapter.actualizarLista(listaCampanas);
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
}