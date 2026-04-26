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
import com.aulaclick.app.network.models.SedeDTO;
import com.aulaclick.app.network.models.TipoRecurso;
import com.aulaclick.app.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionTiposActivity extends AppCompatActivity {

    private TipoRecursoAdapter adapter;
    private List<TipoRecurso> listaTipos = new ArrayList<>();
    private List<TipoRecurso> todosTipos = new ArrayList<>();
    private List<SedeDTO> listaSedesAdmin = new ArrayList<>();
    private Long sedeFiltroCurrent = null;
    private String userRol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_tipos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userRol = new SessionManager(this).getUserRole();

        RecyclerView rvTipos = findViewById(R.id.rvTipos);
        rvTipos.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TipoRecursoAdapter(listaTipos, new TipoRecursoAdapter.OnTipoClickListener() {
            @Override
            public void onEditClick(TipoRecurso tipo) {
                showEditDialog(tipo);
            }
            @Override
            public void onDeleteClick(TipoRecurso tipo) {
                showDeleteConfirmation(tipo);
            }
        });
        rvTipos.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setVisibility(View.VISIBLE);
        fabAdd.setOnClickListener(v -> showAddDialog());

        if ("ADMIN".equalsIgnoreCase(userRol)) {
            cargarSedes();
        }

        cargarTipos();

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
        List<TipoRecurso> filtrados = new ArrayList<>();
        for (TipoRecurso t : todosTipos) {
            if (sedeFiltroCurrent == null || sedeFiltroCurrent.equals(t.getSedeId())) filtrados.add(t);
        }
        listaTipos = filtrados;
        adapter.updateData(filtrados);
    }

    private void cargarTipos() {
        ApiClient.getApiService().getTiposRecurso().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<TipoRecurso>> call, @NonNull Response<List<TipoRecurso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todosTipos = response.body();
                    aplicarFiltroSede();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<TipoRecurso>> call, @NonNull Throwable t) {
                Toast.makeText(GestionTiposActivity.this, R.string.error_load_tipos, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.title_add_item, getString(R.string.catalog_tipo)));

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
                crearTipo(name, sedeId);
            } else {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void crearTipo(String name, Long sedeId) {
        TipoRecurso tipo = new TipoRecurso(name);
        tipo.setSedeId(sedeId);
        ApiClient.getApiService().crearTipoRecurso(tipo).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TipoRecurso> call, @NonNull Response<TipoRecurso> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionTiposActivity.this, R.string.msg_tipo_created, Toast.LENGTH_SHORT).show();
                    cargarTipos();
                } else {
                    Toast.makeText(GestionTiposActivity.this, "Error " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<TipoRecurso> call, @NonNull Throwable t) {
                Toast.makeText(GestionTiposActivity.this, R.string.error_network_prefix, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(TipoRecurso tipo) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Editar Tipo de Recurso");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        final EditText input = viewInflated.findViewById(R.id.input);
        input.setText(tipo.getNombre());
        // Sede no editable en modo edición
        viewInflated.findViewById(R.id.sedeContainer).setVisibility(View.GONE);

        builder.setView(viewInflated);
        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                actualizarTipo(tipo.getId(), name);
            } else {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void actualizarTipo(Integer id, String name) {
        TipoRecurso tipo = new TipoRecurso(name);
        ApiClient.getApiService().actualizarTipoRecurso(id, tipo).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TipoRecurso> call, @NonNull Response<TipoRecurso> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionTiposActivity.this, "Tipo de recurso actualizado", Toast.LENGTH_SHORT).show();
                    cargarTipos();
                } else if (response.code() == 403) {
                    Toast.makeText(GestionTiposActivity.this, "Sin permisos para editar este tipo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GestionTiposActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<TipoRecurso> call, @NonNull Throwable t) {
                Toast.makeText(GestionTiposActivity.this, R.string.error_network_prefix, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(TipoRecurso tipo) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> eliminarTipo(tipo.getId()))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void eliminarTipo(Integer id) {
        ApiClient.getApiService().eliminarTipoRecurso(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    Toast.makeText(GestionTiposActivity.this, R.string.msg_deleted_successfully, Toast.LENGTH_SHORT).show();
                    cargarTipos();
                } else if (response.code() == 409) {
                    Toast.makeText(GestionTiposActivity.this, R.string.error_delete_conflict, Toast.LENGTH_LONG).show();
                } else if (response.code() == 403) {
                    Toast.makeText(GestionTiposActivity.this, "Sin permisos para eliminar este tipo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GestionTiposActivity.this, R.string.error_delete_generic, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(GestionTiposActivity.this, getString(R.string.error_network_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
