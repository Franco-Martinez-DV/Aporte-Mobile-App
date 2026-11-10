package com.aporte.app.data.api;

import com.aporte.app.model.data.ApiResponse;
import com.aporte.app.model.data.Campana;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @GET("campanas")
    Call<ApiResponse<List<Campana>>> obtenerCampanas();

    @POST("campanas")
    Call<ApiResponse<Campana>> crearCampana(@Body Campana campana);
}