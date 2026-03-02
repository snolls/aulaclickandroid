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

        findViewById(R.id.btnAddDepartamento).setOnClickListener(v -> showAddDialog(getString(R.string.catalog_depto)));
        findViewById(R.id.btnAddTipo).setOnClickListener(v -> showAddDialog(getString(R.string.catalog_tipo)));
        findViewById(R.id.btnAddEquipamiento).setOnClickListener(v -> showAddDialog(getString(R.string.catalog_equip)));
    }

    private void showAddDialog(String titleLabel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_add_item, titleLabel));

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        final EditText input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        builder.setPositiveButton(R.string.btn_save, (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                saveItem(titleLabel, name);
            } else {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveItem(String type, String name) {
        if (type.equals(getString(R.string.catalog_depto))) {
            ApiClient.getApiService().crearDepartamento(new Departamento(name)).enqueue(new Callback<Departamento>() {
                @Override
                public void onResponse(Call<Departamento> call, Response<Departamento> response) {
                    if (response.isSuccessful()) Toast.makeText(AdminPanelActivity.this, R.string.msg_depto_created, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<Departamento> call, Throwable t) {}
            });
        } else if (type.equals(getString(R.string.catalog_tipo))) {
            ApiClient.getApiService().crearTipoRecurso(new TipoRecurso(name)).enqueue(new Callback<TipoRecurso>() {
                @Override
                public void onResponse(Call<TipoRecurso> call, Response<TipoRecurso> response) {
                    if (response.isSuccessful()) Toast.makeText(AdminPanelActivity.this, R.string.msg_tipo_created, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<TipoRecurso> call, Throwable t) {}
            });
        } else if (type.equals(getString(R.string.catalog_equip))) {
            ApiClient.getApiService().crearEquipamiento(new Equipamiento(name)).enqueue(new Callback<Equipamiento>() {
                @Override
                public void onResponse(Call<Equipamiento> call, Response<Equipamiento> response) {
                    if (response.isSuccessful()) Toast.makeText(AdminPanelActivity.this, R.string.msg_equip_created, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<Equipamiento> call, Throwable t) {}
            });
        }
    }
}
