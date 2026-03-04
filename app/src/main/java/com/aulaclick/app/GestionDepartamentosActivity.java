package com.aulaclick.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.Departamento;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionDepartamentosActivity extends AppCompatActivity {

    private DepartamentoAdapter adapter;
    private List<Departamento> listaDepartamentos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_departamentos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rvDepartamentos = findViewById(R.id.rvDepartamentos);
        rvDepartamentos.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DepartamentoAdapter(listaDepartamentos, this::showDeleteConfirmation);
        rvDepartamentos.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddDialog());

        cargarDepartamentos();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Cierra esta activity actual y vuelve a la anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarDepartamentos() {
        ApiClient.getApiService().getDepartamentos().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Departamento>> call, @NonNull Response<List<Departamento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaDepartamentos = response.body();
                    adapter.updateData(listaDepartamentos);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Departamento>> call, @NonNull Throwable t) {
                Toast.makeText(GestionDepartamentosActivity.this, R.string.error_load_deptos, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.title_add_item, getString(R.string.catalog_depto)));

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        final EditText input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        builder.setPositiveButton(R.string.btn_save, (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                crearDepartamento(name);
            } else {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void crearDepartamento(String name) {
        ApiClient.getApiService().crearDepartamento(new Departamento(name)).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Departamento> call, @NonNull Response<Departamento> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionDepartamentosActivity.this, R.string.msg_depto_created, Toast.LENGTH_SHORT).show();
                    cargarDepartamentos();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Departamento> call, @NonNull Throwable t) {
                Toast.makeText(GestionDepartamentosActivity.this, R.string.error_network_prefix, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(Departamento depto) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> eliminarDepartamento(depto.getId()))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void eliminarDepartamento(Integer id) {
        ApiClient.getApiService().eliminarDepartamento(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    Toast.makeText(GestionDepartamentosActivity.this, R.string.msg_deleted_successfully, Toast.LENGTH_SHORT).show();
                    cargarDepartamentos();
                } else if (response.code() == 409) {
                    Toast.makeText(GestionDepartamentosActivity.this, R.string.error_delete_conflict, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(GestionDepartamentosActivity.this, R.string.error_delete_generic, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(GestionDepartamentosActivity.this, getString(R.string.error_network_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
