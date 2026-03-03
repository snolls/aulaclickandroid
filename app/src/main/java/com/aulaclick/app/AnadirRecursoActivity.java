package com.aulaclick.app;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.Departamento;
import com.aulaclick.app.network.models.Equipamiento;
import com.aulaclick.app.network.models.Recurso;
import com.aulaclick.app.network.models.TipoRecurso;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
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
    private LinearLayout containerEquipamiento;
    private SwitchMaterial switchEstado;
    private MaterialButton btnGuardar;
    private ImageButton btnAddTipo, btnAddDepto, btnAddEquip;

    private Integer idDepartamentoSeleccionado;
    private Map<String, Integer> mapaDepartamentos = new HashMap<>();
    private Map<String, Integer> mapaTiposRecurso = new HashMap<>();
    private List<CheckBox> listaCheckBoxesEquip = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_recurso);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombreRecurso);
        tvTipo = findViewById(R.id.tvTipoRecurso);
        etCapacidad = findViewById(R.id.etCapacidad);
        tvDepartamento = findViewById(R.id.tvDepartamento);
        containerEquipamiento = findViewById(R.id.containerEquipamiento);
        switchEstado = findViewById(R.id.switchEstado);
        btnGuardar = findViewById(R.id.btnGuardarRecurso);
        
        btnAddTipo = findViewById(R.id.btnAddTipoRecurso);
        btnAddDepto = findViewById(R.id.btnAddDepartamento);
        btnAddEquip = findViewById(R.id.btnAddEquipamiento);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        configurarListeners();
        cargarDatosDinámicos();

        btnGuardar.setOnClickListener(v -> guardarRecurso());
    }

    private void configurarListeners() {
        tvTipo.setOnItemClickListener((parent, view, position, id) -> {
            // No necesitamos el ID del tipo, solo el nombre para el modelo Recurso.tipo
        });

        tvDepartamento.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item != null) {
                String seleccion = item.toString();
                idDepartamentoSeleccionado = mapaDepartamentos.get(seleccion);
            }
        });

        btnAddTipo.setOnClickListener(v -> mostrarDialogoCreacionRapida(getString(R.string.catalog_tipo)));
        btnAddDepto.setOnClickListener(v -> mostrarDialogoCreacionRapida(getString(R.string.catalog_depto)));
        btnAddEquip.setOnClickListener(v -> mostrarDialogoCreacionRapida(getString(R.string.catalog_equip)));
    }

    private void mostrarDialogoCreacionRapida(String tipoCatalog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_add_item, tipoCatalog));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.hint_name);
        builder.setView(input);

        builder.setPositiveButton(R.string.btn_save, (dialog, which) -> {
            String nombre = input.getText().toString().trim();
            if (!nombre.isEmpty()) {
                if (tipoCatalog.equals(getString(R.string.catalog_tipo))) {
                    crearNuevoTipo(nombre);
                } else if (tipoCatalog.equals(getString(R.string.catalog_depto))) {
                    crearNuevoDepto(nombre);
                } else {
                    crearNuevoEquipamiento(nombre);
                }
            } else {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void crearNuevoTipo(String nombre) {
        ApiClient.getApiService().crearTipoRecurso(new TipoRecurso(nombre)).enqueue(new Callback<TipoRecurso>() {
            @Override
            public void onResponse(Call<TipoRecurso> call, Response<TipoRecurso> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AnadirRecursoActivity.this, R.string.msg_tipo_created, Toast.LENGTH_SHORT).show();
                    cargarTiposYSeleccionar(nombre);
                }
            }
            @Override
            public void onFailure(Call<TipoRecurso> call, Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void crearNuevoDepto(String nombre) {
        ApiClient.getApiService().crearDepartamento(new Departamento(nombre)).enqueue(new Callback<Departamento>() {
            @Override
            public void onResponse(Call<Departamento> call, Response<Departamento> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AnadirRecursoActivity.this, R.string.msg_depto_created, Toast.LENGTH_SHORT).show();
                    cargarDeptosYSeleccionar(nombre);
                }
            }
            @Override
            public void onFailure(Call<Departamento> call, Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void crearNuevoEquipamiento(String nombre) {
        ApiClient.getApiService().crearEquipamiento(new Equipamiento(nombre)).enqueue(new Callback<Equipamiento>() {
            @Override
            public void onResponse(Call<Equipamiento> call, Response<Equipamiento> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AnadirRecursoActivity.this, R.string.msg_equip_created, Toast.LENGTH_SHORT).show();
                    cargarEquipamientos();
                }
            }
            @Override
            public void onFailure(Call<Equipamiento> call, Throwable t) {
                Toast.makeText(AnadirRecursoActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDatosDinámicos() {
        cargarDeptosYSeleccionar(null);
        cargarTiposYSeleccionar(null);
        cargarEquipamientos();
    }

    private void cargarDeptosYSeleccionar(String nombreASeleccionar) {
        ApiClient.getApiService().getDepartamentos().enqueue(new Callback<List<Departamento>>() {
            @Override
            public void onResponse(Call<List<Departamento>> call, Response<List<Departamento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> nombres = new ArrayList<>();
                    mapaDepartamentos.clear();
                    for (Departamento d : response.body()) {
                        nombres.add(d.getNombre());
                        mapaDepartamentos.put(d.getNombre(), d.getId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AnadirRecursoActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombres);
                    tvDepartamento.setAdapter(adapter);
                    if (nombreASeleccionar != null) {
                        tvDepartamento.setText(nombreASeleccionar, false);
                        idDepartamentoSeleccionado = mapaDepartamentos.get(nombreASeleccionar);
                    }
                }
            }
            @Override public void onFailure(Call<List<Departamento>> call, Throwable t) {}
        });
    }

    private void cargarTiposYSeleccionar(String nombreASeleccionar) {
        ApiClient.getApiService().getTiposRecurso().enqueue(new Callback<List<TipoRecurso>>() {
            @Override
            public void onResponse(Call<List<TipoRecurso>> call, Response<List<TipoRecurso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> nombres = new ArrayList<>();
                    for (TipoRecurso t : response.body()) {
                        nombres.add(t.getNombre());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AnadirRecursoActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombres);
                    tvTipo.setAdapter(adapter);
                    if (nombreASeleccionar != null) {
                        tvTipo.setText(nombreASeleccionar, false);
                    }
                }
            }
            @Override public void onFailure(Call<List<TipoRecurso>> call, Throwable t) {}
        });
    }

    private void cargarEquipamientos() {
        ApiClient.getApiService().getEquipamientos().enqueue(new Callback<List<Equipamiento>>() {
            @Override
            public void onResponse(Call<List<Equipamiento>> call, Response<List<Equipamiento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    containerEquipamiento.removeAllViews();
                    listaCheckBoxesEquip.clear();
                    for (Equipamiento e : response.body()) {
                        CheckBox cb = new CheckBox(AnadirRecursoActivity.this);
                        cb.setText(e.getNombre());
                        cb.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                        containerEquipamiento.addView(cb);
                        listaCheckBoxesEquip.add(cb);
                    }
                }
            }
            @Override public void onFailure(Call<List<Equipamiento>> call, Throwable t) {}
        });
    }

    private void guardarRecurso() {
        String nombre = etNombre.getText().toString().trim();
        String capacidadStr = etCapacidad.getText().toString().trim();
        String tipo = tvTipo.getText().toString().trim();
        String estado = switchEstado.isChecked() ? getString(R.string.status_activo) : getString(R.string.status_inactivo);

        List<String> equipamientoSeleccionado = new ArrayList<>();
        for (CheckBox cb : listaCheckBoxesEquip) {
            if (cb.isChecked()) {
                equipamientoSeleccionado.add(cb.getText().toString());
            }
        }

        if (nombre.isEmpty() || capacidadStr.isEmpty() || tipo.isEmpty() || idDepartamentoSeleccionado == null) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Integer capacidad = Integer.parseInt(capacidadStr);
        Recurso nuevoRecurso = new Recurso(nombre, tipo, capacidad, estado, idDepartamentoSeleccionado, equipamientoSeleccionado);

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
                    Toast.makeText(AnadirRecursoActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Recurso> call, Throwable t) {
                btnGuardar.setText(R.string.btn_save_recurso);
                btnGuardar.setEnabled(true);
                Toast.makeText(AnadirRecursoActivity.this, "Error red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
