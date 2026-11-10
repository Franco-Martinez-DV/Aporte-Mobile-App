package com.aporte.app.data.api;

import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @GET("campanas")
    Call<ApiResponse<List<Campana>>> obtenerCampanas();

    @POST("campanas")
    Call<ApiResponse<Campana>> crearCampana(@Body Campana campana);

    @POST("campanas/{id}/aportar")
    Call<ApiResponse<Campana>> registrarAporte(@Path("id") String id, @Body Map<String, Object> body);
    @POST("campanas/{id}/ingreso-externo")
    Call<ApiResponse<Campana>> registrarIngresoExterno(@Path("id") String id, @Body Map<String, Object> body);
}