package com.aulaclick.app;

import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.Recurso;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnadirRecursoActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etCapacidad, etUbicacion;
    private AutoCompleteTextView actvTipo;
    private SwitchMaterial switchEstado;
    private MaterialButton btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_recurso);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombreRecurso);
        actvTipo = findViewById(R.id.actvTipoRecurso);
        etCapacidad = findViewById(R.id.etCapacidad);
        etUbicacion = findViewById(R.id.etUbicacion);
        switchEstado = findViewById(R.id.switchEstado);
        btnGuardar = findViewById(R.id.btnGuardarRecurso);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        btnGuardar.setOnClickListener(v -> guardarRecurso());
    }

    private void guardarRecurso() {
        String nombre = etNombre.getText().toString().trim();
        String tipo = actvTipo.getText().toString().trim();
        String capacidadStr = etCapacidad.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();
        String estado = switchEstado.isChecked() ? "Activo" : "Inactivo";

        if (nombre.isEmpty() || capacidadStr.isEmpty() || ubicacion.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer capacidad = Integer.parseInt(capacidadStr);
        // idDepartamento = 1 temporalmente según requerimiento
        Recurso nuevoRecurso = new Recurso(nombre, tipo, capacidad, ubicacion, estado, 1);

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
                    Toast.makeText(AnadirRecursoActivity.this, "Error al guardar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Recurso> call, Throwable t) {
                btnGuardar.setText("GUARDAR RECURSO");
                btnGuardar.setEnabled(true);
                Toast.makeText(AnadirRecursoActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
