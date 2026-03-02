package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.Departamento;
import com.aulaclick.app.network.models.Equipamiento;
import com.aulaclick.app.network.models.TipoRecurso;
import com.google.android.material.card.MaterialCardView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        MaterialCardView cardAddRecurso = findViewById(R.id.cardAddRecurso);
        cardAddRecurso.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnadirRecursoActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnAddDepartamento).setOnClickListener(v -> showAddDialog("Departamento"));
        findViewById(R.id.btnAddTipo).setOnClickListener(v -> showAddDialog("Tipo de Recurso"));
        findViewById(R.id.btnAddEquipamiento).setOnClickListener(v -> showAddDialog("Equipamiento"));
    }

    private void showAddDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir " + title);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        final EditText input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                saveItem(title, name);
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveItem(String type, String name) {
        if (type.equals("Departamento")) {
            ApiClient.getApiService().crearDepartamento(new Departamento(name)).enqueue(new Callback<Departamento>() {
                @Override
                public void onResponse(Call<Departamento> call, Response<Departamento> response) {
                    if (response.isSuccessful()) Toast.makeText(AdminPanelActivity.this, "Departamento creado", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<Departamento> call, Throwable t) {}
            });
        } else if (type.equals("Tipo de Recurso")) {
            ApiClient.getApiService().crearTipoRecurso(new TipoRecurso(name)).enqueue(new Callback<TipoRecurso>() {
                @Override
                public void onResponse(Call<TipoRecurso> call, Response<TipoRecurso> response) {
                    if (response.isSuccessful()) Toast.makeText(AdminPanelActivity.this, "Tipo de recurso creado", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<TipoRecurso> call, Throwable t) {}
            });
        } else if (type.equals("Equipamiento")) {
            ApiClient.getApiService().crearEquipamiento(new Equipamiento(name)).enqueue(new Callback<Equipamiento>() {
                @Override
                public void onResponse(Call<Equipamiento> call, Response<Equipamiento> response) {
                    if (response.isSuccessful()) Toast.makeText(AdminPanelActivity.this, "Equipamiento creado", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<Equipamiento> call, Throwable t) {}
            });
        }
    }
}
