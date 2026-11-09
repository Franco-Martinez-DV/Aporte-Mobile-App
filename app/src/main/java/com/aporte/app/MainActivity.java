package com.aporte.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aporte.app.data.api.RetrofitClient;
import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;
import com.aporte.app.ui.adapter.CampanaAdapter;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "APORTE_MOBILE";
    private RecyclerView recyclerView;
    private CampanaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewCampanas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CampanaAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        cargarCampanasDesdeApi();
    }

    private void cargarCampanasDesdeApi() {
        RetrofitClient.getApiService().obtenerCampanas().enqueue(new Callback<ApiResponse<List<Campana>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Campana>>> call, Response<ApiResponse<List<Campana>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Campana> listaCampanas = response.body().getData();
                    Log.d(TAG, "Campañas obtenidas: " + listaCampanas.size());

                    adapter.actualizarLista(listaCampanas);
                } else {
                    Log.e(TAG, "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Campana>>> call, Throwable t) {
                Log.e(TAG, "Error de red: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error al conectar con la API", Toast.LENGTH_SHORT).show();
            }
        });
    }
}