package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.CambioPasswordDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.aulaclick.app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PerfilActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        sessionManager = new SessionManager(this);

        // Cargar los datos del usuario
        String nombre = sessionManager.getUserName();
        String rolRaw = sessionManager.getUserRole();
        String rolDisplay = formatRol(rolRaw);

        TextView tvUserName  = findViewById(R.id.tvUserName);
        Chip chipUserRole    = findViewById(R.id.chipUserRole);

        if (tvUserName  != null) tvUserName.setText(nombre.isEmpty() ? "Usuario" : nombre);
        if (chipUserRole != null) chipUserRole.setText(rolDisplay);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        MaterialButton btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        MaterialButton btnGoAdmin = findViewById(R.id.btnGoAdminPanel);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        boolean esAdmin = rolRaw != null && rolRaw.toUpperCase().contains("ADMIN");
        android.view.View cardAdminSection = findViewById(R.id.cardAdminSection);
        if (cardAdminSection != null)
            cardAdminSection.setVisibility(esAdmin ? android.view.View.VISIBLE : android.view.View.GONE);
        if (btnGoAdmin != null)
            btnGoAdmin.setVisibility(esAdmin ? android.view.View.VISIBLE : android.view.View.GONE);

        TextInputEditText etCurrentPassword = findViewById(R.id.etCurrentPassword);
        TextInputLayout tilCurrentPassword  = findViewById(R.id.tilCurrentPassword);
        TextInputLayout tilNewPassword      = findViewById(R.id.tilNewPassword);
        TextInputLayout tilConfirmPassword  = findViewById(R.id.tilConfirmPassword);

        etCurrentPassword.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { if (tilCurrentPassword != null) tilCurrentPassword.setError(null); }
            public void afterTextChanged(android.text.Editable s) {}
        });
        etNewPassword.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { if (tilNewPassword != null) tilNewPassword.setError(null); }
            public void afterTextChanged(android.text.Editable s) {}
        });
        etConfirmPassword.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { if (tilConfirmPassword != null) tilConfirmPassword.setError(null); }
            public void afterTextChanged(android.text.Editable s) {}
        });

        android.widget.TextView tvInfoPassword = findViewById(R.id.tvInfoPassword);
        if (tvInfoPassword != null) {
            tvInfoPassword.setOnClickListener(v ->
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Requisitos de la contraseña")
                    .setMessage("• Mínimo 12 caracteres.\n• Al menos un número (0-9).\n• Al menos una letra (sin ñ ni acentos).\n• Al menos un símbolo (!#$%&'()*+,-./:;<=>?[]^_`{|}~).\n• No puede contener '@', '\"', ni tu nombre, apellidos o usuario.")
                    .setPositiveButton("Entendido", null)
                    .show()
            );
        }

        btnUpdatePassword.setOnClickListener(v -> {
            String currentPass = etCurrentPassword.getText() != null ? etCurrentPassword.getText().toString().trim() : "";
            String newPass     = etNewPassword.getText()     != null ? etNewPassword.getText().toString().trim()     : "";
            String confirmPass = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

            if (currentPass.isEmpty()) {
                if (tilCurrentPassword != null) tilCurrentPassword.setError("Introduce la contraseña actual");
                etCurrentPassword.requestFocus();
                return;
            }
            if (newPass.isEmpty()) {
                if (tilNewPassword != null) tilNewPassword.setError("Introduce la nueva contraseña");
                etNewPassword.requestFocus();
                return;
            }
            if (!esPasswordValida(newPass, sessionManager.getUserName(), null)) {
                if (tilNewPassword != null) tilNewPassword.setError("La contraseña no cumple con los requisitos de seguridad.");
                etNewPassword.requestFocus();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                if (tilConfirmPassword != null) tilConfirmPassword.setError("Las contraseñas no coinciden");
                etConfirmPassword.requestFocus();
                return;
            }

            long idUsuario = sessionManager.getUserId();
            ApiClient.getApiService()
                    .cambiarPassword(idUsuario, new CambioPasswordDTO(currentPass, newPass))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(PerfilActivity.this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                                etCurrentPassword.setText("");
                                etNewPassword.setText("");
                                etConfirmPassword.setText("");
                                etCurrentPassword.requestFocus();
                            } else if (response.code() == 400 || response.code() == 401) {
                                if (tilCurrentPassword != null) tilCurrentPassword.setError("Contraseña actual incorrecta");
                                etCurrentPassword.requestFocus();
                            } else {
                                Toast.makeText(PerfilActivity.this, "Error al actualizar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            Toast.makeText(PerfilActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnGoAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPanelActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, R.string.msg_logging_out, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Configurar la navegación inferior
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_perfil);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_recursos) {
                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_reservas) {
                    Intent intent = new Intent(this, MisReservasActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_perfil) {
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) bottomNav.getMenu().findItem(R.id.nav_perfil).setChecked(true);
    }

    private boolean esPasswordValida(String password, String nombre, String email) {
        if (password.length() < 12) return false;
        if (!password.matches(".*[0-9].*")) return false;
        if (!password.matches(".*[a-zA-Z].*")) return false;
        if (password.contains("@") || password.contains("\"")) return false;
        if (!password.matches(".*[!#$%&'()*+,-./:;<=>?\\[\\]^_`{|}~].*")) return false;
        String passLower = password.toLowerCase();
        if (email != null && !email.isEmpty()) {
            String localPart = email.split("@")[0].toLowerCase();
            if (passLower.contains(localPart)) return false;
        }
        if (nombre != null && !nombre.isEmpty()) {
            for (String parte : nombre.toLowerCase().split(" ")) {
                if (parte.length() > 2 && passLower.contains(parte)) return false;
            }
        }
        return true;
    }

    private String formatRol(String rol) {
        if (rol == null) return "Usuario";
        String r = rol.trim().toUpperCase();
        if (r.contains("ADMIN"))   return "Administrador";
        if (r.contains("PROFESOR")) return "Profesor";
        if (r.contains("USER") || r.contains("ALUMNO")) return "Alumno";
        return rol.isEmpty() ? "Usuario" : rol;
    }
}
