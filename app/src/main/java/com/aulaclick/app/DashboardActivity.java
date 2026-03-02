package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Setup RecyclerView
        RecyclerView rvRecursos = findViewById(R.id.rvRecursos);
        rvRecursos.setLayoutManager(new LinearLayoutManager(this));

        List<Recurso> listaPrueba = new ArrayList<>();
        listaPrueba.add(new Recurso("Aula de Informática 1", "30", "Libre"));
        listaPrueba.add(new Recurso("Laboratorio de Física", "20", "Ocupado"));
        listaPrueba.add(new Recurso("Sala de Juntas", "15", "Mantenimiento"));
        listaPrueba.add(new Recurso("Aula Magna", "100", "Libre"));

        RecursoAdapter adapter = new RecursoAdapter(listaPrueba, recurso -> {
            Intent intent = new Intent(this, DetalleRecursoActivity.class);
            intent.putExtra("nombre", recurso.getNombre());
            startActivity(intent);
        });
        rvRecursos.setAdapter(adapter);

        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_recursos) {
                return true;
            } else if (id == R.id.nav_reservas) {
                startActivity(new Intent(this, MisReservasActivity.class));
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, PerfilActivity.class));
                return true;
            }
            return false;
        });
    }
}
