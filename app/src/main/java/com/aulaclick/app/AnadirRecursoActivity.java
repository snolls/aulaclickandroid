package com.aulaclick.app;

import android.os.Bundle;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnadirRecursoActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etCapacidad;
    private ChipGroup cgTipo, cgDepartamento, cgEquipamiento;
    private SwitchMaterial switchEstado;
    private MaterialButton btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_recurso);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombreRecurso);
        cgTipo = findViewById(R.id.cgTipo);
        etCapacidad = findViewById(R.id.etCapacidad);
        cgDepartamento = findViewById(R.id.cgDepartamento);
        cgEquipamiento = findViewById(R.id.chipGroupEquipamiento);
        switchEstado = findViewById(R.id.switchEstado);
        btnGuardar = findViewById(R.id.btnGuardarRecurso);

        cargarDatosDinámicos();

        btnGuardar.setOnClickListener(v -> guardarRecurso());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta pantalla y vuelve a la anterior limpiamente
        return true;
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
                    cgDepartamento.removeAllViews();
                    for (Departamento d : response.body()) {
                        if (d.getNombre() != null) {
                            Chip chip = createChip(d.getNombre(), d.getId());
                            cgDepartamento.addView(chip);
                        }
                    }
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
                    cgTipo.removeAllViews();
                    for (TipoRecurso t : response.body()) {
                        if (t.getNombre() != null) {
                            Chip chip = createChip(t.getNombre(), t.getId());
                            cgTipo.addView(chip);
                        }
                    }
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
                    cgEquipamiento.removeAllViews();
                    for (Equipamiento e : response.body()) {
                        if (e.getNombre() != null) {
                            Chip chip = createChip(e.getNombre(), e.getId());
                            cgEquipamiento.addView(chip);
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

    private Chip createChip(String text, Integer id) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setTag(id);
        chip.setClickable(true);
        return chip;
    }

    private void guardarRecurso() {
        String nombre = etNombre.getText().toString().trim();
        String capacidadStr = etCapacidad.getText().toString().trim();
        String estado = switchEstado.isChecked() ? getString(R.string.status_activo) : getString(R.string.status_inactivo);

        int deptoChipId = cgDepartamento.getCheckedChipId();
        int tipoChipId = cgTipo.getCheckedChipId();

        if (nombre.isEmpty() || capacidadStr.isEmpty() || deptoChipId == -1 || tipoChipId == -1) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Chip deptoChip = findViewById(deptoChipId);
        Chip tipoChip = findViewById(tipoChipId);
        
        Integer idDepartamento = (Integer) deptoChip.getTag();
        Integer idTipoRecurso = (Integer) tipoChip.getTag();

        List<Integer> idsEquipamientoSeleccionados = new ArrayList<>();
        for (int id : cgEquipamiento.getCheckedChipIds()) {
            Chip chip = findViewById(id);
            idsEquipamientoSeleccionados.add((Integer) chip.getTag());
        }

        Integer capacidad = Integer.parseInt(capacidadStr);
        
        Recurso nuevoRecurso = new Recurso(nombre, idTipoRecurso, capacidad, estado, idDepartamento, idsEquipamientoSeleccionados);

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
