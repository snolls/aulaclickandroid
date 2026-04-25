package com.aulaclick.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.EstadisticasDTO;

import com.aulaclick.app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPanelActivity extends AppCompatActivity {

    private TextView tvMetricUsuarios;
    private TextView tvMetricRecursos;
    private TextView tvMetricActivas;
    private TextView tvMetricCanceladas;
    private ProgressBar progressMetricas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        android.widget.ImageButton btnBack = findViewById(R.id.btnBackAdmin);
        if (btnBack != null) btnBack.setOnClickListener(v -> volverAPerfil());

        TextView tvAdminGreeting = findViewById(R.id.tvAdminGreeting);
        SessionManager sessionManager = new SessionManager(this);
        String nombre = sessionManager.getUserName();
        if (nombre != null && !nombre.trim().isEmpty()) {
            tvAdminGreeting.setText("Hola, " + nombre);
        } else {
            tvAdminGreeting.setText("Hola, Administrador");
        }

        tvMetricUsuarios = findViewById(R.id.tvMetricUsuarios);
        tvMetricRecursos = findViewById(R.id.tvMetricRecursos);
        tvMetricActivas = findViewById(R.id.tvMetricActivas);
        tvMetricCanceladas = findViewById(R.id.tvMetricCanceladas);
        progressMetricas = findViewById(R.id.progressMetricas);

        String userRol = new SessionManager(this).getUserRole();
        boolean isAdminSede = "ADMIN_SEDE".equalsIgnoreCase(userRol);

        com.google.android.material.card.MaterialCardView cardReservasActivas =
                findViewById(R.id.cardReservasActivas);
        if (cardReservasActivas != null) {
            cardReservasActivas.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminReservasActivity.class);
                intent.putExtra(AdminReservasActivity.EXTRA_TAB, AdminReservasActivity.TAB_ACTIVAS);
                startActivity(intent);
            });
        }

        com.google.android.material.card.MaterialCardView cardReservasCanceladas =
                findViewById(R.id.cardReservasCanceladas);
        if (cardReservasCanceladas != null) {
            cardReservasCanceladas.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminReservasActivity.class);
                intent.putExtra(AdminReservasActivity.EXTRA_TAB, AdminReservasActivity.TAB_CANCELADAS);
                startActivity(intent);
            });
        }

        MaterialCardView cardAddRecurso = findViewById(R.id.cardAddRecurso);
        cardAddRecurso.setVisibility(View.VISIBLE);
        cardAddRecurso.setOnClickListener(v -> startActivity(new Intent(this, AnadirRecursoActivity.class)));

        MaterialCardView cardGestionUsuarios = findViewById(R.id.cardGestionUsuarios);
        if (cardGestionUsuarios != null) {
            cardGestionUsuarios.setVisibility(View.VISIBLE);
            cardGestionUsuarios.setOnClickListener(v ->
                    startActivity(new Intent(this, GestionUsuariosActivity.class)));
        }

        MaterialCardView cardDepto = findViewById(R.id.cardDepto);
        cardDepto.setVisibility(View.VISIBLE);
        cardDepto.setOnClickListener(v ->
                startActivity(new Intent(this, GestionDepartamentosActivity.class)));

        MaterialCardView cardTipo = findViewById(R.id.cardTipo);
        cardTipo.setVisibility(View.VISIBLE);
        cardTipo.setOnClickListener(v ->
                startActivity(new Intent(this, GestionTiposActivity.class)));

        MaterialCardView cardEquipamiento = findViewById(R.id.cardEquipamiento);
        cardEquipamiento.setVisibility(View.VISIBLE);
        cardEquipamiento.setOnClickListener(v ->
                startActivity(new Intent(this, GestionEquipamientoActivity.class)));

        MaterialCardView cardGaleria = findViewById(R.id.cardGaleria);
        cardGaleria.setVisibility(View.VISIBLE);
        cardGaleria.setOnClickListener(v ->
                startActivity(new Intent(this, GestionGaleriaActivity.class)));

        MaterialCardView cardGestionSedes = findViewById(R.id.cardGestionSedes);
        if (cardGestionSedes != null) {
            if (isAdminSede) {
                cardGestionSedes.setVisibility(View.GONE);
            } else {
                cardGestionSedes.setVisibility(View.VISIBLE);
                cardGestionSedes.setOnClickListener(v ->
                        startActivity(new Intent(this, GestionSedesActivity.class)));
            }
        }

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
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) bottomNav.getMenu().findItem(R.id.nav_perfil).setChecked(true);
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        progressMetricas.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getEstadisticas().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<EstadisticasDTO> call, @NonNull Response<EstadisticasDTO> response) {
                progressMetricas.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    EstadisticasDTO e = response.body();
                    tvMetricUsuarios.setText(String.valueOf(e.getTotalUsuarios()));
                    tvMetricRecursos.setText(String.valueOf(e.getTotalRecursos()));
                    tvMetricActivas.setText(String.valueOf(e.getTotalReservasActivas()));
                    tvMetricCanceladas.setText(String.valueOf(e.getTotalReservasCanceladas()));
                } else {
                    mostrarErrorMetricas();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EstadisticasDTO> call, @NonNull Throwable t) {
                progressMetricas.setVisibility(View.GONE);
                mostrarErrorMetricas();
            }
        });
    }

    private void mostrarErrorMetricas() {
        tvMetricUsuarios.setText("—");
        tvMetricRecursos.setText("—");
        tvMetricActivas.setText("—");
        tvMetricCanceladas.setText("—");
        Toast.makeText(this, getString(R.string.error_load_stats), Toast.LENGTH_SHORT).show();
    }

    private void volverAPerfil() {
        Intent intent = new Intent(this, PerfilActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        volverAPerfil();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            volverAPerfil();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
