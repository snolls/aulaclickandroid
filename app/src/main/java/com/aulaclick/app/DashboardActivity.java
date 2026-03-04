package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
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

    private RecyclerView rvRecursos;
    private RecursoAdapter adapter;
    private List<Recurso> listaRecursos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Setup RecyclerView
        rvRecursos = findViewById(R.id.rvRecursos);
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
                Intent intent = new Intent(this, AdminPanelActivity.class);
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
        ApiClient.getApiService().getRecursos().enqueue(new Callback<List<Recurso>>() {
            @Override
            public void onResponse(Call<List<Recurso>> call, Response<List<Recurso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaRecursos.clear();
                    listaRecursos.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(DashboardActivity.this, R.string.error_load_recursos, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Recurso>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, getString(R.string.error_network_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
