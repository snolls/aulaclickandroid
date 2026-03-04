package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;

public class AdminPanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        MaterialCardView cardAddRecurso = findViewById(R.id.cardAddRecurso);
        cardAddRecurso.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnadirRecursoActivity.class);
            startActivity(intent);
        });

        // Intent connections to catalog management activities
        findViewById(R.id.cardDepto).setOnClickListener(v -> {
            Intent intent = new Intent(this, GestionDepartamentosActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardTipo).setOnClickListener(v -> {
            Intent intent = new Intent(this, GestionTiposActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardEquipamiento).setOnClickListener(v -> {
            Intent intent = new Intent(this, GestionEquipamientoActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta pantalla y vuelve a la anterior limpiamente
        return true;
    }
}
