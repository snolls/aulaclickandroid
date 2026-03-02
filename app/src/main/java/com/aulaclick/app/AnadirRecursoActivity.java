package com.aulaclick.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.Departamento;
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

    private TextInputEditText etNombre, etCapacidad, etUbicacion, etEquipamiento;
    private AutoCompleteTextView tvTipo, tvDepartamento;
    private SwitchMaterial switchEstado;
    private MaterialButton btnGuardar;

    private Integer idDepartamentoSeleccionado;
    private Integer idTipoRecursoSeleccionado;
    private Map<String, Integer> mapaDepartamentos = new HashMap<>();
    private Map<String, Integer> mapaTiposRecurso = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_recurso);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombreRecurso);
        tvTipo = findViewById(R.id.tvTipoRecurso);
        etCapacidad = findViewById(R.id.etCapacidad);
        etUbicacion = findViewById(R.id.etUbicacion);
        tvDepartamento = findViewById(R.id.tvDepartamento);
        etEquipamiento = findViewById(R.id.etEquipamiento);
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
                String seleccion = item.toString();
                idTipoRecursoSeleccionado = mapaTiposRecurso.get(seleccion);
            }
        });

        tvDepartamento.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item != null) {
                String seleccion = item.toString();
                idDepartamentoSeleccionado = mapaDepartamentos.get(seleccion);
            }
        });
    }

    private void cargarDatosDinámicos() {
        // Cargar Departamentos
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

        // Cargar Tipos de Recurso
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

    private void guardarRecurso() {
        String nombre = etNombre.getText().toString().trim();
        String capacidadStr = etCapacidad.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();
        String equipamiento = etEquipamiento.getText().toString().trim();
        String estado = switchEstado.isChecked() ? getString(R.string.status_activo) : "Inactivo"; // "Inactivo" should probably be in strings too

        if (nombre.isEmpty() || capacidadStr.isEmpty() || ubicacion.isEmpty() || 
            idDepartamentoSeleccionado == null || idTipoRecursoSeleccionado == null) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Integer capacidad = Integer.parseInt(capacidadStr);
        
        // Usamos el nombre del tipo seleccionado para el campo 'tipo' del modelo
        String tipoNombre = tvTipo.getText().toString();

        Recurso nuevoRecurso = new Recurso(nombre, tipoNombre, capacidad, ubicacion, estado, idDepartamentoSeleccionado, equipamiento);

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
