package com.aulaclick.app;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.SedeDTO;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionSedesActivity extends AppCompatActivity {

    private RecyclerView rvSedes;
    private ProgressBar progressSedes;
    private TextView tvEmptySedes;
    private AlertDialog dialogActivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_sedes);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSedes);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvSedes = findViewById(R.id.rvSedes);
        progressSedes = findViewById(R.id.progressSedes);
        tvEmptySedes = findViewById(R.id.tvEmptySedes);

        rvSedes.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAddSede);
        fab.setOnClickListener(v -> mostrarDialogoSede(null));

        cargarSedes();
    }

    private void cargarSedes() {
        progressSedes.setVisibility(View.VISIBLE);
        rvSedes.setVisibility(View.GONE);
        tvEmptySedes.setVisibility(View.GONE);

        ApiClient.getApiService().getSedes().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<SedeDTO>> call, @NonNull Response<List<SedeDTO>> response) {
                progressSedes.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<SedeDTO> sedes = response.body();
                    if (sedes.isEmpty()) {
                        tvEmptySedes.setVisibility(View.VISIBLE);
                    } else {
                        rvSedes.setAdapter(new SedesAdapter(sedes, GestionSedesActivity.this::mostrarDialogoSede));
                        rvSedes.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmptySedes.setVisibility(View.VISIBLE);
                    Toast.makeText(GestionSedesActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SedeDTO>> call, @NonNull Throwable t) {
                progressSedes.setVisibility(View.GONE);
                tvEmptySedes.setVisibility(View.VISIBLE);
                Toast.makeText(GestionSedesActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoSede(@Nullable SedeDTO sedeAEditar) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sede, null);
        TextInputEditText etNombre = dialogView.findViewById(R.id.etSedeNombre);
        TextInputEditText etCiudad = dialogView.findViewById(R.id.etSedeCiudad);
        TextInputEditText etDireccion = dialogView.findViewById(R.id.etSedeDireccion);

        boolean isNew = (sedeAEditar == null);

        if (!isNew) {
            etNombre.setText(sedeAEditar.getNombre());
            etCiudad.setText(sedeAEditar.getCiudad());
            etDireccion.setText(sedeAEditar.getDireccion());
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(isNew ? "Nueva Sede" : "Editar Sede")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(isNew ? "Guardar" : "Actualizar", null);

        if (!isNew) {
            builder.setNeutralButton("Eliminar", (dialog, which) -> confirmarEliminacion(sedeAEditar));
        }

        dialogActivo = builder.create();
        dialogActivo.show();

        dialogActivo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            String ciudad = etCiudad.getText() != null ? etCiudad.getText().toString().trim() : "";
            String direccion = etDireccion.getText() != null ? etDireccion.getText().toString().trim() : "";

            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            SedeDTO dto = new SedeDTO();
            dto.setNombre(nombre);
            dto.setCiudad(ciudad);
            dto.setDireccion(direccion);

            if (isNew) {
                crearSede(dto);
            } else {
                actualizarSede(sedeAEditar.getId(), dto);
            }
        });
    }

    private void crearSede(SedeDTO dto) {
        ApiClient.getApiService().crearSede(dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SedeDTO> call, @NonNull Response<SedeDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionSedesActivity.this, "Sede creada", Toast.LENGTH_SHORT).show();
                    if (dialogActivo != null) dialogActivo.dismiss();
                    cargarSedes();
                } else {
                    Toast.makeText(GestionSedesActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SedeDTO> call, @NonNull Throwable t) {
                Toast.makeText(GestionSedesActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarSede(Long id, SedeDTO dto) {
        ApiClient.getApiService().actualizarSede(id, dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SedeDTO> call, @NonNull Response<SedeDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionSedesActivity.this, "Sede actualizada", Toast.LENGTH_SHORT).show();
                    if (dialogActivo != null) dialogActivo.dismiss();
                    cargarSedes();
                } else {
                    Toast.makeText(GestionSedesActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SedeDTO> call, @NonNull Throwable t) {
                Toast.makeText(GestionSedesActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarEliminacion(SedeDTO sede) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Sede")
                .setMessage("¿Estás seguro de eliminar la sede " + sede.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarSede(sede.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarSede(Long id) {
        ApiClient.getApiService().eliminarSede(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionSedesActivity.this, "Sede eliminada", Toast.LENGTH_SHORT).show();
                    if (dialogActivo != null) dialogActivo.dismiss();
                    cargarSedes();
                } else {
                    Toast.makeText(GestionSedesActivity.this, "Error al eliminar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(GestionSedesActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
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
