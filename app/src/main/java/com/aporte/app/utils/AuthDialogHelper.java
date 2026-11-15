package com.aporte.app.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class AuthDialogHelper {

    public interface OnAuthSuccessListener {
        void onSuccess();
    }

    public static void mostrarDialogoAuth(Context context, OnAuthSuccessListener listener) {
        final boolean[] modoRegistro = {false};

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(56, 32, 56, 16);

        // Botones para alternar entre Iniciar Sesión y Crear Cuenta
        LinearLayout tabsLayout = new LinearLayout(context);
        tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabsLayout.setPadding(0, 0, 0, 24);

        Button btnTabLogin = new Button(context);
        btnTabLogin.setText("Iniciar Sesión");
        LinearLayout.LayoutParams paramsTab = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        paramsTab.setMarginEnd(8);
        btnTabLogin.setLayoutParams(paramsTab);

        Button btnTabRegistro = new Button(context);
        btnTabRegistro.setText("Crear Cuenta");
        LinearLayout.LayoutParams paramsTab2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        paramsTab2.setMarginStart(8);
        btnTabRegistro.setLayoutParams(paramsTab2);

        tabsLayout.addView(btnTabLogin);
        tabsLayout.addView(btnTabRegistro);
        layout.addView(tabsLayout);

        EditText etEmail = new EditText(context);
        etEmail.setHint("Correo electrónico");
        etEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(etEmail);

        EditText etPassword = new EditText(context);
        etPassword.setHint("Contraseña (mínimo 6 caracteres)");
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPassword);

        EditText etConfirmar = new EditText(context);
        etConfirmar.setHint("Confirmar contraseña");
        etConfirmar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConfirmar.setVisibility(View.GONE);
        layout.addView(etConfirmar);

        TextView tvError = new TextView(context);
        tvError.setTextColor(Color.parseColor("#DC2626"));
        tvError.setTextSize(12f);
        tvError.setPadding(0, 12, 0, 0);
        tvError.setVisibility(View.GONE);
        layout.addView(tvError);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Cuenta en Aporte")
                .setView(layout)
                .setPositiveButton("Ingresar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        Runnable actualizarEstiloTabs = () -> {
            int azul = Color.parseColor("#0077FF");
            int grisFondo = Color.parseColor("#E2E8F0");
            int grisTexto = Color.parseColor("#334155");

            if (modoRegistro[0]) {
                btnTabRegistro.setBackgroundColor(azul);
                btnTabRegistro.setTextColor(Color.WHITE);
                btnTabLogin.setBackgroundColor(grisFondo);
                btnTabLogin.setTextColor(grisTexto);
                etConfirmar.setVisibility(View.VISIBLE);
                if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Registrarme");
                }
            } else {
                btnTabLogin.setBackgroundColor(azul);
                btnTabLogin.setTextColor(Color.WHITE);
                btnTabRegistro.setBackgroundColor(grisFondo);
                btnTabRegistro.setTextColor(grisTexto);
                etConfirmar.setVisibility(View.GONE);
                if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Ingresar");
                }
            }
            tvError.setVisibility(View.GONE);
        };

        btnTabLogin.setOnClickListener(v -> {
            modoRegistro[0] = false;
            actualizarEstiloTabs.run();
        });

        btnTabRegistro.setOnClickListener(v -> {
            modoRegistro[0] = true;
            actualizarEstiloTabs.run();
        });

        dialog.show();
        actualizarEstiloTabs.run();

        Button btnPositivo = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnPositivo.setTextColor(Color.parseColor("#0077FF"));

        btnPositivo.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString();
            String confirm = etConfirmar.getText().toString();

            if (email.isEmpty() || pass.isEmpty()) {
                tvError.setText("Completá correo electrónico y contraseña.");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            if (modoRegistro[0] && !pass.equals(confirm)) {
                tvError.setText("Las contraseñas no coinciden.");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            btnPositivo.setEnabled(false);
            btnPositivo.setText("Procesando...");
            tvError.setVisibility(View.GONE);

            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (modoRegistro[0]) {
                auth.createUserWithEmailAndPassword(email, pass)
                        .addOnSuccessListener(result -> {
                            Toast.makeText(context, "¡Cuenta creada! Bienvenido/a", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            if (listener != null) listener.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            btnPositivo.setEnabled(true);
                            btnPositivo.setText("Registrarme");
                            tvError.setText(traducirError(e));
                            tvError.setVisibility(View.VISIBLE);
                        });
            } else {
                auth.signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener(result -> {
                            Toast.makeText(context, "Sesión iniciada correctamente", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            if (listener != null) listener.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            btnPositivo.setEnabled(true);
                            btnPositivo.setText("Ingresar");
                            tvError.setText(traducirError(e));
                            tvError.setVisibility(View.VISIBLE);
                        });
            }
        });
    }

    private static String traducirError(Exception e) {
        if (e instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) e).getErrorCode();
            switch (code) {
                case "ERROR_INVALID_CREDENTIAL":
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_USER_NOT_FOUND":
                    return "Correo o contraseña incorrectos.";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "Ya existe una cuenta registrada con este correo.";
                case "ERROR_WEAK_PASSWORD":
                    return "La contraseña debe tener al menos 6 caracteres.";
                case "ERROR_INVALID_EMAIL":
                    return "Ingresá un correo electrónico válido.";
            }
        }
        return "No se pudo autenticar: " + e.getMessage();
    }
}