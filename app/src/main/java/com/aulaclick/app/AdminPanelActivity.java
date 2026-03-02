package com.aulaclick.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

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
    }
}
