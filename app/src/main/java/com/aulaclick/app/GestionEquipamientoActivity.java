package com.aulaclick.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.Equipamiento;
import com.aulaclick.app.network.models.SedeDTO;
import com.aulaclick.app.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionEquipamientoActivity extends AppCompatActivity {

    private EquipamientoAdapter adapter;
    private List<Equipamiento> listaEquipamiento = new ArrayList<>();
    private List<Equipamiento> todosEquipamientos = new ArrayList<>();
    private List<SedeDTO> listaSedesAdmin = new ArrayList<>();
    private Long sedeFiltroCurrent = null;
    private String userRol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_equipamiento);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userRol = new SessionManager(this).getUserRole();

        RecyclerView rvEquipamiento = findViewById(R.id.rvEquipamiento);
        rvEquipamiento.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EquipamientoAdapter(listaEquipamiento, this::showDeleteConfirmation);
        rvEquipamiento.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setVisibility(View.VISIBLE);
        fabAdd.setOnClickListener(v -> showAddDialog());

        if ("ADMIN".equalsIgnoreCase(userRol)) {
            cargarSedes();
        }

        cargarEquipamiento();

        FloatingActionButton fabForce = findViewById(R.id.fabAdd);
        if (fabForce != null) {
            fabForce.show();
            fabForce.setAlpha(1.0f);
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

    private void cargarSedes() {
        ApiClient.getApiService().getSedes().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<SedeDTO>> call, @NonNull Response<List<SedeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSedesAdmin = response.body();
                    configurarSpinnerFiltro();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<SedeDTO>> call, @NonNull Throwable t) {}
        });
    }

    private void configurarSpinnerFiltro() {
        View ll = findViewById(R.id.llFiltroSedeCatalog);
        android.widget.Spinner spinner = findViewById(R.id.spinnerFiltroSedeCatalog);
        if (ll == null || spinner == null) return;
        ll.setVisibility(View.VISIBLE);

        List<SedeDTO> opciones = new ArrayList<>();
        SedeDTO todas = new SedeDTO(); todas.setNombre(getString(R.string.label_todas_las_sedes));
        opciones.add(todas);
        opciones.addAll(listaSedesAdmin);

        ArrayAdapter<SedeDTO> adp = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adp);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                sedeFiltroCurrent = position == 0 ? null : opciones.get(position).getId();
                aplicarFiltroSede();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    private void aplicarFiltroSede() {
        List<Equipamiento> filtrados = new ArrayList<>();
        for (Equipamiento e : todosEquipamientos) {
            if (sedeFiltroCurrent == null || sedeFiltroCurrent.equals(e.getSedeId())) filtrados.add(e);
        }
        listaEquipamiento = filtrados;
        adapter.updateData(filtrados);
    }

    private void cargarEquipamiento() {
        ApiClient.getApiService().getEquipamientos().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Equipamiento>> call, @NonNull Response<List<Equipamiento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todosEquipamientos = response.body();
                    aplicarFiltroSede();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Equipamiento>> call, @NonNull Throwable t) {
                Toast.makeText(GestionEquipamientoActivity.this, R.string.error_load_equipamiento, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.title_add_item, getString(R.string.catalog_equip)));

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        final EditText input = viewInflated.findViewById(R.id.input);
        LinearLayout sedeContainer = viewInflated.findViewById(R.id.sedeContainer);
        Spinner spinnerSede = viewInflated.findViewById(R.id.spinnerSedeDialog);

        if ("ADMIN".equalsIgnoreCase(userRol) && !listaSedesAdmin.isEmpty()) {
            sedeContainer.setVisibility(View.VISIBLE);
            ArrayAdapter<SedeDTO> sedeAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, listaSedesAdmin);
            sedeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSede.setAdapter(sedeAdapter);
        }

        builder.setView(viewInflated);
        builder.setPositiveButton(R.string.btn_save, (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                Long sedeId = null;
                if ("ADMIN".equalsIgnoreCase(userRol) && sedeContainer.getVisibility() == View.VISIBLE) {
                    int pos = spinnerSede.getSelectedItemPosition();
                    if (pos >= 0 && pos < listaSedesAdmin.size()) {
                        sedeId = listaSedesAdmin.get(pos).getId();
                    }
                }
                crearEquipamiento(name, sedeId);
            } else {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void crearEquipamiento(String name, Long sedeId) {
        Equipamiento equip = new Equipamiento(name);
        equip.setSedeId(sedeId);
        ApiClient.getApiService().crearEquipamiento(equip).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Equipamiento> call, @NonNull Response<Equipamiento> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionEquipamientoActivity.this, R.string.msg_equip_created, Toast.LENGTH_SHORT).show();
                    cargarEquipamiento();
                } else {
                    Toast.makeText(GestionEquipamientoActivity.this, "Error " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Equipamiento> call, @NonNull Throwable t) {
                Toast.makeText(GestionEquipamientoActivity.this, R.string.error_network_prefix, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(Equipamiento equip) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> eliminarEquipamiento(equip.getId()))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void eliminarEquipamiento(Integer id) {
        ApiClient.getApiService().eliminarEquipamiento(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    Toast.makeText(GestionEquipamientoActivity.this, R.string.msg_deleted_successfully, Toast.LENGTH_SHORT).show();
                    cargarEquipamiento();
                } else if (response.code() == 409) {
                    Toast.makeText(GestionEquipamientoActivity.this, R.string.error_delete_conflict, Toast.LENGTH_LONG).show();
                } else if (response.code() == 403) {
                    Toast.makeText(GestionEquipamientoActivity.this, "Sin permisos para eliminar este equipamiento", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GestionEquipamientoActivity.this, R.string.error_delete_generic, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(GestionEquipamientoActivity.this, getString(R.string.error_network_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
