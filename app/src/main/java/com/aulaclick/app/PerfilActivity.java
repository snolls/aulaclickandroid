package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PerfilActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        MaterialButton btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        MaterialButton btnGoAdmin = findViewById(R.id.btnGoAdminPanel);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        btnUpdatePassword.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();

            if (newPass.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce una nueva contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.equals(confirmPass)) {
                Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                etNewPassword.setText("");
                etConfirmPassword.setText("");
                findViewById(R.id.etCurrentPassword).requestFocus();
            } else {
                Toast.makeText(this, "Las contraseñas nuevas no coinciden", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPanelActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
