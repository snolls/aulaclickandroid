package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MisReservasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_reservas);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rvMisReservas = findViewById(R.id.rvMisReservas);
        rvMisReservas.setLayoutManager(new LinearLayoutManager(this));

        List<Reserva> listaPrueba = new ArrayList<>();
        listaPrueba.add(new Reserva("Aula de Informática 3", "15 Oct, 2024", "09:00 - 11:00", "Confirmada"));
        listaPrueba.add(new Reserva("Laboratorio de Química", "18 Oct, 2024", "12:00 - 14:00", "Pendiente"));
        listaPrueba.add(new Reserva("Sala de Conferencias", "20 Oct, 2024", "10:00 - 12:00", "Confirmada"));

        MisReservasAdapter adapter = new MisReservasAdapter(listaPrueba);
        rvMisReservas.setAdapter(adapter);

        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_reservas);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_recursos) {
                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_reservas) {
                    return true;
                } else if (id == R.id.nav_perfil) {
                    Intent intent = new Intent(this, PerfilActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
