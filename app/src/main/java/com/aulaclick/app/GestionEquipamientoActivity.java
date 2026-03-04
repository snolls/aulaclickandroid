package com.aulaclick.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.network.models.Equipamiento;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionEquipamientoActivity extends AppCompatActivity {

    private RecyclerView rvEquipamiento;
    private EquipamientoAdapter adapter;
    private List<Equipamiento> listaEquipamiento = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_equipamiento);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvEquipamiento = findViewById(R.id.rvEquipamiento);
        rvEquipamiento.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EquipamientoAdapter(listaEquipamiento, this::showDeleteConfirmation);
        rvEquipamiento.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddDialog());

        cargarEquipamiento();
    }

    private void cargarEquipamiento() {
        ApiClient.getApiService().getEquipamientos().enqueue(new Callback<List<Equipamiento>>() {
            @Override
            public void onResponse(Call<List<Equipamiento>> call, Response<List<Equipamiento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaEquipamiento = response.body();
                    adapter.updateData(listaEquipamiento);
                }
            }

            @Override
            public void onFailure(Call<List<Equipamiento>> call, Throwable t) {
                Toast.makeText(GestionEquipamientoActivity.this, R.string.error_load_equipamiento, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.title_add_item, getString(R.string.catalog_equip)));

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        final EditText input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        builder.setPositiveButton(R.string.btn_save, (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                crearEquipamiento(name);
            } else {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void crearEquipamiento(String name) {
        ApiClient.getApiService().crearEquipamiento(new Equipamiento(name)).enqueue(new Callback<Equipamiento>() {
            @Override
            public void onResponse(Call<Equipamiento> call, Response<Equipamiento> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionEquipamientoActivity.this, R.string.msg_equip_created, Toast.LENGTH_SHORT).show();
                    cargarEquipamiento();
                }
            }

            @Override
            public void onFailure(Call<Equipamiento> call, Throwable t) {
                Toast.makeText(GestionEquipamientoActivity.this, R.string.error_network_prefix, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(Equipamiento equip) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> eliminarEquipamiento(equip.getId()))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void eliminarEquipamiento(Integer id) {
        ApiClient.getApiService().eliminarEquipamiento(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    Toast.makeText(GestionEquipamientoActivity.this, R.string.msg_deleted_successfully, Toast.LENGTH_SHORT).show();
                    cargarEquipamiento();
                } else if (response.code() == 409) {
                    Toast.makeText(GestionEquipamientoActivity.this, R.string.error_delete_conflict, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(GestionEquipamientoActivity.this, R.string.error_delete_generic, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GestionEquipamientoActivity.this, getString(R.string.error_network_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
