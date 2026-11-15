package com.aporte.app.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageHelper {

    private static final String CLOUD_NAME = "xojhqty1";
    private static final String UPLOAD_PRESET = "aporte";

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final OkHttpClient httpClient = new OkHttpClient();

    private static final LruCache<String, Bitmap> memoriaCache = new LruCache<>(20);

    public interface UploadCallback {
        void onSuccess(String secureUrl);
        void onError(String mensaje);
    }

    public static void cargarImagen(String url, ImageView imageView) {
        if (url == null || url.trim().isEmpty()) {
            imageView.setTag(null);
            imageView.setVisibility(View.GONE);
            return;
        }

        imageView.setVisibility(View.VISIBLE);
        imageView.setTag(url);

        Bitmap enCache = memoriaCache.get(url);
        if (enCache != null) {
            imageView.setImageBitmap(enCache);
            return;
        }

        imageView.setImageDrawable(null);

        executor.execute(() -> {
            try {
                InputStream in = new URL(url).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                if (bitmap != null) {
                    memoriaCache.put(url, bitmap);
                    mainHandler.post(() -> {
                        if (url.equals(imageView.getTag())) {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (url.equals(imageView.getTag())) {
                        imageView.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public static void subirImagenCloudinary(Context context, Uri imageUri, UploadCallback callback) {
        executor.execute(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap original = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) inputStream.close();

                if (original == null) {
                    mainHandler.post(() -> callback.onError("No se pudo leer la imagen seleccionada"));
                    return;
                }

                Bitmap escalado = redimensionarSiEsNecesario(original, 1200);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                escalado.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] bytesImagen = baos.toByteArray();

                RequestBody archivoBody = RequestBody.create(MediaType.parse("image/jpeg"), bytesImagen);
                MultipartBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "portada.jpg", archivoBody)
                        .addFormDataPart("upload_preset", UPLOAD_PRESET)
                        .addFormDataPart("folder", "aporte_campanas")
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload")
                        .post(requestBody)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        mainHandler.post(() -> callback.onError("Error al subir la imagen a Cloudinary"));
                        return;
                    }
                    String jsonStr = response.body().string();
                    JSONObject json = new JSONObject(jsonStr);
                    String secureUrl = json.getString("secure_url");
                    mainHandler.post(() -> callback.onSuccess(secureUrl));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Error de red al subir imagen: " + e.getMessage()));
            }
        });
    }

    private static Bitmap redimensionarSiEsNecesario(Bitmap bitmap, int maxAncho) {
        int ancho = bitmap.getWidth();
        int alto = bitmap.getHeight();
        if (ancho <= maxAncho) return bitmap;

        float ratio = (float) maxAncho / ancho;
        int nuevoAlto = Math.round(alto * ratio);
        return Bitmap.createScaledBitmap(bitmap, maxAncho, nuevoAlto, true);
    }

    public static void subirComprobanteCloudinary(Context context, Uri fileUri, UploadCallback callback) {
        executor.execute(() -> {
            try {
                String mimeType = context.getContentResolver().getType(fileUri);
                if (mimeType == null) mimeType = "application/pdf";

                byte[] bytesArchivo;
                String nombreArchivo;

                if (mimeType.startsWith("image/")) {
                    InputStream is = context.getContentResolver().openInputStream(fileUri);
                    Bitmap original = BitmapFactory.decodeStream(is);
                    if (is != null) is.close();
                    if (original == null) {
                        mainHandler.post(() -> callback.onError("No se pudo leer la imagen seleccionada"));
                        return;
                    }
                    Bitmap escalado = redimensionarSiEsNecesario(original, 1400);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    escalado.compress(Bitmap.CompressFormat.JPEG, 82, baos);
                    bytesArchivo = baos.toByteArray();
                    nombreArchivo = "factura.jpg";
                    mimeType = "image/jpeg";
                } else {
                    InputStream is = context.getContentResolver().openInputStream(fileUri);
                    if (is == null) {
                        mainHandler.post(() -> callback.onError("No se pudo abrir el archivo PDF"));
                        return;
                    }
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[8192];
                    int nRead;
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    is.close();
                    bytesArchivo = buffer.toByteArray();
                    nombreArchivo = "factura.pdf";
                }

                RequestBody archivoBody = RequestBody.create(MediaType.parse(mimeType), bytesArchivo);
                MultipartBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", nombreArchivo, archivoBody)
                        .addFormDataPart("upload_preset", UPLOAD_PRESET)
                        .addFormDataPart("folder", "aporte_comprobantes")
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/auto/upload")
                        .post(requestBody)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        mainHandler.post(() -> callback.onError("Error al subir la factura a Cloudinary"));
                        return;
                    }
                    JSONObject json = new JSONObject(response.body().string());
                    String secureUrl = json.getString("secure_url");
                    mainHandler.post(() -> callback.onSuccess(secureUrl));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Error al procesar comprobante: " + e.getMessage()));
            }
        });
    }
}