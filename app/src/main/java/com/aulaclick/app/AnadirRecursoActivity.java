package com.aulaclick.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
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

    private TextInputEditText etNombre, etCapacidad, etUbicacion, etEquipamiento;
    private AutoCompleteTextView tvTipo, tvDepartamento;
    private SwitchMaterial switchEstado;
    private MaterialButton btnGuardar;

    private Map<String, Integer> deptoMap = new HashMap<>();
    private Map<String, Integer> tipoMap = new HashMap<>();

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

        cargarDatosDesplegables();

        btnGuardar.setOnClickListener(v -> guardarRecurso());
    }

    private void cargarDatosDesplegables() {
        // Cargar Departamentos
        ApiClient.getApiService().getDepartamentos().enqueue(new Callback<List<Departamento>>() {
            @Override
            public void onResponse(Call<List<Departamento>> call, Response<List<Departamento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> nombres = new ArrayList<>();
                    for (Departamento d : response.body()) {
                        nombres.add(d.getNombre());
                        deptoMap.put(d.getNombre(), d.getId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AnadirRecursoActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombres);
                    tvDepartamento.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Departamento>> call, Throwable t) {}
        });

        // Cargar Tipos de Recurso
        ApiClient.getApiService().getTiposRecurso().enqueue(new Callback<List<TipoRecurso>>() {
            @Override
            public void onResponse(Call<List<TipoRecurso>> call, Response<List<TipoRecurso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> nombres = new ArrayList<>();
                    for (TipoRecurso t : response.body()) {
                        nombres.add(t.getNombre());
                        tipoMap.put(t.getNombre(), t.getId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AnadirRecursoActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombres);
                    tvTipo.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<TipoRecurso>> call, Throwable t) {}
        });
    }

    private void guardarRecurso() {
        String nombre = etNombre.getText().toString().trim();
        String tipoTexto = tvTipo.getText().toString().trim();
        String capacidadStr = etCapacidad.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();
        String deptoTexto = tvDepartamento.getText().toString().trim();
        String equipamiento = etEquipamiento.getText().toString().trim();
        String estado = switchEstado.isChecked() ? "Activo" : "Inactivo";

        if (nombre.isEmpty() || capacidadStr.isEmpty() || ubicacion.isEmpty() || deptoTexto.isEmpty() || tipoTexto.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer capacidad = Integer.parseInt(capacidadStr);
        Integer idDepartamento = deptoMap.get(deptoTexto);

        if (idDepartamento == null) {
            Toast.makeText(this, "Departamento no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        Recurso nuevoRecurso = new Recurso(nombre, tipoTexto, capacidad, ubicacion, estado, idDepartamento, equipamiento);

        btnGuardar.setText("Guardando...");
        btnGuardar.setEnabled(false);

        ApiClient.getApiService().crearRecurso(nuevoRecurso).enqueue(new Callback<Recurso>() {
            @Override
            public void onResponse(Call<Recurso> call, Response<Recurso> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AnadirRecursoActivity.this, "Recurso guardado", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnGuardar.setText("GUARDAR RECURSO");
                    btnGuardar.setEnabled(true);
                    Toast.makeText(AnadirRecursoActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Recurso> call, Throwable t) {
                btnGuardar.setText("GUARDAR RECURSO");
                btnGuardar.setEnabled(true);
                Toast.makeText(AnadirRecursoActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
