package com.aporte.app.model.data;
import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private T data;

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
}