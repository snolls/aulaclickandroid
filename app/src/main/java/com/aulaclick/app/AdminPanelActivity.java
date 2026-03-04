package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class AdminPanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

        // Setup Bottom Navigation
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
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta pantalla y vuelve a la anterior limpiamente
        return true;
    }
}
