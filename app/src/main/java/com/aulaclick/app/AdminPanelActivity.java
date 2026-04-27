package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.EstadisticasDTO;
import com.aulaclick.app.network.models.SedeDTO;
import com.aulaclick.app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPanelActivity extends AppCompatActivity {

    private TextView tvMetricUsuarios;
    private TextView tvMetricRecursos;
    private TextView tvMetricActivas;
    private TextView tvMetricCanceladas;
    private ProgressBar progressMetricas;

    private Long sedeSeleccionadaId = null;
    private final List<SedeDTO> sedesDisponibles = new ArrayList<>();
    private Spinner spinnerSedePanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        android.widget.ImageButton btnBack = findViewById(R.id.btnBackAdmin);
        if (btnBack != null) btnBack.setOnClickListener(v -> volverAPerfil());

        TextView tvAdminGreeting = findViewById(R.id.tvAdminGreeting);
        SessionManager sessionManager = new SessionManager(this);
        String nombre = sessionManager.getUserName();
        tvAdminGreeting.setText((nombre != null && !nombre.trim().isEmpty())
                ? "Hola, " + nombre : "Hola, Administrador");

        tvMetricUsuarios  = findViewById(R.id.tvMetricUsuarios);
        tvMetricRecursos  = findViewById(R.id.tvMetricRecursos);
        tvMetricActivas   = findViewById(R.id.tvMetricActivas);
        tvMetricCanceladas = findViewById(R.id.tvMetricCanceladas);
        progressMetricas  = findViewById(R.id.progressMetricas);
        spinnerSedePanel  = findViewById(R.id.spinnerSedePanel);

        String userRol    = sessionManager.getUserRole();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                volverAPerfil();
            }
        });
        boolean isAdmin   = "ADMIN".equalsIgnoreCase(userRol);
        boolean isAdminSede = "ADMIN_SEDE".equalsIgnoreCase(userRol);

        // Mostrar filtro de sede solo para ADMIN global
        if (isAdmin) {
            View llFiltro = findViewById(R.id.llFiltroSedePanel);
            if (llFiltro != null) llFiltro.setVisibility(View.VISIBLE);
            cargarSedes();
        }

        // Tarjetas de reservas — con sedeSeleccionadaId
        MaterialCardView cardReservasActivas = findViewById(R.id.cardReservasActivas);
        if (cardReservasActivas != null) {
            cardReservasActivas.setOnClickListener(v -> abrirReservas(AdminReservasActivity.TAB_ACTIVAS));
        }

        MaterialCardView cardReservasCanceladas = findViewById(R.id.cardReservasCanceladas);
        if (cardReservasCanceladas != null) {
            cardReservasCanceladas.setOnClickListener(v -> abrirReservas(AdminReservasActivity.TAB_CANCELADAS));
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
        cardDepto.setOnClickListener(v -> startActivity(new Intent(this, GestionDepartamentosActivity.class)));

        MaterialCardView cardTipo = findViewById(R.id.cardTipo);
        cardTipo.setVisibility(View.VISIBLE);
        cardTipo.setOnClickListener(v -> startActivity(new Intent(this, GestionTiposActivity.class)));

        MaterialCardView cardEquipamiento = findViewById(R.id.cardEquipamiento);
        cardEquipamiento.setVisibility(View.VISIBLE);
        cardEquipamiento.setOnClickListener(v -> startActivity(new Intent(this, GestionEquipamientoActivity.class)));

        MaterialCardView cardGaleria = findViewById(R.id.cardGaleria);
        cardGaleria.setVisibility(View.VISIBLE);
        cardGaleria.setOnClickListener(v -> startActivity(new Intent(this, GestionGaleriaActivity.class)));

        MaterialCardView cardGestionSedes = findViewById(R.id.cardGestionSedes);
        if (cardGestionSedes != null) {
            if (isAdminSede) {
                cardGestionSedes.setVisibility(View.GONE);
            } else {
                cardGestionSedes.setVisibility(View.VISIBLE);
                cardGestionSedes.setOnClickListener(v -> startActivity(new Intent(this, GestionSedesActivity.class)));
            }
        }

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

    private void cargarSedes() {
        ApiClient.getApiService().getSedes().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<SedeDTO>> call,
                                   @NonNull Response<List<SedeDTO>> response) {
                if (response.isSuccessful() && response.body() != null && spinnerSedePanel != null) {
                    sedesDisponibles.clear();

                    SedeDTO todas = new SedeDTO();
                    todas.setNombre("Todas las sedes");
                    sedesDisponibles.add(todas);
                    sedesDisponibles.addAll(response.body());

                    ArrayAdapter<SedeDTO> adapter = new ArrayAdapter<>(
                            AdminPanelActivity.this,
                            android.R.layout.simple_spinner_item, sedesDisponibles);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSedePanel.setAdapter(adapter);

                    spinnerSedePanel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            sedeSeleccionadaId = position == 0 ? null : sedesDisponibles.get(position).getId();
                            cargarEstadisticas();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<SedeDTO>> call, @NonNull Throwable t) {}
        });
    }

    private void abrirReservas(int tab) {
        Intent intent = new Intent(this, AdminReservasActivity.class);
        intent.putExtra(AdminReservasActivity.EXTRA_TAB, tab);
        if (sedeSeleccionadaId != null) {
            intent.putExtra(AdminReservasActivity.EXTRA_SEDE_ID, sedeSeleccionadaId);
        }
        startActivity(intent);
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

        ApiClient.getApiService().getEstadisticas(sedeSeleccionadaId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<EstadisticasDTO> call,
                                   @NonNull Response<EstadisticasDTO> response) {
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { volverAPerfil(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
