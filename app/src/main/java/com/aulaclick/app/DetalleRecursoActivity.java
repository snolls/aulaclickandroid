package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DetalleRecursoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_recurso);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        String nombreRecurso = getIntent().getStringExtra("nombre");
        if (nombreRecurso != null) {
            toolbar.setTitle(nombreRecurso);
        }

        FloatingActionButton fab = findViewById(R.id.fabNuevaReserva);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevaReservaActivity.class);
            startActivity(intent);
        });
    }
}
