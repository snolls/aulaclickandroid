package com.aulaclick.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.Departamento;
import com.aulaclick.app.network.models.Equipamiento;
import com.aulaclick.app.network.models.Recurso;
import com.aulaclick.app.network.models.TipoRecurso;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnadirRecursoActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etCapacidad;
    private AutoCompleteTextView tvTipo, tvDepartamento;
    private TextInputLayout tilTipo, tilDepartamento;
    private ChipGroup chipGroupEquipamiento;
    private SwitchMaterial switchEstado;
    private MaterialButton btnGuardar;

    private Integer idDepartamentoSeleccionado;
    private Integer idTipoRecursoSeleccionado;
    private Map<String, Integer> mapaDepartamentos = new HashMap<>();
    private Map<String, Integer> mapaTiposRecurso = new HashMap<>();
    private Map<Integer, Chip> mapaChipsEquipamiento = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_recurso);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombreRecurso);
        tilTipo = findViewById(R.id.tilTipoRecurso);
        tvTipo = findViewById(R.id.tvTipoRecurso);
        etCapacidad = findViewById(R.id.etCapacidad);
        tilDepartamento = findViewById(R.id.tilDepartamento);
        tvDepartamento = findViewById(R.id.tvDepartamento);
        chipGroupEquipamiento = findViewById(R.id.chipGroupEquipamiento);
        switchEstado = findViewById(R.id.switchEstado);
        btnGuardar = findViewById(R.id.btnGuardarRecurso);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        configurarListeners();
        cargarDatosDinámicos();

        btnGuardar.setOnClickListener(v -> guardarRecurso());
    }

    private void configurarListeners() {
        tvTipo.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item != null) {
                idTipoRecursoSeleccionado = mapaTiposRecurso.get(item.toString());
            }
        });

        tvDepartamento.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item != null) {
                idDepartamentoSeleccionado = mapaDepartamentos.get(item.toString());
            }
        });

        tilTipo.setEndIconOnClickListener(v -> mostrarDialogoAnadir(getString(R.string.catalog_tipo)));
        tilDepartamento.setEndIconOnClickListener(v -> mostrarDialogoAnadir(getString(R.string.catalog_depto)));
    }

    private void cargarDatosDinámicos() {
        cargarDepartamentos();
        cargarTiposRecurso();
        cargarEquipamiento();
    }

    private void cargarDepartamentos() {
        ApiClient.getApiService().getDepartamentos().enqueue(new Callback<List<Departamento>>() {
            @Override
            public void onResponse(Call<List<Departamento>> call, Response<List<Departamento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> nombres = new ArrayList<>();
                    mapaDepartamentos.clear();
                    for (Departamento d : response.body()) {
                        if (d.getNombre() != null) {
                            nombres.add(d.getNombre());
                            mapaDepartamentos.put(d.getNombre(), d.getId());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AnadirRecursoActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombres);
                    tvDepartamento.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Departamento>> call, Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, R.string.error_load_deptos, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarTiposRecurso() {
        ApiClient.getApiService().getTiposRecurso().enqueue(new Callback<List<TipoRecurso>>() {
            @Override
            public void onResponse(Call<List<TipoRecurso>> call, Response<List<TipoRecurso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> nombres = new ArrayList<>();
                    mapaTiposRecurso.clear();
                    for (TipoRecurso t : response.body()) {
                        if (t.getNombre() != null) {
                            nombres.add(t.getNombre());
                            mapaTiposRecurso.put(t.getNombre(), t.getId());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AnadirRecursoActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombres);
                    tvTipo.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<TipoRecurso>> call, Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, R.string.error_load_tipos, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarEquipamiento() {
        ApiClient.getApiService().getEquipamientos().enqueue(new Callback<List<Equipamiento>>() {
            @Override
            public void onResponse(Call<List<Equipamiento>> call, Response<List<Equipamiento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chipGroupEquipamiento.removeAllViews();
                    mapaChipsEquipamiento.clear();
                    for (Equipamiento e : response.body()) {
                        if (e.getNombre() != null) {
                            Chip chip = new Chip(AnadirRecursoActivity.this);
                            chip.setText(e.getNombre());
                            chip.setCheckable(true);
                            chipGroupEquipamiento.addView(chip);
                            mapaChipsEquipamiento.put(e.getId(), chip);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Equipamiento>> call, Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, R.string.error_load_equipamiento, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoAnadir(String tipo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_add_item, tipo));

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText etInput = dialogView.findViewById(R.id.input);

        builder.setPositiveButton(R.string.btn_save, (dialog, which) -> {
            String nombre = etInput.getText().toString().trim();
            if (!nombre.isEmpty()) {
                if (tipo.equals(getString(R.string.catalog_tipo))) {
                    crearTipoRecurso(nombre);
                } else if (tipo.equals(getString(R.string.catalog_depto))) {
                    crearDepartamento(nombre);
                }
            } else {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.show();
    }

    private void crearTipoRecurso(String nombre) {
        ApiClient.getApiService().crearTipoRecurso(new TipoRecurso(nombre)).enqueue(new Callback<TipoRecurso>() {
            @Override
            public void onResponse(Call<TipoRecurso> call, Response<TipoRecurso> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AnadirRecursoActivity.this, R.string.msg_tipo_created, Toast.LENGTH_SHORT).show();
                    cargarTiposRecurso();
                }
            }

            @Override
            public void onFailure(Call<TipoRecurso> call, Throwable t) {}
        });
    }

    private void crearDepartamento(String nombre) {
        ApiClient.getApiService().crearDepartamento(new Departamento(nombre)).enqueue(new Callback<Departamento>() {
            @Override
            public void onResponse(Call<Departamento> call, Response<Departamento> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AnadirRecursoActivity.this, R.string.msg_depto_created, Toast.LENGTH_SHORT).show();
                    cargarDepartamentos();
                }
            }

            @Override
            public void onFailure(Call<Departamento> call, Throwable t) {}
        });
    }

    private void guardarRecurso() {
        String nombre = etNombre.getText().toString().trim();
        String capacidadStr = etCapacidad.getText().toString().trim();
        String estado = switchEstado.isChecked() ? getString(R.string.status_activo) : getString(R.string.status_inactivo);

        if (nombre.isEmpty() || capacidadStr.isEmpty() || idDepartamentoSeleccionado == null || idTipoRecursoSeleccionado == null) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> idsEquipamientoSeleccionados = new ArrayList<>();
        for (Map.Entry<Integer, Chip> entry : mapaChipsEquipamiento.entrySet()) {
            if (entry.getValue().isChecked()) {
                idsEquipamientoSeleccionados.add(entry.getKey());
            }
        }

        Integer capacidad = Integer.parseInt(capacidadStr);
        
        Recurso nuevoRecurso = new Recurso(nombre, idTipoRecursoSeleccionado, capacidad, estado, idDepartamentoSeleccionado, idsEquipamientoSeleccionados);

        btnGuardar.setText(R.string.btn_saving);
        btnGuardar.setEnabled(false);

        ApiClient.getApiService().crearRecurso(nuevoRecurso).enqueue(new Callback<Recurso>() {
            @Override
            public void onResponse(Call<Recurso> call, Response<Recurso> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AnadirRecursoActivity.this, R.string.msg_recurso_saved, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnGuardar.setText(R.string.btn_save_recurso);
                    btnGuardar.setEnabled(true);
                    Toast.makeText(AnadirRecursoActivity.this, getString(R.string.error_server_prefix, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Recurso> call, Throwable t) {
                btnGuardar.setText(R.string.btn_save_recurso);
                btnGuardar.setEnabled(true);
                Toast.makeText(AnadirRecursoActivity.this, getString(R.string.error_network_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
