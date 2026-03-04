package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.LoginRequest;
import com.aulaclick.app.network.models.UsuarioResponse;
import com.aulaclick.app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        
        // Si ya está logueado, ir directo al Dashboard
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, R.string.error_fill_fields_login, Toast.LENGTH_SHORT).show();
            } else {
                ejecutarLogin(email, password);
            }
        });
    }

    private void ejecutarLogin(String email, String password) {
        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.btn_connecting);

        LoginRequest loginRequest = new LoginRequest(email, password);
        ApiClient.getApiService().login(loginRequest).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call, @NonNull Response<UsuarioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UsuarioResponse usuario = response.body();
                    
                    // Guardar sesión
                    sessionManager.saveSession(
                            usuario.getIdUsuario(),
                            usuario.getNombreCompleto(),
                            usuario.getRol()
                    );

                    Toast.makeText(MainActivity.this, getString(R.string.msg_welcome, usuario.getNombreCompleto()), Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    restaurarBoton();
                    Toast.makeText(MainActivity.this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                restaurarBoton();
                Toast.makeText(MainActivity.this, getString(R.string.error_network_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restaurarBoton() {
        btnLogin.setEnabled(true);
        btnLogin.setText(R.string.btn_login);
    }
}
