package com.aulaclick.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.Recurso;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private RecursoAdapter adapter;
    private final List<Recurso> listaRecursos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Setup RecyclerView
        RecyclerView rvRecursos = findViewById(R.id.rvRecursos);
        rvRecursos.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecursoAdapter(listaRecursos, recurso -> {
            Intent intent = new Intent(this, DetalleRecursoActivity.class);
            intent.putExtra("nombre", recurso.getNombre());
            startActivity(intent);
        });
        rvRecursos.setAdapter(adapter);

        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_recursos);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_recursos) {
                return true;
            } else if (id == R.id.nav_reservas) {
                Intent intent = new Intent(this, MisReservasActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_perfil) {
                Intent intent = new Intent(this, PerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });

        cargarRecursos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarRecursos();
    }

    private void cargarRecursos() {
        ApiClient.getApiService().getRecursos().enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Recurso>> call, @NonNull Response<List<Recurso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaRecursos.clear();
                    listaRecursos.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(DashboardActivity.this, R.string.error_load_recursos, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Recurso>> call, @NonNull Throwable t) {
                Toast.makeText(DashboardActivity.this, getString(R.string.error_network_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
