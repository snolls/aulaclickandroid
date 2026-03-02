package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.aulaclick.app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PerfilActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        sessionManager = new SessionManager(this);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        MaterialButton btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        MaterialButton btnGoAdmin = findViewById(R.id.btnGoAdminPanel);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        btnUpdatePassword.setOnClickListener(v -> {
            String newPass = etNewPassword.getText() != null ? etNewPassword.getText().toString() : "";
            String confirmPass = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

            if (newPass.isEmpty()) {
                Toast.makeText(this, R.string.error_password_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.equals(confirmPass)) {
                Toast.makeText(this, R.string.msg_password_updated, Toast.LENGTH_SHORT).show();
                etNewPassword.setText("");
                etConfirmPassword.setText("");
                findViewById(R.id.etCurrentPassword).requestFocus();
            } else {
                Toast.makeText(this, R.string.error_passwords_dont_match, Toast.LENGTH_SHORT).show();
            }
        });

        btnGoAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPanelActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            // 1. Limpiar los datos de sesión en SharedPreferences
            sessionManager.logout();

            // 2. Mostrar mensaje de despedida
            Toast.makeText(this, R.string.msg_logging_out, Toast.LENGTH_SHORT).show();

            // 3. Volver a la pantalla de Login (MainActivity) limpiando el stack de actividades
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
