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
import com.aulaclick.app.network.models.TipoRecurso;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionTiposActivity extends AppCompatActivity {

    private RecyclerView rvTipos;
    private TipoRecursoAdapter adapter;
    private List<TipoRecurso> listaTipos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_tipos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvTipos = findViewById(R.id.rvTipos);
        rvTipos.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TipoRecursoAdapter(listaTipos, this::showDeleteConfirmation);
        rvTipos.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddDialog());

        cargarTipos();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Cierra esta activity actual y vuelve a la anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarTipos() {
        ApiClient.getApiService().getTiposRecurso().enqueue(new Callback<List<TipoRecurso>>() {
            @Override
            public void onResponse(Call<List<TipoRecurso>> call, Response<List<TipoRecurso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaTipos = response.body();
                    adapter.updateData(listaTipos);
                }
            }

            @Override
            public void onFailure(Call<List<TipoRecurso>> call, Throwable t) {
                Toast.makeText(GestionTiposActivity.this, R.string.error_load_tipos, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.title_add_item, getString(R.string.catalog_tipo)));

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        final EditText input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        builder.setPositiveButton(R.string.btn_save, (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                crearTipo(name);
            } else {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void crearTipo(String name) {
        ApiClient.getApiService().crearTipoRecurso(new TipoRecurso(name)).enqueue(new Callback<TipoRecurso>() {
            @Override
            public void onResponse(Call<TipoRecurso> call, Response<TipoRecurso> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GestionTiposActivity.this, R.string.msg_tipo_created, Toast.LENGTH_SHORT).show();
                    cargarTipos();
                }
            }

            @Override
            public void onFailure(Call<TipoRecurso> call, Throwable t) {
                Toast.makeText(GestionTiposActivity.this, R.string.error_network_prefix, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(TipoRecurso tipo) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> eliminarTipo(tipo.getId()))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void eliminarTipo(Integer id) {
        ApiClient.getApiService().eliminarTipoRecurso(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    Toast.makeText(GestionTiposActivity.this, R.string.msg_deleted_successfully, Toast.LENGTH_SHORT).show();
                    cargarTipos();
                } else if (response.code() == 409) {
                    Toast.makeText(GestionTiposActivity.this, R.string.error_delete_conflict, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(GestionTiposActivity.this, R.string.error_delete_generic, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GestionTiposActivity.this, getString(R.string.error_network_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
