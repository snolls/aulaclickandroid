package com.aulaclick.app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AnadirRecursoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_recurso);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnGuardarRecurso).setOnClickListener(v -> {
            Toast.makeText(this, "Guardando recurso...", Toast.LENGTH_SHORT).show();
            v.postDelayed(this::finish, 1500);
        });
    }
}
