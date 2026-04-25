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
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private Button btnLogin;
    private SessionManager sessionManager;
    private boolean isSettingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        
        // Si ya está logueado, ir directo al Dashboard (o al onboarding si es la primera vez)
        if (sessionManager.isLoggedIn()) {
            Class<?> destino = sessionManager.isOnboardingDone()
                    ? DashboardActivity.class : OnboardingActivity.class;
            startActivity(new Intent(this, destino));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Limpiar errores en cuanto el usuario empieza a corregir
        android.text.TextWatcher resetErrorWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isSettingError) {
                    tilEmail.setError(null);
                    tilPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };
        etEmail.addTextChangedListener(resetErrorWatcher);
        etPassword.addTextChangedListener(resetErrorWatcher);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            tilEmail.setError(null);
            tilPassword.setError(null);

            if (TextUtils.isEmpty(email)) {
                tilEmail.setError(getString(R.string.error_email_required));
                etEmail.requestFocus();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError(getString(R.string.error_email_invalid_format));
                etEmail.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                tilPassword.setError(getString(R.string.error_password_required));
                etPassword.requestFocus();
                return;
            }
            ejecutarLogin(email, password);
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

                    // LOG: auditar token recibido del backend
                    String tokenRecibido = usuario.getToken();
                    if (tokenRecibido != null && !tokenRecibido.isEmpty()) {
                        android.util.Log.d("JWT_DEBUG", "Login OK. Token recibido (primeros 20): "
                                + tokenRecibido.substring(0, Math.min(tokenRecibido.length(), 20)) + "...");
                    } else {
                        android.util.Log.e("JWT_DEBUG", "¡CRÍTICO! Backend no envió token. usuario.getToken()=null. Verifica @SerializedName en UsuarioResponse o campo en JSON del backend.");
                    }

                    // Guardar sesión y token JWT
                    sessionManager.saveSession(
                            usuario.getIdUsuario(),
                            usuario.getNombreCompleto(),
                            usuario.getRol(),
                            tokenRecibido
                    );
                    ApiClient.setToken(tokenRecibido);

                    Toast.makeText(MainActivity.this, getString(R.string.msg_welcome, usuario.getNombreCompleto()), Toast.LENGTH_SHORT).show();
                    
                    Class<?> destino = sessionManager.isOnboardingDone()
                            ? DashboardActivity.class : OnboardingActivity.class;
                    startActivity(new Intent(MainActivity.this, destino));
                    finish();
                } else {
                    restaurarBoton();
                    isSettingError = true;

                    int code = response.code();

                    // Leer el cuerpo del error para detectar el mensaje del backend
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string().toLowerCase();
                        }
                    } catch (Exception ignored) {}

                    if (code == 404 || errorBody.contains("usuario") && errorBody.contains("encontrado")
                            || errorBody.contains("not found") || errorBody.contains("no existe")) {
                        // El email no existe en la base de datos
                        tilEmail.setError(getString(R.string.error_email_not_found));
                        tilPassword.setError(null);
                        etPassword.setText("");
                        etEmail.requestFocus();

                    } else if (code == 401 || code == 403
                            || errorBody.contains("contraseña") || errorBody.contains("password")
                            || errorBody.contains("credencial")) {
                        // El email existe pero la contraseña es incorrecta
                        tilEmail.setError(null);
                        tilPassword.setError(getString(R.string.error_wrong_password));
                        etPassword.setText("");
                        etPassword.requestFocus();

                    } else {
                        // Error genérico: marcar ambos campos
                        String errorMsg = getString(R.string.error_invalid_credentials);
                        tilEmail.setError(errorMsg);
                        tilPassword.setError(errorMsg);
                        etPassword.setText("");
                        etEmail.requestFocus();
                    }

                    isSettingError = false;
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
